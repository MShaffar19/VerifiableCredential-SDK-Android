// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.repository.dao

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.datasource.db.dao.IdentifierDao
import com.microsoft.did.sdk.identifier.models.Identifier
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test

class IdentifierDaoInstrumentedTest {
    private var identifierDao: IdentifierDao
    private var sdkDatabase: SdkDatabase

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        sdkDatabase = Room.inMemoryDatabaseBuilder(context, SdkDatabase::class.java).build()
        identifierDao = sdkDatabase.identifierDao()
    }

    @Test
    fun insertAndRetrieveIdentifierByIdTest() {
        val suppliedIdentifier = Identifier(
            "did:ion:test:testId",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName"
        )
        identifierDao.insert(suppliedIdentifier)
        val actualIdentifier = identifierDao.queryByIdentifier(suppliedIdentifier.id)
        assertThat(actualIdentifier).isEqualTo(suppliedIdentifier)
    }

    @Test
    fun insertIdentifierWithEmptyIdTest() {
        val suppliedIdentifier = Identifier(
            "",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName"
        )
        identifierDao.insert(suppliedIdentifier)
        val actualIdentifier = identifierDao.queryByIdentifier(suppliedIdentifier.id)
        assertThat(actualIdentifier).isEqualTo(suppliedIdentifier)
    }

    @Test
    fun retrieveIdentifierByNonExistingIdTest() {
        val nonExistingId = "nonExistingId"
        val actualIdentifier = identifierDao.queryByIdentifier(nonExistingId)
        assertThat(actualIdentifier).isNull()
    }

    @Test
    fun insertIdentifiersWithSameIdsFailingForSecondInsertTest() {
        val suppliedIdentifier1 = Identifier(
            "did:ion:test:testId",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName"
        )
        val suppliedIdentifier2 = Identifier(
            "did:ion:test:testId",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName"
        )
        identifierDao.insert(suppliedIdentifier1)
        Assertions.assertThatThrownBy { identifierDao.insert(suppliedIdentifier2) }
            .isInstanceOf(android.database.sqlite.SQLiteConstraintException::class.java)
        val actualIdentifier = identifierDao.queryByIdentifier(suppliedIdentifier1.id)
        assertThat(actualIdentifier).isEqualTo(suppliedIdentifier1)
    }

    @Test
    fun insertAndRetrieveIdentifierByNameTest() {
        val suppliedIdentifier = Identifier(
            "did:ion:test:testId",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName"
        )
        identifierDao.insert(suppliedIdentifier)
        val actualIdentifierName = "testIdentifierName"
        val actualIdentifier = identifierDao.queryByName(actualIdentifierName)
        assertThat(actualIdentifier).isEqualTo(suppliedIdentifier)
    }

    @Test
    fun retrieveIdentifierByNonExistingNameTest() {
        val nonExistingName = "nonExistingName"
        val actualIdentifier = identifierDao.queryByName(nonExistingName)
        assertThat(actualIdentifier).isNull()
    }

    @Test
    fun insertTwoIdentifiersWithSameNameAndRetrieveTest() {
        val suppliedIdentifier1 = Identifier(
            "did:ion:test:testId1",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName"
        )
        val suppliedIdentifier2 = Identifier(
            "did:ion:test:testId2",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName"
        )
        identifierDao.insert(suppliedIdentifier1)
        identifierDao.insert(suppliedIdentifier2)
        val actualIdentifierName = "testIdentifierName"
        var actualIdentifier = identifierDao.queryByName(actualIdentifierName)
        assertThat(actualIdentifier).isEqualTo(suppliedIdentifier1)
        actualIdentifier = identifierDao.queryByIdentifier(suppliedIdentifier2.id)
        assertThat(actualIdentifier).isEqualTo(suppliedIdentifier2)
    }

    @After
    fun tearDown() {
        sdkDatabase.close()
    }
}