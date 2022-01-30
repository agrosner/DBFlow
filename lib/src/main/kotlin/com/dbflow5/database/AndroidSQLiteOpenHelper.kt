package com.dbflow5.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.OpenHelperCreator

/**
 * Description: Wraps around the [SQLiteOpenHelper] and provides extra features for use in this library.
 */
open class AndroidSQLiteOpenHelper(
    private val context: Context,
    private val dbFlowDatabase: DBFlowDatabase,
    listener: DatabaseCallback?,
    private val databaseHelperDelegate: DatabaseHelperDelegate = DatabaseHelperDelegate(
        context,
        listener,
        dbFlowDatabase
    ),
) : SQLiteOpenHelper(
    context,
    if (dbFlowDatabase.isInMemory) null else dbFlowDatabase.databaseFileName,
    null,
    dbFlowDatabase.databaseVersion,
), OpenHelper, OpenHelperDelegate by databaseHelperDelegate {


    private var androidDatabase: AndroidDatabase? = null
    private val _databaseName = dbFlowDatabase.databaseFileName

    override val database: DatabaseWrapper
        get() {
            if (androidDatabase == null || androidDatabase?.database?.isOpen == false) {
                androidDatabase = AndroidDatabase.from(writableDatabase, dbFlowDatabase)
            }
            return androidDatabase!!
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
        databaseHelperDelegate.onConfigure(AndroidDatabase.from(db, dbFlowDatabase))
    }

    override fun onCreate(db: SQLiteDatabase) {
        databaseHelperDelegate.onCreate(AndroidDatabase.from(db, dbFlowDatabase))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        databaseHelperDelegate.onUpgrade(
            AndroidDatabase.from(db, dbFlowDatabase),
            oldVersion,
            newVersion
        )
    }

    override fun onOpen(db: SQLiteDatabase) {
        databaseHelperDelegate.onOpen(
            AndroidDatabase.from(
                db,
                dbFlowDatabase
            )
        )
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        databaseHelperDelegate.onDowngrade(
            AndroidDatabase.from(
                db,
                dbFlowDatabase
            ), oldVersion, newVersion
        )
    }

    override fun closeDB() {
        androidDatabase?.database?.close()
    }

    override fun deleteDB() {
        context.deleteDatabase(_databaseName)
    }

    companion object {
        @JvmStatic
        fun createHelperCreator(context: Context): OpenHelperCreator =
            OpenHelperCreator { db: DBFlowDatabase, databaseCallback: DatabaseCallback? ->
                AndroidSQLiteOpenHelper(context, db, databaseCallback)
            }
    }

}

