package com.dbflow5.runtime

import android.annotation.TargetApi
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Build.VERSION_CODES
import android.os.Handler
import com.dbflow5.adapter2.ModelAdapter
import com.dbflow5.config.DatabaseConfig
import com.dbflow5.config.FlowLog
import com.dbflow5.getNotificationUri
import com.dbflow5.structure.ChangeAction
import com.dbflow5.structure.Model
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
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
open class FlowContentObserver(
    private val contentAuthority: String,
    handler: Handler? = null,
    private val dispatcher: CoroutineDispatcher = handler?.asCoroutineDispatcher()
        ?: Dispatchers.Main,
    private val scope: CoroutineScope = CoroutineScope(dispatcher),
) : ContentObserver(handler) {

    private val registeredTables = mutableSetOf<ModelAdapter<*>>()
    private val notificationUris = hashSetOf<Uri>()
    private val tableUris = hashSetOf<Uri>()

    private val uriDecoder = contentDecoder()

    private var isInTransaction = false
    private var notifyAllUris = false


    private val internalNotificationFlow = MutableStateFlow<ContentNotification<*>?>(null)

    /**
     * Register to hook into the [ContentNotification]. Use [filterIsInstance]
     * to retrieve specific table types.
     */
    val notificationFlow: Flow<ContentNotification<*>>
        get() = internalNotificationFlow.filterNotNull()

    val isSubscribed: Boolean
        get() = registeredTables.isNotEmpty()

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
     * Ends the transaction where it finishes, and will call [.onChange] for
     * every URI called (if set)/
     */
    fun endTransactionAndNotify() {
        if (isInTransaction) {
            isInTransaction = false

            synchronized(notificationUris) {
                for (uri in notificationUris) {
                    onChange(uri)
                }
                notificationUris.clear()
            }
            synchronized(tableUris) {
                for (uri in tableUris) {
                    contentDecoder().decode<Any>(uri)?.let { notification ->
                        scope.launch {
                            internalNotificationFlow.emit(notification)
                        }
                    }
                }
                tableUris.clear()
            }
        }
    }

    /**
     * Registers the observer for model change events for specific class.
     */
    fun registerForContentChanges(
        contentResolver: ContentResolver,
        adapter: ModelAdapter<*>,
    ) {
        val uri = ContentNotification.TableChange(
            dbRepresentable = adapter,
            action = ChangeAction.NONE,
            authority = contentAuthority
        ).uri
        contentResolver.registerContentObserver(
            uri,
            true,
            this,
        )
        REGISTERED_COUNT.incrementAndGet()
        registeredTables.add(adapter)
    }

    /**
     * Unregisters this list for model change events
     */
    fun unregisterForContentChanges(contentResolver: ContentResolver) {
        contentResolver.unregisterContentObserver(this)
        REGISTERED_COUNT.decrementAndGet()
        registeredTables.clear()
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        uri?.let { onChange(it) }
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    private fun onChange(uri: Uri) {
        val notification = uriDecoder.decode<Any>(uri)
        if (notification != null && notification.action != ChangeAction.NONE) {
            // transactions batch the calls into one sequence. Here we queue up
            // uris if in a transaction
            if (!isInTransaction) {
                scope.launch {
                    internalNotificationFlow.emit(notification)
                }
            } else {

                if (!notifyAllUris) {
                    val locUri = getNotificationUri(
                        contentAuthority, notification.dbRepresentable.type,
                        ChangeAction.CHANGE
                    )
                    synchronized(notificationUris) { notificationUris.add(locUri) }
                    synchronized(tableUris) { tableUris.add(locUri) }
                } else {
                    synchronized(notificationUris) { notificationUris.add(uri) }
                    synchronized(tableUris) {
                        tableUris.add(
                            getNotificationUri(
                                contentAuthority,
                                notification.dbRepresentable.type,
                                notification.action
                            )
                        )
                    }
                }
            }
        } else {
            FlowLog.log(
                FlowLog.Level.W,
                "Received URI change for unregistered " +
                    "${notification?.dbRepresentable?.type ?: "uri: $uri"} . URI ignored."
            )
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
