package com.raizlabs.dbflow5.query

import android.content.ContentValues
import com.raizlabs.dbflow5.KClass
import com.raizlabs.dbflow5.addContentValues
import com.raizlabs.dbflow5.sql.Query

actual class Set<T : Any> internal actual constructor(queryBuilderBase: Query, table: KClass<T>)
    : InternalSet<T>(queryBuilderBase, table) {

    fun conditionValues(contentValues: ContentValues) = apply {
        addContentValues(contentValues, operatorGroup)
    }
}
