package com.microsoft.portableIdentity.sdk.identifier.document.service

import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.Endpoint
import kotlinx.serialization.Polymorphic

interface IdentifierDocService {
    val id: String
    val type: String
    @Polymorphic
    val serviceEndpoint: Endpoint
}