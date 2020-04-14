/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcResponseContent
import com.microsoft.portableIdentity.sdk.auth.protectors.OidcResponseFormatter
import com.microsoft.portableIdentity.sdk.auth.protectors.OidcResponseSigner
import com.microsoft.portableIdentity.sdk.auth.requests.IssuanceRequest
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.requests.PresentationRequest
import com.microsoft.portableIdentity.sdk.auth.requests.Request
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.auth.responses.Response
import com.microsoft.portableIdentity.sdk.auth.validators.OidcRequestValidator
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialContent
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.CardRepository
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.AuthenticationException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import io.ktor.http.Url
import io.ktor.util.toMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardManager @Inject constructor(
    private val picRepository: CardRepository,
    private val validator: OidcRequestValidator, // TODO: should this be a generic Validator?
    private val signer: OidcResponseSigner,
    private val formatter: OidcResponseFormatter
) {

    /**
     * Get Presentation Request.
     *
     * @param uri OpenID Connect Uri that points to the presentation request.
     *
     * @return PresentationRequest object that contains all attestations.
     */
    suspend fun getPresentationRequest(uri: String): PresentationRequest {
        val url = Url(uri)
        if (url.protocol.name != "openid") {
            throw AuthenticationException("request format not supported")
        }

        val requestParameters = url.parameters.toMap()
        val serializedToken = requestParameters["request"]?.first()
        if (serializedToken != null) {
            return PresentationRequest(requestParameters, serializedToken)
        }

        val requestUri = requestParameters["request_uri"]?.first() ?: throw AuthenticationException("Cannot fetch request: No request uri found")
        val requestToken = picRepository.getRequest(requestUri) ?: throw AuthenticationException("Cannot fetch request: No request token found")
        return PresentationRequest(requestParameters, requestToken)
    }

    /**
     * Get Issuance Request from a contract.
     *
     * @param contractUrl url that the contract is fetched from
     *
     * @return IssuanceRequest object containing all metadata about what is needed to fulfill request including display information.
     */
    suspend fun getIssuanceRequest(contractUrl: String): IssuanceRequest {
        val contract = picRepository.getContract(contractUrl) ?: throw AuthenticationException("No contract found")
        return IssuanceRequest(contract, contractUrl)
    }

    /**
     * Validate an OpenID Connect Request.
     */
    suspend fun isValid(request: OidcRequest): Result<Boolean, Exception> {
        return validator.validate(request)
    }

    /**
     * Get contract Urls from VC Attestations.
     * Private Preview: gets first contract from each Verifiable Credential Attestation.
     */
    fun getContractUrls(request: OidcRequest): List<String> {
        val attestations = request.content.attestations ?: return emptyList()
        val contracts = mutableListOf<String>()
        attestations.presentations.forEach {
            contracts.add(it.contracts.first())
        }
        return contracts
    }

    /**
     * Create Response from Request.
     */
    fun createResponse(request: Request): Response {
        return when (request) {
            is PresentationRequest -> PresentationResponse(request)
            is IssuanceRequest -> IssuanceResponse(request)
            else -> throw AuthenticationException("No Response Type that matches Request Type.")
        }
    }

    /**
     * Send a Response.
     */
    suspend fun sendResponse(response: OidcResponse, responderIdentifier: Identifier): Any {
        val responseContent = formatter.formContents(response, responderIdentifier, responderIdentifier.signatureKeyReference)
        val serializedResponseContent = Serializer.stringify(OidcResponseContent.serializer(), responseContent)
        val signedResponse = signer.sign(serializedResponseContent, responderIdentifier)
        val serializedSignedResponse = signedResponse.serialize()
        return when (response) {
            is IssuanceResponse -> picRepository.sendResponse(response.audience, serializedSignedResponse) ?: throw AuthenticationException("Unable to send response.")
            is PresentationResponse -> picRepository.sendPresentationResponse(response.audience, serializedSignedResponse) ?: throw AuthenticationException("Unable to send response.")
            else -> throw AuthenticationException("Response Not Supported")
        }
    }

    /**
     * Puts together card and saves in repository.
     */
    suspend fun saveCard(signedVerifiableCredential: String, contract: PicContract) {
        val card = createCard(signedVerifiableCredential, contract)
        picRepository.insert(card)
    }

    fun createCard(signedVerifiableCredential: String, contract: PicContract): PortableIdentityCard {
        val contents = unwrapSignedVerifiableCredential(signedVerifiableCredential)
        val verifiableCredential = VerifiableCredential(signedVerifiableCredential, contents)
        return PortableIdentityCard(contents.jti, verifiableCredential, contract.display)
    }

    private fun unwrapSignedVerifiableCredential(signedVerifiableCredential: String): VerifiableCredentialContent {
        val token = JwsToken.deserialize(signedVerifiableCredential)
        return Serializer.parse(VerifiableCredentialContent.serializer(), token.content())
    }

    fun getCards(): LiveData<List<PortableIdentityCard>> {
        return picRepository.getAllCards()
    }
}