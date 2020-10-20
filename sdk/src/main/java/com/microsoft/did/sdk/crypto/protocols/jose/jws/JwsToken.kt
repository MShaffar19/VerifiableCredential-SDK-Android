package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.Constants.CREDENTIAL_PRESENTATION_FORMAT
import com.microsoft.did.sdk.util.byteArrayToString
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.controlflow.SignatureException
import com.microsoft.did.sdk.util.log.SdkLog
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.stringToByteArray
import java.util.Locale

/**
 * Class for containing JWS token operations.
 * @class
 */
class JwsToken private constructor(
    private val payload: String,
    signatures: List<JwsSignature> = emptyList(),
    private val serializer: Serializer
) {

    val signatures: MutableList<JwsSignature> = signatures.toMutableList()

    companion object {
        fun deserialize(jws: String, serializer: Serializer): JwsToken {
            val compactRegex = Regex("([A-Za-z\\d_-]*)\\.([A-Za-z\\d_-]*)\\.([A-Za-z\\d_-]*)")
            val compactMatches = compactRegex.matchEntire(jws.trim())
            when {
                compactMatches != null -> {
                    // compact JWS format
                    val protected = compactMatches.groupValues[1]
                    val payload = compactMatches.groupValues[2]
                    val signature = compactMatches.groupValues[3]
                    val jwsSignatureObject = JwsSignature(
                        protected = protected,
                        header = null,
                        signature = signature
                    )
                    return JwsToken(payload, listOf(jwsSignatureObject), serializer)
                }
                jws.toLowerCase(Locale.ENGLISH).contains("\"signatures\"") -> { // check for signature or signatures
                    // GENERAL
                    val token = serializer.parse(JwsGeneralJson.serializer(), jws)
                    return JwsToken(
                        payload = token.payload,
                        signatures = token.signatures,
                        serializer = serializer
                    )
                }
                jws.toLowerCase(Locale.ENGLISH).contains("\"signature\"") -> {
                    // Flat
                    val token = serializer.parse(JwsFlatJson.serializer(), jws)
                    return JwsToken(
                        payload = token.payload,
                        signatures = listOf(
                            JwsSignature(
                                protected = token.protected,
                                header = token.header,
                                signature = token.signature
                            )
                        ),
                        serializer = serializer
                    )
                }
                else -> {
                    // Unidentifiable garbage
                    throw SignatureException("Unable to parse JWS token.")
                }
            }
        }
    }

    constructor(content: ByteArray, serializer: Serializer) : this(Base64Url.encode(content), emptyList(), serializer)

    constructor(content: String, serializer: Serializer) : this(Base64Url.encode(stringToByteArray(content)), emptyList(), serializer)

    /**
     * Serialize a JWS token object from token.
     */
    fun serialize(serializer: Serializer, format: JwsFormat = JwsFormat.Compact): String {
        return when (format) {
            JwsFormat.Compact -> {
                intermediateCompactSerialize()
            }
            JwsFormat.FlatJson -> {
                val jws = intermediateFlatJsonSerialize()
                serializer.stringify(JwsFlatJson.serializer(), jws)
            }
            JwsFormat.GeneralJson -> {
                val jws = intermediateGeneralJsonSerialize()
                serializer.stringify(JwsGeneralJson.serializer(), jws)
            }
        }
    }

    private fun intermediateCompactSerialize(): String {
        val signature = this.signatures.firstOrNull()
        if (signature == null) {
            val jws = JwsCompact(
                protected = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0",
                payload = this.payload,
                signature = ""
            )
            return "${jws.protected}.${jws.payload}"
        }
        val jws = JwsCompact(
            protected = signature.protected,
            payload = this.payload,
            signature = signature.signature
        )
        return "${jws.protected}.${jws.payload}.${jws.signature}"
    }

    private fun intermediateFlatJsonSerialize(): JwsFlatJson {
        val signature = this.signatures.firstOrNull() ?: throw SignatureException("This JWS token contains no signatures")
        return JwsFlatJson(
            protected = signature.protected,
            header = signature.header,
            payload = this.payload,
            signature = signature.signature
        )
    }

    private fun intermediateGeneralJsonSerialize(): JwsGeneralJson {
        if (this.signatures.count() == 0) {
            throw SignatureException("This JWS token contains no signatures")
        }
        return JwsGeneralJson(
            payload = this.payload,
            signatures = this.signatures.toList()
        )
    }

    /**
     * Adds a signature using the given key
     * @param signingKeyReference reference to signing key
     * @param cryptoOperations CryptoOperations used to form the signatures
     * @param header optional headers added to the signature
     */
    fun sign(signingKeyReference: String, cryptoOperations: CryptoOperations, header: Map<String, String> = emptyMap()) {
        var startLoad = System.nanoTime()
        // 1. Get the signing key's metadata
        val signingKey = cryptoOperations.keyStore.getPrivateKey(signingKeyReference).getKey()

        var loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - cryptoOperations.keyStore.getPrivateKey(: ${loadTimeInMs}ms")


        startLoad = System.nanoTime()
        // 3. Compute headers
        val headers = header.toMutableMap()
        val protected = mutableMapOf<String, String>()

        val algorithmName = if (!headers.containsKey(JoseConstants.Alg.value)) {
            signingKey.alg?.also { protected[JoseConstants.Alg.value] = it }
                ?: throw KeyException("No algorithm defined for key $signingKeyReference")
        } else {
            headers[JoseConstants.Alg.value]!!
        }

        val kid = headers[JoseConstants.Kid.value]
        if (kid == null) {
            protected[JoseConstants.Kid.value] = signingKey.kid
        } else {
            protected[JoseConstants.Kid.value] = kid
        }

        val type = headers[JoseConstants.Type.value]
        if (type != null)
            protected[JoseConstants.Type.value] = CREDENTIAL_PRESENTATION_FORMAT

        var encodedProtected = ""
        if (protected.isNotEmpty()) {
            val jsonProtected = serializer.stringify(protected, String::class, String::class)
            encodedProtected = Base64Url.encode(stringToByteArray(jsonProtected))
        }
        loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - compute headers: ${loadTimeInMs}ms")
        startLoad = System.nanoTime()
        val signatureInput = stringToByteArray("$encodedProtected.${this.payload}")
        loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - signature input: ${loadTimeInMs}ms")

        startLoad = System.nanoTime()
        val signature = cryptoOperations.sign(
            signatureInput, signingKeyReference,
            JwaCryptoConverter.jwaAlgToWebCrypto(algorithmName)
        )
        loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - sign: ${loadTimeInMs}ms")

        startLoad = System.nanoTime()
        val signatureBase64 = Base64Url.encode(signature)

        this.signatures.add(
            JwsSignature(
                protected = encodedProtected,
                header = headers,
                signature = signatureBase64
            )
        )
        loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - base64+add: ${loadTimeInMs}ms")
    }

    /**
     *Verify the JWS signatures
     */
    fun verify(cryptoOperations: CryptoOperations, publicKeys: List<PublicKey> = emptyList(), all: Boolean = false): Boolean {
        val results = this.signatures.map {
            val fullyQuantifiedKid = it.getKid(serializer) ?: ""
            val kid = JwaCryptoConverter.extractDidAndKeyId(fullyQuantifiedKid).second
            val signatureInput = "${it.protected}.${this.payload}"
            val publicKey = cryptoOperations.keyStore.getPublicKeyById(kid)
            if (publicKey != null) {
                verifyWithKey(cryptoOperations, signatureInput, it, publicKey)
            } else {
                // use one of the provided public Keys
                val key = publicKeys.firstOrNull {
                    it.kid.endsWith(kid)
                }
                when {
                    key != null -> {
                        verifyWithKey(cryptoOperations, signatureInput, it, key)
                    }
                    publicKeys.isNotEmpty() -> {
                        verifyWithKey(cryptoOperations, signatureInput, it, publicKeys.first())
                    }
                    else -> false
                }
            }
        }
        return if (all) {
            results.reduce { result, valid ->
                result && valid
            }
        } else {
            results.reduce { result, valid ->
                result || valid
            }
        }
    }

    private fun verifyWithKey(crypto: CryptoOperations, data: String, signature: JwsSignature, key: PublicKey): Boolean {
        val alg = signature.getAlg(serializer) ?: throw SignatureException("This signature contains no algorithm.")
        val subtleAlg = JwaCryptoConverter.jwaAlgToWebCrypto(alg)
        val subtle = crypto.subtleCryptoFactory.getMessageSigner(subtleAlg.name, SubtleCryptoScope.PUBLIC)
        val cryptoKey = subtle.importKey(
            KeyFormat.Jwk, key.toJWK(), subtleAlg,
            true, key.key_ops ?: listOf(KeyUsage.Verify)
        )
        val rawSignature = Base64Url.decode(signature.signature)
        val rawData = stringToByteArray(data)
        return subtle.verify(subtleAlg, cryptoKey, rawSignature, rawData)
    }

    /**
     * Plaintext payload content
     */
    fun content(): String {
        return byteArrayToString(Base64Url.decode(this.payload))
    }

}