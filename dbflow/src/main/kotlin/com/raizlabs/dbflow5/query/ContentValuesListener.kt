package com.raizlabs.dbflow5.query

import android.content.ContentValues
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.annotation.provider.ContentProvider
import com.raizlabs.dbflow5.structure.Model

/**
 * Description: Called after the declared [ContentValues] are bound. It enables
 * us to listen and add custom behavior to the [ContentValues]. These must be
 * defined in a [Model] class to register properly.
 *
 *
 * This class will no longer get called during updates unless explicit call to
 * [ModelAdapter.bindToContentValues]
 * or [ModelAdapter.bindToInsertValues] with setting [Table.generateContentValues] to true.
 *
 * @see SQLiteStatementListener
 */
@Deprecated("")
interface ContentValuesListener {

    /**
     * Called during an [Model.update] and at the end of
     * [ModelAdapter.bindToContentValues]
     * . It enables you to custom-ly change the values as necessary during update to the database.
     *
     * @param contentValues The content values to bind to for an update.
     */
    fun onBindToContentValues(contentValues: ContentValues)

    /**
     * Called during an [Model.update] and at the end of
     * [ModelAdapter.bindToInsertValues].
     * It enables you to custom-ly change the values as necessary during insert
     * to the database for a [ContentProvider].
     *
     * @param contentValues The content values to insert into DB for a [ContentProvider]
     */
    fun onBindToInsertValues(contentValues: ContentValues)
}
