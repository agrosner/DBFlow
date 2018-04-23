package com.raizlabs.dbflow5.database

actual class PlatformDatabaseRestoreHelper : DatabaseRestoreHelper {
    override fun movePrepackagedDatabase(dbFlowDatabase: DBFlowDatabase, backupHelper: OpenHelper?,
                                         tempDbFileName: String, databaseName: String, prepackagedName: String) {
        TODO("not implemented")
    }

    override fun restoreBackUp(dbFlowDatabase: DBFlowDatabase): Boolean {
        TODO("not implemented")
    }

    override fun restoreDatabase(dbFlowDatabase: DBFlowDatabase, backupHelper: OpenHelper?,
                                 databaseName: String, prepackagedName: String) {
        TODO("not implemented")
    }

    override fun backupDatabase(dbFlowDatabase: DBFlowDatabase, tempDbFileName: String) {
        TODO("not implemented")
    }
}