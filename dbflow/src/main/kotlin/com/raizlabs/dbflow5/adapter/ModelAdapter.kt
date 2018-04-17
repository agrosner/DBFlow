package com.raizlabs.dbflow5.adapter

import android.content.ContentValues
import com.raizlabs.dbflow5.config.DBFlowDatabase

actual abstract class ModelAdapter<T : Any> actual constructor(database: DBFlowDatabase) : InternalModelAdapter<T>(database) {

    fun bindToContentValues(contentValues: ContentValues, model: T) {
        bindToInsertValues(contentValues, model)
    }

    fun bindToInsertValues(contentValues: ContentValues, model: T) {
        throw RuntimeException("ContentValues are no longer generated automatically. To enable it," +
            " set generateContentValues = true in @Table for $tableName.")
    }
}
