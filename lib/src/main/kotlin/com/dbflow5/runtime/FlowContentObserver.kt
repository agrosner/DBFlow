package com.dbflow5.runtime

import android.annotation.TargetApi
import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Handler
import com.dbflow5.TABLE_QUERY_PARAM
import com.dbflow5.config.DatabaseConfig
import com.dbflow5.config.FlowLog
import com.dbflow5.config.FlowManager
import com.dbflow5.getNotificationUri
import com.dbflow5.query.NameAlias
import com.dbflow5.query.Operator
import com.dbflow5.query.SQLOperator
import com.dbflow5.structure.ChangeAction
import com.dbflow5.structure.Model
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger

/**
 * Description: Listens for [Model] changes. Register for specific
 * tables with [.addModelChangeListener].
 * Provides ability to register and deregister listeners for when data is inserted, deleted, updated, and saved if the device is
 * above [VERSION_CODES.JELLY_BEAN]. If below it will only provide one callback. This is to be paired
 * with the [ContentResolverNotifier] specified in the [DatabaseConfig].
 *
 * @param [contentAuthority] Reuse the same authority as defined in your manifest and [ContentResolverNotifier].
 */
open class FlowContentObserver(private val contentAuthority: String,
                               handler: Handler? = null) : ContentObserver(handler) {

    private val modelChangeListeners = CopyOnWriteArraySet<OnModelStateChangedListener>()
    private val onTableChangedListeners = CopyOnWriteArraySet<OnTableChangedListener>()
    private val registeredTables = hashMapOf<String, Class<*>>()
    private val notificationUris = hashSetOf<Uri>()
    private val tableUris = hashSetOf<Uri>()

    protected var isInTransaction = false
    private var notifyAllUris = false

    val isSubscribed: Boolean
        get() = !registeredTables.isEmpty()

    /**
     * Listens for specific model changes. This is only available in [VERSION_CODES.JELLY_BEAN]
     * or higher due to the api of [ContentObserver].
     */
    interface OnModelStateChangedListener {

        /**
         * Notifies that the state of a [Model]
         * has changed for the table this is registered for.
         *
         * @param table            The table that this change occurred on. This is ONLY available on [VERSION_CODES.JELLY_BEAN]
         * and up.
         * @param action           The action on the model. for versions prior to [VERSION_CODES.JELLY_BEAN] ,
         * the [Action.CHANGE] will always be called for any action.
         * @param primaryKeyValues The array of primary [SQLOperator] of what changed. Call [SQLOperator.columnName]
         * and [SQLOperator.value] to get each information.
         */
        fun onModelStateChanged(table: Class<*>?, action: ChangeAction, primaryKeyValues: Array<SQLOperator>)
    }

    interface ContentChangeListener : OnModelStateChangedListener, OnTableChangedListener

    /**
     * If true, this class will get specific when it needs to, such as using all [Action] qualifiers.
     * If false, it only uses the [Action.CHANGE] action in callbacks.
     *
     * @param notifyAllUris
     */
    fun setNotifyAllUris(notifyAllUris: Boolean) {
        this.notifyAllUris = notifyAllUris
    }

    /**
     * Starts a transaction where when it is finished, this class will receive a notification of all of the changes by
     * calling [.endTransactionAndNotify]. Note it may lead to unexpected behavior if called from different threads.
     */
    fun beginTransaction() {
        if (!isInTransaction) {
            isInTransaction = true
        }
    }

    /**
     * Ends the transaction where it finishes, and will call [.onChange] for Jelly Bean and up for
     * every URI called (if set), or [.onChange] once for lower than Jelly bean.
     */
    open fun endTransactionAndNotify() {
        if (isInTransaction) {
            isInTransaction = false

            if (Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
                onChange(true)
            } else {
                synchronized(notificationUris) {
                    for (uri in notificationUris) {
                        onChange(uri, true)
                    }
                    notificationUris.clear()
                }
                synchronized(tableUris) {
                    for (uri in tableUris) {
                        for (onTableChangedListener in onTableChangedListeners) {
                            uri.authority?.let { authority ->
                                uri.fragment?.let { fragment ->
                                    onTableChangedListener.onTableChanged(registeredTables[authority],
                                        ChangeAction.valueOf(fragment))
                                }
                            }
                        }
                    }
                    tableUris.clear()
                }
            }
        }
    }

    /**
     * Add a listener for model changes
     *
     * @param modelChangeListener Generic model change events from an [Action]
     */
    fun addModelChangeListener(modelChangeListener: OnModelStateChangedListener) {
        modelChangeListeners.add(modelChangeListener)
    }

    /**
     * Removes a listener for model changes
     *
     * @param modelChangeListener Generic model change events from a [Action]
     */
    fun removeModelChangeListener(modelChangeListener: OnModelStateChangedListener) {
        modelChangeListeners.remove(modelChangeListener)
    }

    fun addOnTableChangedListener(onTableChangedListener: OnTableChangedListener) {
        onTableChangedListeners.add(onTableChangedListener)
    }

    fun removeTableChangedListener(onTableChangedListener: OnTableChangedListener) {
        onTableChangedListeners.remove(onTableChangedListener)
    }

    /**
     * Add a listener for model + table changes
     *
     * @param contentChangeListener Generic model change events from an [Action]
     */
    fun addContentChangeListener(contentChangeListener: ContentChangeListener) {
        modelChangeListeners.add(contentChangeListener)
        onTableChangedListeners.add(contentChangeListener)
    }

    /**
     * Removes a listener for model + table changes
     *
     * @param contentChangeListener Generic model change events from a [Action]
     */
    fun removeContentChangeListener(contentChangeListener: ContentChangeListener) {
        modelChangeListeners.remove(contentChangeListener)
        onTableChangedListeners.remove(contentChangeListener)
    }

    /**
     * Registers the observer for model change events for specific class.
     */
    open fun registerForContentChanges(context: Context,
                                       table: Class<*>) {
        registerForContentChanges(context.contentResolver, table)
    }

    /**
     * Registers the observer for model change events for specific class.
     */
    fun registerForContentChanges(contentResolver: ContentResolver,
                                  table: Class<*>) {
        contentResolver.registerContentObserver(
            getNotificationUri(contentAuthority, table, null), true, this)
        REGISTERED_COUNT.incrementAndGet()
        if (!registeredTables.containsValue(table)) {
            registeredTables.put(FlowManager.getTableName(table), table)
        }
    }

    /**
     * Unregisters this list for model change events
     */
    fun unregisterForContentChanges(context: Context) {
        context.contentResolver.unregisterContentObserver(this)
        REGISTERED_COUNT.decrementAndGet()
        registeredTables.clear()
    }

    override fun onChange(selfChange: Boolean) {
        modelChangeListeners.forEach { it.onModelStateChanged(null, ChangeAction.CHANGE, arrayOf()) }
        onTableChangedListeners.forEach { it.onTableChanged(null, ChangeAction.CHANGE) }
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    override fun onChange(selfChange: Boolean, uri: Uri) {
        onChange(uri, false)
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    private fun onChange(uri: Uri, calledInternally: Boolean) {
        var locUri = uri
        val fragment = locUri.fragment
        val tableName = locUri.getQueryParameter(TABLE_QUERY_PARAM)

        val queryNames = locUri.queryParameterNames
        val columnsChanged = arrayListOf<SQLOperator>()
        queryNames.asSequence()
            .filter { it != TABLE_QUERY_PARAM }
            .forEach { key ->
                val param = Uri.decode(locUri.getQueryParameter(key))
                val columnName = Uri.decode(key)
                columnsChanged += Operator.op<Any>(NameAlias.Builder(columnName).build()).eq(param)
            }

        val table = tableName?.let { t -> registeredTables[t] }
        if (table != null && fragment != null) {
            var action = ChangeAction.valueOf(fragment)
            if (!isInTransaction) {
                modelChangeListeners.forEach {
                    it.onModelStateChanged(table, action, columnsChanged.toTypedArray())
                }

                if (!calledInternally) {
                    onTableChangedListeners.forEach { it.onTableChanged(table, action) }
                }
            } else {
                // convert this uri to a CHANGE op if we don't care about individual changes.
                if (!notifyAllUris) {
                    action = ChangeAction.CHANGE
                    locUri = getNotificationUri(contentAuthority, table, action)
                }
                synchronized(notificationUris) {
                    // add and keep track of unique notification uris for when transaction completes.
                    notificationUris.add(locUri)
                }

                synchronized(tableUris) {
                    tableUris.add(getNotificationUri(contentAuthority, table, action))
                }
            }
        } else {
            FlowLog.log(FlowLog.Level.W, "Received URI change for unregistered table $tableName . URI ignored.")
        }
    }

    companion object {

        private val REGISTERED_COUNT = AtomicInteger(0)
        private var forceNotify = false

        /**
         * @return true if we have registered for content changes. Otherwise we do not notify
         * in [SqlUtils]
         * for efficiency purposes.
         */
        fun shouldNotify(): Boolean {
            return forceNotify || REGISTERED_COUNT.get() > 0
        }

        /**
         * Removes count of observers registered, so we do not send out calls when [Model] changes.
         */
        fun clearRegisteredObserverCount() {
            REGISTERED_COUNT.set(0)
        }

        /**
         * @param forceNotify if true, this will force itself to notify whenever a model changes even though
         * an observer (appears to be) is not registered.
         */
        fun setShouldForceNotify(forceNotify: Boolean) {
            Companion.forceNotify = forceNotify
        }
    }

}
