package com.dbflow5.provider

/**
 * Description:
 */

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Description: Used as a stub, include this in order to work around Android O changes to [ContentProvider]
 */
class StubContentProvider : ContentProvider() {

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun query(uri: Uri,
                       projection: Array<String>?,
                       selection: String?,
                       selectionArgs: Array<String>?,
                       sortOrder: String?): Cursor? {
        return null
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun update(uri: Uri, values: ContentValues?,
                        selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }
}
