package com.raizlabs.android.dbflow.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.database.DatabaseWrapper

class RealContentProvider : ContentProvider() {

    lateinit var database: DatabaseWrapper

    override fun onCreate(): Boolean {
        FlowManager.init(FlowConfig.Builder(context).build())
        database = FlowManager.getDatabase(TestDatabase::class.java).writableDatabase
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