package com.raizlabs.android.dbflow.structure.provider

import android.content.ContentProvider

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.OperatorGroup
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Provides a base implementation of a [Model] backed
 * by a content provider. All operations sync with the content provider in this app from a [ContentProvider]
 */
abstract class BaseSyncableProviderModel : BaseModel(), ModelProvider {

    override fun DatabaseWrapper.insert(): Long {
        val rowId = super.insert()
        ContentUtils.insert(insertUri, this)
        return rowId
    }

    override fun DatabaseWrapper.save(): Boolean {
        return if (exists()) {
            super.save() && ContentUtils.update(updateUri, this) > 0
        } else {
            super.save() && ContentUtils.insert(insertUri, this) != null
        }
    }

    override fun DatabaseWrapper.delete(): Boolean = super.delete() && ContentUtils.delete(deleteUri, this) > 0

    override fun DatabaseWrapper.update(): Boolean = super.update() && ContentUtils.update(updateUri, this) > 0

    override fun load(whereOperatorGroup: OperatorGroup,
                      orderBy: String?, vararg columns: String?) {
        val cursor = ContentUtils.query(FlowManager.context.contentResolver,
                queryUri, whereOperatorGroup, orderBy, *columns)
        cursor?.let {
            val flowCursor = FlowCursor.from(cursor)
            if (flowCursor.moveToFirst()) {
                modelAdapter.loadFromCursor(flowCursor, this)
                flowCursor.close()
            }
        }
    }

    override fun load() {
        load(modelAdapter.getPrimaryConditionClause(this), "")
    }
}
