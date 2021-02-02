package com.dbflow5.provider

import android.content.ContentProvider
import com.dbflow5.config.FlowManager.contentResolver
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.OperatorGroup
import com.dbflow5.structure.BaseModel
import com.dbflow5.structure.Model

/**
 * Description: Provides a base implementation of a [Model] backed
 * by a content provider. All operations sync with the content provider in this app from a [ContentProvider]
 */
abstract class BaseSyncableProviderModel : BaseModel(), ModelProvider {

    override fun insert(wrapper: DatabaseWrapper): Long {
        val rowId = super.insert(wrapper)
        ContentUtils.insert(contentResolver, insertUri, this)
        return rowId
    }

    override fun save(wrapper: DatabaseWrapper): Boolean = super.save(wrapper) && if (exists(wrapper)) {
        ContentUtils.update(contentResolver, updateUri, this) > 0
    } else {
        ContentUtils.insert(contentResolver, insertUri, this) != null
    }

    override fun delete(wrapper: DatabaseWrapper): Boolean = super.delete(wrapper) && ContentUtils.delete(contentResolver, deleteUri, this) > 0

    override fun update(wrapper: DatabaseWrapper): Boolean = super.update(wrapper) && ContentUtils.update(contentResolver, updateUri, this) > 0

    @Suppress("UNCHECKED_CAST")
    override fun <T> load(whereOperatorGroup: OperatorGroup,
                          orderBy: String?,
                          wrapper: DatabaseWrapper,
                          vararg columns: String?): T? {
        val cursor = ContentUtils.query(contentResolver,
            queryUri, whereOperatorGroup, orderBy, *columns)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val model: T = modelAdapter.loadFromCursor(cursor, wrapper) as T
                cursor.close()
                return model
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> load(wrapper: DatabaseWrapper): T? = load(modelAdapter.getPrimaryConditionClause(this), "", wrapper) as T?
}
