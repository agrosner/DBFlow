package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.test.FlowTestCase

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

class BlobModelTest : FlowTestCase() {

    @Test
    fun testBlob() {

        val blobModel = BlobModel()
        blobModel.blob = Blob(TEST_BLOB.toByteArray())
        blobModel.save()

        assertTrue(blobModel.exists())

        val model = Select().from(BlobModel::class.java)
                .where(BlobModel_Table.key.`is`(blobModel.key))
                .querySingle()

        assertNotNull(model)
        assertNotNull(model!!.blob)
        assertEquals(String(model.blob.blob), TEST_BLOB)
    }

    companion object {

        private val TEST_BLOB = "This is a test"
    }
}