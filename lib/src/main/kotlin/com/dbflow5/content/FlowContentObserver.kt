package com.dbflow5.content

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.config.FlowLog
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

/**
 * Provides a first-class way to register for model notifications from the [ContentResolverNotifier].
 *
 * Register with the [notificationFlow] to receive updates.
 *
 * You can also [beginTransaction] to batch changes together and then emit them at once
 * by calling [endTransactionAndNotify].
 */
class FlowContentObserver(
    private val contentAuthority: String,
    handler: Handler? = null,
    private val dispatcher: CoroutineDispatcher = handler?.asCoroutineDispatcher()
        ?: Dispatchers.Main,
    private val scope: CoroutineScope = CoroutineScope(dispatcher),
    /**
     * If true, this class will emit all [ChangeAction] events. If false,
     * these are consolidated into a single [ChangeAction.CHANGE] action.
     */
    private val notifyAllUris: Boolean = false,
    /**
     * Defines how uri are decoded.
     */
    private val uriDecoder: ContentNotificationDecoder = defaultContentDecoder(),
    /**
     * Defines how uri are encoded.
     */
    private val uriEncoder: ContentNotificationEncoder = defaultContentEncoder(),
) : ContentObserver(handler) {

    private val transactionMutex = Mutex()

    private val registeredTables = mutableSetOf<ModelAdapter<*>>()
    private val allChanges = hashSetOf<ContentNotification<*>>()
    private val tableChanges = hashSetOf<ContentNotification.TableChange<*>>()

    private val inTransaction = MutableStateFlow(false)
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
     * Starts a transaction where when it is finished, this class will receive a notification of all of the changes by
     * calling [.endTransactionAndNotify]. Note it may lead to unexpected behavior if called from different threads.
     */
    suspend fun beginTransaction() {
        inTransaction.emit(true)
    }

    /**
     * Ends the transaction where it finishes, and will call [.onChange] for
     * every URI called (if set)/
     */
    suspend fun endTransactionAndNotify() {
        if (inTransaction.value) {
            inTransaction.emit(false)
            transactionMutex.withLock {
                for (uri in allChanges) {
                    onChange(uri)
                }
                allChanges.clear()
                for (change in tableChanges) {
                    scope.launch {
                        internalNotificationFlow.emit(change)
                    }
                }
                tableChanges.clear()
            }
        }
    }

    /**
     * Runs operations inside a transaction block. Use this to easily batch changes.
     */
    suspend fun transact(fn: () -> Unit) {
        beginTransaction()
        fn()
        endTransactionAndNotify()
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
        )
        contentResolver.registerContentObserver(
            uriEncoder.encode(uri),
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
        uri?.let { uriDecoder.decode<Any>(uri) }
            ?.let {
                scope.launch { onChange(it) }
            } ?: FlowLog.log(
            FlowLog.Level.W,
            "Received URI change for unregistered uri: $uri:  URI ignored."
        )
    }

    private suspend fun onChange(notification: ContentNotification<*>) {
        if (notification.action != ChangeAction.NONE) {
            // transactions batch the calls into one sequence. Here we queue up
            // uris if in a transaction
            if (!inTransaction.value) {
                scope.launch {
                    internalNotificationFlow.emit(notification)
                }
            } else {
                val updatedNotification = if (!notifyAllUris) {
                    notification.mutate(action = ChangeAction.CHANGE)
                } else {
                    notification
                }
                transactionMutex.withLock {
                    allChanges.add(updatedNotification)
                    tableChanges.add(
                        ContentNotification.TableChange(
                            action = updatedNotification.action,
                            authority = updatedNotification.authority,
                            dbRepresentable = updatedNotification.dbRepresentable,
                        )
                    )
                }
            }
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
