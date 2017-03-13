package com.raizlabs.android.dbflow.customhelper

import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener
import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperDelegate
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.OpenHelper

/**
 * Description: A custom open helper class that you can specify.
 */
class CustomOpenHelper(flowManager: DatabaseDefinition,
                       listener: DatabaseHelperListener) : OpenHelper {

    override fun performRestoreFromBackup() {
    }

    override fun getDatabase(): DatabaseWrapper? {
        return null
    }

    override fun getDelegate(): DatabaseHelperDelegate? {
        return null
    }

    override fun isDatabaseIntegrityOk(): Boolean {
        return false
    }

    override fun backupDB() {

    }

    override fun setDatabaseListener(helperListener: DatabaseHelperListener) {

    }

    override fun closeDB() {
    }
}
