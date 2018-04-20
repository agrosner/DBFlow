package com.raizlabs.dbflow5.database

/**
 * Defines how we preserve database data. whether that is something backed up or prepackaged with app.
 */
interface DatabaseRestoreHelper {

    /**
     * Copies over the prepackaged DB into the main DB then deletes the existing DB to save storage space. If
     * we have a backup that exists
     *
     * @param databaseName    The name of the database to copy over
     * @param prepackagedName The name of the prepackaged db file
     */
    fun movePrepackagedDatabase(dbFlowDatabase: DBFlowDatabase,
                                backupHelper: OpenHelper?,
                                tempDbFileName: String,
                                databaseName: String, prepackagedName: String)

    /**
     * If integrity check fails, this method will use the backup db to fix itself. In order to prevent
     * loss of data, please backup often!
     */
    fun restoreBackUp(dbFlowDatabase: DBFlowDatabase): Boolean

    /**
     * Will use the already existing app database if [DBFlowDatabase.backupEnabled] is true. If the existing
     * is not there we will try to use the prepackaged database for that purpose.
     *
     * @param databaseName    The name of the database to restore
     * @param prepackagedName The name of the prepackaged db file
     */
    fun restoreDatabase(dbFlowDatabase: DBFlowDatabase,
                        backupHelper: OpenHelper?,
                        databaseName: String,
                        prepackagedName: String)

    fun backupDatabase(dbFlowDatabase: DBFlowDatabase, tempDbFileName: String)
}

expect class PlatformDatabaseRestoreHelper : DatabaseRestoreHelper
