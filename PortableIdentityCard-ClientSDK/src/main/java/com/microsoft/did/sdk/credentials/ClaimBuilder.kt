package com.microsoft.did.sdk.credentials

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.utilities.ILogger
import com.microsoft.did.sdk.utilities.Serializer

class ClaimBuilder(forClass: ClaimClass? = null, private val logger: ILogger) {
    var context: String? = null
    var type: String? = null
    var issuerName: String? = forClass?.issuerName
    var claimLogo: ClaimClass.ClaimLogo? = forClass?.claimLogo
    var claimName: String? = forClass?.claimName
    var hexBackgroundColor: String? = forClass?.hexBackgroundColor
    var hexFontColor: String? = forClass?.hexFontColor
    var moreInfo: String? = forClass?.moreInfo
    val helpLinks: MutableMap<String, String> = forClass?.helpLinks?.toMutableMap() ?: mutableMapOf()
    private val claimClassDescriptions: MutableList<ClaimDescription> = forClass?.claimDescriptions?.toMutableList() ?: mutableListOf()
    var readPermissionDescription: PermissionDescription? = forClass?.readPermissionDescription

    private val claimDescriptions: MutableList<ClaimDescription> = mutableListOf()
    private val claimDetails: MutableList<Map<String, String>> = mutableListOf()


    fun addClassDescription(header: String, body: String) {
        claimClassDescriptions.add(ClaimDescription(header, body))
    }

    fun addClaimDescription(header: String, body: String) {
        claimDescriptions.add(ClaimDescription(header, body))
    }

    fun addClaimDetail(claim: Map<String, String>) {
        claimDetails.add(claim)
    }

    fun buildClass(): ClaimClass {
        return ClaimClass(
            issuerName,
            claimLogo,
            claimName,
            hexBackgroundColor,
            hexFontColor,
            moreInfo,
            helpLinks,
            claimClassDescriptions,
            readPermissionDescription
        )
    }

    fun buildObject(classUri: String, identifier: Identifier, cryptoOperations: CryptoOperations? = null): ClaimObject {
        if (context.isNullOrBlank() || type.isNullOrBlank()) {
            throw logger.error("Context and Type must be set.")
        }
        val claims = if (cryptoOperations != null) {
            val serializedData = Serializer.stringify(claimDetails, Map::class)
            val token = JwsToken(serializedData, logger = logger)
            token.sign(identifier.signatureKeyReference, cryptoOperations)
            SignedClaimDetail(
                data = token.serialize(JwsFormat.Compact)
            )
        } else {
            UnsignedClaimDetail(
                data = claimDetails.toList()
            )
        }
        return ClaimObject(
            classUri,
            context!!,
            type!!,
            identifier.document.id,
            claimDescriptions,
            claims
        )
    }
}