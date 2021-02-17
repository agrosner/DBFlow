package com.dbflow5.provider

import android.content.ContentProvider
import com.dbflow5.config.FlowManager
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.OperatorGroup
import com.dbflow5.structure.BaseModel
import com.dbflow5.structure.Model

/**
 * Description: Provides a base implementation of a [Model] backed
 * by a content provider. All model operations are overridden using the [ContentUtils].
 * Consider using a [BaseSyncableProviderModel] if you wish to
 * keep modifications locally from the [ContentProvider]
 */
abstract class BaseProviderModel : BaseModel(), ModelProvider {

    override fun delete(wrapper: DatabaseWrapper): Boolean = ContentUtils.delete(FlowManager.contentResolver, deleteUri, this) > 0

    override fun save(wrapper: DatabaseWrapper): Boolean {
        val count = ContentUtils.update(FlowManager.contentResolver, updateUri, this)
        return if (count == 0) {
            insert(wrapper) > 0
        } else {
            count > 0
        }
    }

    override fun update(wrapper: DatabaseWrapper): Boolean = ContentUtils.update(FlowManager.contentResolver, updateUri, this) > 0

    override fun insert(wrapper: DatabaseWrapper): Long = if (ContentUtils.insert(FlowManager.contentResolver, insertUri, this) != null) 1 else 0

    /**
     * Runs a query on the [ContentProvider] to see if it returns data.
     *
     * @return true if this model exists in the [ContentProvider] based on its primary keys.
     */
    override fun exists(wrapper: DatabaseWrapper): Boolean {
        val cursor = ContentUtils.query(FlowManager.contentResolver,
            queryUri, modelAdapter.getPrimaryConditionClause(this), "")
        val exists = cursor != null && cursor.count > 0
        cursor?.close()
        return exists
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> load(whereOperatorGroup: OperatorGroup,
                          orderBy: String?,
                          wrapper: DatabaseWrapper,
                          vararg columns: String?): T? {
        ContentUtils.query(FlowManager.contentResolver,
            queryUri, whereOperatorGroup, orderBy, *columns)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return modelAdapter.loadFromCursor(cursor, wrapper) as T
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> load(wrapper: DatabaseWrapper): T? = load(modelAdapter.getPrimaryConditionClause(this), "", wrapper) as T?

}
