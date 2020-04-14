package com.microsoft.portableIdentity.sdk.registrars

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.Identifier

/**
 * @interface defining methods and properties
 * to be implemented by specific registration methods.
 */
abstract class Registrar {

    /**
     * Registers the identifier document on the ledger
     * returning the identifier generated by the registrar.
     * @param identifierDocument to be registered.
     * @param signingKeyReference reference to the key to be used for signing request.
     * @param encryptionKeyReference reference to the key to be used for encrypting request.
     * @param recoveryKeyReference reference to the key to be used if identifier has to be recovered later.
     * @return Identifier that was created and saved in database.
     * @throws Error if unable to register Identifier Document.
     */
    abstract suspend fun register(signatureKeyReference: String, recoveryKeyReference: String, cryptoOperations: CryptoOperations): Identifier
}