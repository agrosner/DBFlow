package com.raizlabs.android.dbflow.structure.backup

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.FlowTestCase

import org.junit.Test

import org.junit.Assert.assertTrue

/**
 * Description:
 */
class BackupTest : FlowTestCase() {

    @Test
    fun testBackup() {

        Delete.table(BackupModel::class.java)

        val backupModel = BackupModel()
        backupModel.name = "Test"
        backupModel.save()

        assertTrue(backupModel.exists())

        Delete.table(BackupModel::class.java)

    }
}
