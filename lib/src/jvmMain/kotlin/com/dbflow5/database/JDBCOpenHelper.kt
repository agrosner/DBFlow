package com.dbflow5.database

import com.dbflow5.config.GeneratedDatabase

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

    private val connection = JDBCConnection(
        name = generatedDatabase.openHelperName,
        version = generatedDatabase.version,
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

            override fun onUpgrade(db: JDBCConnectionWrapper, oldVersion: Int, newVersion: Int) {
                databaseHelperDelegate.onUpgrade(
                    JDBCDatabase(generatedDatabase, db),
                    oldVersion, newVersion
                )
            }

            override fun onDowngrade(db: JDBCConnectionWrapper, oldVersion: Int, newVersion: Int) {
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

    override val database: JDBCDatabase by DatabasePropertyDelegate {
        JDBCDatabase(generatedDatabase, connection.writableDatabase)
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        connection.setWriteAheadLoggingEnabled(enabled)
    }

    override fun setDatabaseListener(callback: DatabaseCallback?) {
        databaseHelperDelegate.setDatabaseHelperListener(callback)
    }


    override fun closeDB() {
        connection.close()
    }

    override fun deleteDB() {
        connection.delete()
    }
}

actual fun OpenHelper(db: GeneratedDatabase, callback: DatabaseCallback?): OpenHelper = JDBCOpenHelper(db, callback)
