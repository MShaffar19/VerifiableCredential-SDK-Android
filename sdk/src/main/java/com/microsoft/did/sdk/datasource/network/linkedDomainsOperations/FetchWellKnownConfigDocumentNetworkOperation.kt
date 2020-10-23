/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.linkedDomainsOperations

import com.microsoft.did.sdk.credential.service.models.serviceResponses.LinkedDomainsResponse
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.UnableToFetchWellKnownConfigDocument
import com.microsoft.did.sdk.util.serializer.Serializer
import retrofit2.Response
import javax.inject.Inject

class FetchWellKnownConfigDocumentNetworkOperation @Inject constructor(val url: String, apiProvider: ApiProvider) :
    GetNetworkOperation<LinkedDomainsResponse, LinkedDomainsResponse>() {

    override val call: suspend () -> Response<LinkedDomainsResponse> =
        { apiProvider.linkedDomainsApis.fetchWellKnownConfigDocument("$url/${Constants.WELL_KNOWN_CONFIG_DOCUMENT_LOCATION}") }

/*    override fun onSuccess(response: Response<LinkedDomainsResponse>): Result<List<JwsToken>> {
        val linkedDidsJwt = mutableListOf<JwsToken>()
        response.body()?.linkedDids?.forEach { linkedDidsJwtString ->
            linkedDidsJwt.add(JwsToken.deserialize(linkedDidsJwtString, Serializer()))
        }
        return Result.Success(linkedDidsJwt)
    }*/

    override fun onFailure(response: Response<LinkedDomainsResponse>): Result<Nothing> {
        return Result.Failure(UnableToFetchWellKnownConfigDocument("Unable to fetch well-known config document from $url"))
    }
}