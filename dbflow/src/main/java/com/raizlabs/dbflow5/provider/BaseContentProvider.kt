package com.raizlabs.dbflow5.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.net.Uri
import com.raizlabs.dbflow5.config.DatabaseDefinition
import com.raizlabs.dbflow5.config.DatabaseHolder
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.transaction.ITransaction

/**
 * Description: The base provider class that [com.raizlabs.android.dbflow5.annotation.provider.ContentProvider]
 * extend when generated.
 */
abstract class BaseContentProvider
protected constructor(databaseHolderClass: Class<out DatabaseHolder>? = null) : ContentProvider() {

    protected open var moduleClass: Class<out DatabaseHolder>? = databaseHolderClass

    protected val database: DatabaseDefinition by lazy { FlowManager.getDatabase(databaseName) }

    protected abstract val databaseName: String

    override fun onCreate(): Boolean {
        // If this is a module, then we need to initialize the module as part
        // of the creation process. We can assume the framework has been general
        // framework has been initialized.
        moduleClass
            ?.let { FlowManager.initModule(it) }
            ?: context?.let { FlowManager.init(it) }
        return true
    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {

        val count = database.executeTransaction(object : ITransaction<IntArray> {
            override fun execute(databaseWrapper: DatabaseWrapper): IntArray {
                val count = intArrayOf(0)
                for (contentValues in values) {
                    count[0] += bulkInsert(uri, contentValues)
                }
                return count
            }
        })

        context?.contentResolver?.notifyChange(uri, null)
        return count[0]
    }

    protected abstract fun bulkInsert(uri: Uri, contentValues: ContentValues): Int

}
