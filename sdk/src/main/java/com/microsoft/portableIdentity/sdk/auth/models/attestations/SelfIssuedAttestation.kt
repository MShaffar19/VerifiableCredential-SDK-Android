package com.microsoft.portableIdentity.sdk.auth.models.attestations

import kotlinx.serialization.Serializable

/**
 * EXPERIMENTAL
 */
@Serializable
data class SelfIssuedAttestation (
    val claims: List<ClaimAttestation>
)