package com.raizlabs.dbflow5.provider

import android.content.ContentProvider

import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.query.OperatorGroup
import com.raizlabs.dbflow5.structure.BaseModel
import com.raizlabs.dbflow5.structure.Model
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor

/**
 * Description: Provides a base implementation of a [Model] backed
 * by a content provider. All operations sync with the content provider in this app from a [ContentProvider]
 */
abstract class BaseSyncableProviderModel : BaseModel(), ModelProvider {

    override fun insert(wrapper: DatabaseWrapper): Long {
        val rowId = super.insert(wrapper)
        ContentUtils.insert(insertUri, wrapper)
        return rowId
    }

    override fun save(wrapper: DatabaseWrapper): Boolean {
        return if (exists(wrapper)) {
            super.save(wrapper) && ContentUtils.update(updateUri, wrapper) > 0
        } else {
            super.save(wrapper) && ContentUtils.insert(insertUri, wrapper) != null
        }
    }

    override fun delete(wrapper: DatabaseWrapper): Boolean
            = super.delete(wrapper) && ContentUtils.delete(deleteUri, wrapper) > 0

    override fun update(wrapper: DatabaseWrapper): Boolean
            = super.update(wrapper) && ContentUtils.update(updateUri, wrapper) > 0

    override fun load(whereOperatorGroup: OperatorGroup,
                      orderBy: String?,
                      wrapper: DatabaseWrapper,
                      vararg columns: String?) {
        val cursor = ContentUtils.query(FlowManager.context.contentResolver,
                queryUri, whereOperatorGroup, orderBy, *columns)
        cursor?.let {
            val flowCursor = FlowCursor.from(cursor)
            if (flowCursor.moveToFirst()) {
                modelAdapter.loadFromCursor(flowCursor, this, wrapper)
                flowCursor.close()
            }
        }
    }

    override fun load(wrapper: DatabaseWrapper) {
        load(modelAdapter.getPrimaryConditionClause(this), "", wrapper)
    }
}
