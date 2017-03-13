package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.FlowTestCase
import org.junit.Assert.*
import org.junit.Test

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
        assertEquals(String(model.blob!!.blob), TEST_BLOB)
    }

    companion object {

        private val TEST_BLOB = "This is a test"
    }
}