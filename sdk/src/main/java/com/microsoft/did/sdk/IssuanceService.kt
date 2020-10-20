// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.RequestedVcMap
import com.microsoft.did.sdk.credential.service.protectors.IssuanceResponseFormatter
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchContractNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.formVerifiableCredential
import com.microsoft.did.sdk.util.log.SdkLog
import com.microsoft.did.sdk.util.serializer.Serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssuanceService @Inject constructor(
    private val identifierManager: IdentifierManager,
    private val exchangeService: ExchangeService,
    private val apiProvider: ApiProvider,
    private val issuanceResponseFormatter: IssuanceResponseFormatter,
    private val serializer: Serializer
) {

    /**
     * Load a Issuance Request from a contract.
     *
     * @param contractUrl url that the contract is fetched from
     */
    suspend fun getRequest(contractUrl: String): Result<IssuanceRequest> {
        return runResultTry {
            val startLoad = System.nanoTime()
            val contract = fetchContract(contractUrl).abortOnError()
            val request = IssuanceRequest(contract, contractUrl)
            val loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
            SdkLog.v("mPerf - SDK load time (ISSUANCE}): ${loadTimeInMs}ms")
            Result.Success(request)
        }
    }

    private suspend fun fetchContract(url: String) = FetchContractNetworkOperation(
        url,
        apiProvider
    ).fire()

    /**
     * Send an Issuance Response.
     *
     * @param response IssuanceResponse containing the requested attestations
     * @param enablePairwise when true a pairwise identifier will be used for this communication,
     * otherwise the master identifier is used which may allow the relying party to correlate the user
     */
    suspend fun sendResponse(
        response: IssuanceResponse,
        enablePairwise: Boolean = true
    ): Result<VerifiableCredential> {
        return runResultTry {
            val startLoad = System.nanoTime()
            val masterIdentifier = identifierManager.getMasterIdentifier().abortOnError()
            val verifiableCredential = if (enablePairwise) {
                val pairwiseIdentifier =
                    identifierManager.createPairwiseIdentifier(masterIdentifier, response.request.entityIdentifier).abortOnError()
                val requestedVcMap = exchangeVcsInIssuanceRequest(response, pairwiseIdentifier).abortOnError()
                formAndSendResponse(response, pairwiseIdentifier, requestedVcMap).abortOnError()
            } else {
                val requestedVcMap = response.requestedVcMap
                formAndSendResponse(response, masterIdentifier, requestedVcMap).abortOnError()
            }
            val loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
            SdkLog.v("mPerf - SDK complete response time (ISSUANCE}): ${loadTimeInMs}ms")
            Result.Success(verifiableCredential)
        }
    }

    private suspend fun exchangeVcsInIssuanceRequest(
        response: IssuanceResponse,
        pairwiseIdentifier: Identifier
    ): Result<RequestedVcMap> {
        return runResultTry {
            val exchangedVcMap = response.requestedVcMap.mapValues {
                val owner = identifierManager.getIdentifierById(it.value.contents.sub).abortOnError()
                exchangeService.getExchangedVerifiableCredential(it.value, owner, pairwiseIdentifier).abortOnError()
            }
            Result.Success(exchangedVcMap as RequestedVcMap)
        }
    }

    private suspend fun formAndSendResponse(
        response: IssuanceResponse,
        responder: Identifier,
        requestedVcMap: RequestedVcMap,
        expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS
    ): Result<VerifiableCredential> {
        val formattedResponse = issuanceResponseFormatter.formatResponse(
            requestedVcMap = requestedVcMap,
            issuanceResponse = response,
            responder = responder,
            expiryInSeconds = expiryInSeconds
        )
        val rawVerifiableCredentialResult = SendVerifiableCredentialIssuanceRequestNetworkOperation(
            response.audience,
            formattedResponse,
            apiProvider
        ).fire()

        return when (rawVerifiableCredentialResult) {
            is Result.Success -> Result.Success(formVerifiableCredential(rawVerifiableCredentialResult.payload, serializer))
            is Result.Failure -> rawVerifiableCredentialResult
        }
    }
}