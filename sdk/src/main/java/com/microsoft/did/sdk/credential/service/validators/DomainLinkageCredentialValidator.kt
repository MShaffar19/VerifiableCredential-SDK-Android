// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.validators

interface DomainLinkageCredentialValidator {

    suspend fun validate(domainLinkageCredentialJwt: String, rpDid: String, rpDomain: String): Boolean
}