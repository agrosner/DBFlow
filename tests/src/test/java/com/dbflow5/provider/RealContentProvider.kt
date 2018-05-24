package com.dbflow5.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.dbflow5.TestDatabase
import com.dbflow5.config.FlowConfig
import com.dbflow5.config.FlowManager
import com.dbflow5.config.database
import com.dbflow5.database.DatabaseWrapper

class RealContentProvider : ContentProvider() {

    lateinit var database: DatabaseWrapper

    override fun onCreate(): Boolean {
        FlowManager.init(FlowConfig.Builder(context).build())
        database = database<TestDatabase>()
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }
}