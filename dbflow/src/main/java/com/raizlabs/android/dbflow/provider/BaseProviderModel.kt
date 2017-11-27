package com.raizlabs.android.dbflow.provider

import android.content.ContentProvider
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.query.OperatorGroup
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.database.DatabaseWrapper
import com.raizlabs.android.dbflow.database.FlowCursor

/**
 * Description: Provides a base implementation of a [Model] backed
 * by a content provider. All model operations are overridden using the [ContentUtils].
 * Consider using a [BaseSyncableProviderModel] if you wish to
 * keep modifications locally from the [ContentProvider]
 */
abstract class BaseProviderModel : BaseModel(), ModelProvider {

    override fun delete(wrapper: DatabaseWrapper): Boolean = ContentUtils.delete(deleteUri, this) > 0

    override fun save(wrapper: DatabaseWrapper): Boolean {
        val count = ContentUtils.update(updateUri, this)
        return if (count == 0) {
            ContentUtils.insert(insertUri, this) != null
        } else {
            count > 0
        }
    }

    override fun update(wrapper: DatabaseWrapper): Boolean = ContentUtils.update(updateUri, this) > 0

    override fun insert(wrapper: DatabaseWrapper): Long {
        ContentUtils.insert(insertUri, wrapper)
        return 0
    }

    /**
     * Runs a query on the [ContentProvider] to see if it returns data.
     *
     * @return true if this model exists in the [ContentProvider] based on its primary keys.
     */
    override fun exists(wrapper: DatabaseWrapper): Boolean {
        val cursor = ContentUtils.query(FlowManager.context.contentResolver,
                queryUri, modelAdapter.getPrimaryConditionClause(this), "")
        val exists = cursor != null && cursor.count > 0
        cursor?.close()
        return exists
    }

    override fun load(whereOperatorGroup: OperatorGroup,
                      orderBy: String?,
                      wrapper: DatabaseWrapper,
                      vararg columns: String?) {
        val cursor = ContentUtils.query(FlowManager.context.contentResolver,
                queryUri, whereOperatorGroup, orderBy, *columns)
        if (cursor != null) {
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
