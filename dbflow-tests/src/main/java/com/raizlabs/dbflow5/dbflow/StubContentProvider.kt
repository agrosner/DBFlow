package com.raizlabs.dbflow5.dbflow

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Description: Used as a stub, include this in order to work around Android O changes to [ContentProvider]
 */
open class StubContentProvider : ContentProvider() {

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        TODO("not implemented")
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        TODO("not implemented")
    }

    override fun onCreate(): Boolean = true

    override fun update(uri: Uri?, values: ContentValues?, selection: String?,
                        selectionArgs: Array<out String>?): Int {
        TODO("not implemented")
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("not implemented")
    }

    override fun getType(uri: Uri?): String {
        TODO("not implemented")
    }
}