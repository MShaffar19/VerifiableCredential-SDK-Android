package com.microsoft.did.sdk.crypto.protocols.jose.jws

import android.util.Base64
import com.microsoft.did.sdk.util.Constants
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import org.assertj.core.api.Assertions
import org.junit.Test
import kotlin.random.Random

class JwsTokenTest {
    private val keyRef = Base64.encodeToString(Random.nextBytes(8), Constants.BASE64_URL_SAFE)
    private val key: ECKey
    private val payload: String = "{\"iss\":\"joe\"," +
        "\"exp\":1300819380," +
        "\"http://example.com/is_root\":true}"

    init {
        /* This is the payload used for all the operations below */
        key = ECKeyGenerator(Curve.SECP256K1)
            .keyID(keyRef)
            .generate()
    }

    @Test
    fun deserializeAndVerifyTest() {
        val token = JwsToken.deserialize("eyJhbGciOiJFUzI1NiJ9." +
            "eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFt" +
            "cGxlLmNvbS9pc19yb290Ijp0cnVlfQ." +
            "DtEhU3ljbEg8L38VWAfUAqOyKAM6-Xx-F4GawxaepmXFCgfTjDxw5djxLa8ISlSApmWQxfKTUJqPP3-Kg6NU1Q")
        Assertions.assertThat(
            token.verify(listOf(ECKey.parse("{\"kty\":\"EC\"," +
          "\"crv\":\"P-256\"," +
          "\"x\":\"f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU\"," +
          "\"y\":\"x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0\"," +
          "\"d\":\"jpsQnnGQmL-YBIffH1136cspYG6-0iY7X1fCE9-E9LI\"" +
         "}").toPublicKey()))).isTrue
        Assertions.assertThat(token.content()).isEqualTo(payload)
    }

    @Test
    fun signAndVerify() {
        val testData = Random.Default.nextBytes(32)
        val token = JwsToken(testData)
        token.sign(key)
        val serialized = token.serialize()
        val verifyToken = JwsToken.deserialize(serialized)
        Assertions.assertThat(verifyToken.verify()).isFalse
        Assertions.assertThat(verifyToken.verify(listOf(key.toPublicKey()))).isTrue
    }
}