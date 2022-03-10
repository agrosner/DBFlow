package com.dbflow5.database

import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.delegates.databaseProperty

class JDBCOpenHelper(
    private val generatedDatabase: GeneratedDatabase,
    callback: DatabaseCallback?,
    private val databaseHelperDelegate: DatabaseHelperDelegate = DatabaseHelperDelegate(
        callback,
        generatedDatabase,
        helper = DatabaseHelper(
            JDBCMigrationFileHelper(), generatedDatabase
        ),
        databaseBackup = DatabaseBackup(generatedDatabase),
    ),
) : OpenHelper, OpenHelperDelegate by databaseHelperDelegate {

    private val connection by lazy {
        JDBCConnection(
            name = generatedDatabase.openHelperName,
            version = generatedDatabase.databaseVersion,
            callback = object : JDBCConnectionCallback {
                override fun onOpen(db: JDBCConnectionWrapper) {
                    databaseHelperDelegate.onOpen(
                        JDBCDatabase(generatedDatabase, db)
                    )
                }

                override fun onCreate(db: JDBCConnectionWrapper) {
                    databaseHelperDelegate.onCreate(
                        JDBCDatabase(generatedDatabase, db)
                    )
                }

                override fun onUpgrade(
                    db: JDBCConnectionWrapper,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    databaseHelperDelegate.onUpgrade(
                        JDBCDatabase(generatedDatabase, db),
                        oldVersion, newVersion
                    )
                }

                override fun onDowngrade(
                    db: JDBCConnectionWrapper,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    databaseHelperDelegate.onDowngrade(
                        JDBCDatabase(generatedDatabase, db),
                        oldVersion, newVersion,
                    )
                }

                override fun onConfigure(db: JDBCConnectionWrapper) {
                    databaseHelperDelegate.onConfigure(
                        JDBCDatabase(generatedDatabase, db)
                    )
                }
            }
        )
    }

    override val database: JDBCDatabase by databaseProperty {
        JDBCDatabase(generatedDatabase, connection.writableDatabase)
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        connection.setWriteAheadLoggingEnabled(enabled)
    }

    override fun setDatabaseListener(callback: DatabaseCallback?) {
        databaseHelperDelegate.setDatabaseHelperListener(callback)
    }

    override fun close() {
        connection.close()
    }

    override fun delete() {
        connection.delete()
    }
}

actual fun OpenHelper(db: GeneratedDatabase, callback: DatabaseCallback?): OpenHelper =
    JDBCOpenHelper(db, callback)
