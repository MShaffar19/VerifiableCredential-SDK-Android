// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.utilities.ConsoleLogger
import com.microsoft.did.sdk.utilities.Serializer

class MockPublicKey(key: JsonWebKey): PublicKey(key, ConsoleLogger()) {
    override fun minimumAlphabeticJwk(): String {
        return Serializer.stringify(JsonWebKey.serializer(), this.toJWK())
    }

    override fun toJWK(): JsonWebKey {
        return JsonWebKey(
            kty = "RSA",
            alg = this.alg,
            kid = kid
        )
    }
}