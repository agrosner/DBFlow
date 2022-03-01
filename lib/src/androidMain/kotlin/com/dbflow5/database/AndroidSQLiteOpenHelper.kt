package com.dbflow5.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dbflow5.config.GeneratedDatabase

/**
 * Description: Wraps around the [SQLiteOpenHelper] and provides extra features for use in this library.
 */
class AndroidSQLiteOpenHelper(
    private val context: Context,
    private val generatedDatabase: GeneratedDatabase,
    listener: DatabaseCallback?,
    private val databaseHelperDelegate: DatabaseHelperDelegate = DatabaseHelperDelegate(
        listener,
        generatedDatabase,
        helper = DatabaseHelper(
            AndroidMigrationFileHelper(context),
            generatedDatabase
        ),
        databaseBackup = DatabaseBackup(context, generatedDatabase),
    ),
) : SQLiteOpenHelper(
    context,
    generatedDatabase.openHelperName,
    null,
    generatedDatabase.databaseVersion,
), OpenHelper, OpenHelperDelegate by databaseHelperDelegate {

    private val _databaseName = generatedDatabase.databaseFileName

    override val database: AndroidDatabase by DatabasePropertyDelegate {
        AndroidDatabase.from(
            writableDatabase,
            generatedDatabase
        )
    }

    /**
     * Set a listener to listen for specific DB events and perform an action before we execute this classes
     * specific methods.
     *
     * @param callback
     */
    override fun setDatabaseListener(callback: DatabaseCallback?) {
        databaseHelperDelegate.setDatabaseHelperListener(callback)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        databaseHelperDelegate.onConfigure(AndroidDatabase.from(db, generatedDatabase))
    }

    override fun onCreate(db: SQLiteDatabase) {
        databaseHelperDelegate.onCreate(AndroidDatabase.from(db, generatedDatabase))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        databaseHelperDelegate.onUpgrade(
            AndroidDatabase.from(db, generatedDatabase),
            oldVersion,
            newVersion
        )
    }

    override fun onOpen(db: SQLiteDatabase) {
        databaseHelperDelegate.onOpen(
            AndroidDatabase.from(
                db,
                generatedDatabase
            )
        )
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        databaseHelperDelegate.onDowngrade(
            AndroidDatabase.from(
                db,
                generatedDatabase
            ), oldVersion, newVersion
        )
    }

    override fun closeDB() {
        database.database.close()
    }

    override fun deleteDB() {
        context.deleteDatabase(_databaseName)
    }

    companion object {
        @JvmStatic
        fun createHelperCreator(context: Context): OpenHelperCreator =
            OpenHelperCreator { db: GeneratedDatabase, databaseCallback: DatabaseCallback? ->
                AndroidSQLiteOpenHelper(context, db, databaseCallback)
            }
    }

}

