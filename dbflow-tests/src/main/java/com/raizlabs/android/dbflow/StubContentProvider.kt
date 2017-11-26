package com.raizlabs.android.dbflow

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.raizlabs.android.dbflow.runtime.BaseContentProvider

/**
 * Description:
 */
class StubContentProvider : BaseContentProvider() {
    override val databaseName: String
        get() = TODO("not implemented")

    override fun bulkInsert(uri: Uri, contentValues: ContentValues): Int {
        TODO("not implemented")
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        TODO("not implemented")
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        TODO("not implemented")
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("not implemented")
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("not implemented")
    }

    override fun getType(uri: Uri?): String {
        TODO("not implemented")
    }
}