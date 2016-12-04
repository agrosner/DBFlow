package com.raizlabs.android.dbflow.test

import android.accounts.Account
import android.content.ContentProvider
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.content.ContentValues
import android.content.OperationApplicationException
import android.content.PeriodicSync
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle

import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.annotation.Resetter
import org.robolectric.fakes.BaseCursor
import org.robolectric.manifest.AndroidManifest
import org.robolectric.manifest.ContentProviderData
import org.robolectric.util.NamedStream

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.Objects
import java.util.concurrent.CopyOnWriteArraySet

import org.robolectric.Shadows.shadowOf

/**
 * Shadow for [android.content.ContentResolver].
 */
@Implements(ContentResolver::class)
class ShadowContentResolver2 {
    private var nextDatabaseIdForInserts: Int = 0
    private var nextDatabaseIdForUpdates: Int = 0

    @RealObject var realContentResolver: ContentResolver? = null

    private var cursor: BaseCursor? = null
    private val insertStatements = ArrayList<InsertStatement>()
    private val updateStatements = ArrayList<UpdateStatement>()
    private val deleteStatements = ArrayList<DeleteStatement>()
    private val notifiedUris = ArrayList<NotifiedUri>()
    private val uriCursorMap = HashMap<Uri, BaseCursor>()
    private val inputStreamMap = HashMap<Uri, InputStream>()
    private val contentProviderOperations = HashMap<String, ArrayList<ContentProviderOperation>>()
    private var contentProviderResults: Array<ContentProviderResult>? = null

    private val contentObservers = HashMap<Uri, CopyOnWriteArraySet<ContentObserver>>()

    class NotifiedUri(val uri: Uri, val observer: ContentObserver, val syncToNetwork: Boolean)

    class Status {
        var syncRequests: Int = 0
        var state = -1
        var syncAutomatically: Boolean = false
        var syncExtras: Bundle? = null
        var syncs: MutableList<PeriodicSync>? = ArrayList()
    }

    fun registerInputStream(uri: Uri, inputStream: InputStream) {
        inputStreamMap.put(uri, inputStream)
    }

    @Implementation
    fun openInputStream(uri: Uri): InputStream {
        val inputStream = inputStreamMap[uri]
        if (inputStream != null) {
            return inputStream
        } else {
            return UnregisteredInputStream(uri)
        }
    }

    @Implementation
    fun openOutputStream(uri: Uri): OutputStream {
        return object : OutputStream() {

            @Throws(IOException::class)
            override fun write(arg0: Int) {
            }

            override fun toString(): String {
                return "outputstream for " + uri
            }
        }
    }

    @Implementation
    fun insert(url: Uri, values: ContentValues): Uri {
        val provider = getProvider(url)
        if (provider != null) {
            return provider.insert(url, values)
        } else {
            val insertStatement = InsertStatement(url, ContentValues(values))
            insertStatements.add(insertStatement)
            return Uri.parse(url.toString() + "/" + ++nextDatabaseIdForInserts)
        }
    }

    @Implementation
    fun update(uri: Uri, values: ContentValues, where: String, selectionArgs: Array<String>): Int {
        val provider = getProvider(uri)
        if (provider != null) {
            return provider.update(uri, values, where, selectionArgs)
        } else {
            val updateStatement = UpdateStatement(uri, ContentValues(values), where, selectionArgs)
            updateStatements.add(updateStatement)
            return ++nextDatabaseIdForUpdates
        }
    }

    @Implementation
    fun query(uri: Uri, projection: Array<String>, selection: String,
              selectionArgs: Array<String>, sortOrder: String): Cursor? {
        val provider = getProvider(uri)
        if (provider != null) {
            return provider.query(uri, projection, selection, selectionArgs, sortOrder)
        } else {
            val returnCursor = getCursor(uri) ?: return null

            returnCursor.setQuery(uri, projection, selection, selectionArgs,
                    sortOrder)
            return returnCursor
        }
    }

    @Implementation
    fun getType(uri: Uri): String? {
        val provider = getProvider(uri)
        if (provider != null) {
            return provider.getType(uri)
        } else {
            return null
        }
    }

    @Implementation
    fun call(uri: Uri, method: String, arg: String, extras: Bundle): Bundle? {
        val cp = getProvider(uri)
        if (cp != null) {
            return cp.call(method, arg, extras)
        } else {
            return null
        }
    }

    @Implementation
    fun delete(url: Uri, where: String, selectionArgs: Array<String>): Int {
        val provider = getProvider(url)
        if (provider != null) {
            return provider.delete(url, where, selectionArgs)
        } else {
            val deleteStatement = DeleteStatement(url, where, selectionArgs)
            deleteStatements.add(deleteStatement)
            return 1
        }
    }

    @Implementation
    fun bulkInsert(url: Uri, values: Array<ContentValues>): Int {
        val provider = getProvider(url)
        if (provider != null) {
            return provider.bulkInsert(url, values)
        } else {
            return 0
        }
    }

    @Implementation
    @JvmOverloads fun notifyChange(uri: Uri, observer: ContentObserver?, syncToNetwork: Boolean = false) {
        notifiedUris.add(NotifiedUri(uri, observer, syncToNetwork))

        var observers: CopyOnWriteArraySet<ContentObserver>? = contentObservers[uri]
        if (observers == null) {
            observers = contentObservers[Uri.parse(uri.scheme + "://" + uri.encodedAuthority)]
        }
        if (observers != null) {
            for (obs in observers) {
                if (obs != null && obs !== observer) {
                    obs.dispatchChange(false, uri)
                }
            }
        }
        if (observer != null && observer.deliverSelfNotifications()) {
            observer.dispatchChange(true, uri)
        }
    }

    @Implementation
    @Throws(OperationApplicationException::class)
    fun applyBatch(authority: String, operations: ArrayList<ContentProviderOperation>): Array<ContentProviderResult> {
        val provider = getProvider(authority)
        if (provider != null) {
            return provider.applyBatch(operations)
        } else {
            contentProviderOperations.put(authority, operations)
            return contentProviderResults
        }
    }

    fun setCursor(cursor: BaseCursor) {
        this.cursor = cursor
    }

    fun setCursor(uri: Uri, cursorForUri: BaseCursor) {
        this.uriCursorMap.put(uri, cursorForUri)
    }

    fun setNextDatabaseIdForInserts(nextId: Int) {
        nextDatabaseIdForInserts = nextId
    }

    fun setNextDatabaseIdForUpdates(nextId: Int) {
        nextDatabaseIdForUpdates = nextId
    }

    fun getInsertStatements(): List<InsertStatement> {
        return insertStatements
    }

    fun getUpdateStatements(): List<UpdateStatement> {
        return updateStatements
    }

    val deletedUris: List<Uri>
        get() {
            val uris = ArrayList<Uri>()
            for (deleteStatement in deleteStatements) {
                uris.add(deleteStatement.uri)
            }
            return uris
        }

    fun getDeleteStatements(): List<DeleteStatement> {
        return deleteStatements
    }

    fun getNotifiedUris(): List<NotifiedUri> {
        return notifiedUris
    }

    fun getContentProviderOperations(authority: String): ArrayList<ContentProviderOperation> {
        val operations = contentProviderOperations[authority] ?: return ArrayList()
        return operations
    }

    fun setContentProviderResult(contentProviderResults: Array<ContentProviderResult>) {
        this.contentProviderResults = contentProviderResults
    }

    @Implementation
    fun registerContentObserver(uri: Uri, notifyForDescendents: Boolean, observer: ContentObserver) {
        var observers: CopyOnWriteArraySet<ContentObserver>? = contentObservers[uri]
        if (observers == null) {
            observers = CopyOnWriteArraySet<ContentObserver>()
            contentObservers.put(uri, observers)
        }
        observers.add(observer)
    }

    @Implementation
    fun registerContentObserver(uri: Uri, notifyForDescendents: Boolean, observer: ContentObserver, userHandle: Int) {
        registerContentObserver(uri, notifyForDescendents, observer)
    }

    @Implementation
    fun unregisterContentObserver(observer: ContentObserver?) {
        if (observer != null) {
            for (observers in contentObservers.values) {
                observers.remove(observer)
            }
        }
    }

    /**
     * Non-Android accessor.  Clears the list of registered content observers.
     * Commonly used in test case setup.
     */
    fun clearContentObservers() {
        contentObservers.clear()
    }

    /**
     * Non-Android accessor.  Returns one (which one is unspecified) of the content observers registered with
     * the given URI, or null if none is registered.

     * @param uri Given URI
     * *
     * @return The content observer
     * *
     */
    @Deprecated("")
    @Deprecated("This method return random observer, {@link #getContentObservers} should be used instead.")
    fun getContentObserver(uri: Uri): ContentObserver? {
        val observers = getContentObservers(uri)
        return if (observers.isEmpty()) null else observers.iterator().next()
    }


    /**
     * Non-Android accessor. Returns the content observers registered with
     * the given URI, will be empty if no observer is registered.

     * @param uri Given URI
     * *
     * @return The content observers
     */
    fun getContentObservers(uri: Uri): Collection<ContentObserver> {
        val observers = contentObservers[uri]
        return observers ?: emptyList<ContentObserver>()
    }

    private fun getCursor(uri: Uri): BaseCursor? {
        if (uriCursorMap[uri] != null) {
            return uriCursorMap[uri]
        } else if (cursor != null) {
            return cursor
        } else {
            return null
        }
    }

    class InsertStatement(val uri: Uri, val contentValues: ContentValues)

    class UpdateStatement(val uri: Uri, val contentValues: ContentValues, val where: String, val selectionArgs: Array<String>)

    class DeleteStatement(val uri: Uri, val where: String, val selectionArgs: Array<String>)

    private class UnregisteredInputStream(private val uri: Uri) : InputStream(), NamedStream {

        @Throws(IOException::class)
        override fun read(): Int {
            throw UnsupportedOperationException("You must use ShadowContentResolver.registerInputStream() in order to call read()")
        }

        override fun toString(): String {
            return "stream for " + uri
        }
    }

    companion object {

        private val syncableAccounts = HashMap<String, Map<Account, Status>>()
        private val providers = HashMap<String, ContentProvider>()
        var masterSyncAutomatically: Boolean = false

        @Resetter
        fun reset() {
            syncableAccounts.clear()
            providers.clear()
            masterSyncAutomatically = false
        }

        @Implementation
        fun requestSync(account: Account, authority: String, extras: Bundle) {
            validateSyncExtrasBundle(extras)
            val status = getStatus(account, authority, true)
            status.syncRequests++
            status.syncExtras = extras
        }

        @Implementation
        fun cancelSync(account: Account, authority: String) {
            val status = getStatus(account, authority)
            if (status != null) {
                status.syncRequests = 0
                if (status.syncExtras != null) {
                    status.syncExtras!!.clear()
                }
                // This may be too much, as the above should be sufficient.
                if (status.syncs != null) {
                    status.syncs!!.clear()
                }
            }
        }

        @Implementation
        fun isSyncActive(account: Account, authority: String): Boolean {
            val status = getStatus(account, authority)
            // TODO: this means a sync is *perpetually* active after one request
            return status != null && status.syncRequests > 0
        }

        @Implementation
        fun setIsSyncable(account: Account, authority: String, syncable: Int) {
            getStatus(account, authority, true).state = syncable
        }

        @Implementation
        fun getIsSyncable(account: Account, authority: String): Int {
            return getStatus(account, authority, true).state
        }

        @Implementation
        fun getSyncAutomatically(account: Account, authority: String): Boolean {
            return getStatus(account, authority, true).syncAutomatically
        }

        @Implementation
        fun setSyncAutomatically(account: Account, authority: String, sync: Boolean) {
            getStatus(account, authority, true).syncAutomatically = sync
        }

        @Implementation
        fun addPeriodicSync(account: Account, authority: String, extras: Bundle,
                            pollFrequency: Long) {
            validateSyncExtrasBundle(extras)
            removePeriodicSync(account, authority, extras)
            getStatus(account, authority, true).syncs!!.add(PeriodicSync(account, authority, extras, pollFrequency))
        }

        @Implementation
        fun removePeriodicSync(account: Account, authority: String, extras: Bundle) {
            validateSyncExtrasBundle(extras)
            val status = getStatus(account, authority)
            if (status != null) {
                for (i in status.syncs!!.indices) {
                    if (extras == status.syncs!![i].extras) {
                        status.syncs!!.removeAt(i)
                        break
                    }
                }
            }
        }

        @Implementation
        fun getPeriodicSyncs(account: Account, authority: String): List<PeriodicSync> {
            return getStatus(account, authority, true).syncs
        }

        @Implementation
        fun validateSyncExtrasBundle(extras: Bundle) {
            for (key in extras.keySet()) {
                val value = extras.get(key) ?: continue
                if (value is Long) {
                    continue
                }
                if (value is Int) {
                    continue
                }
                if (value is Boolean) {
                    continue
                }
                if (value is Float) {
                    continue
                }
                if (value is Double) {
                    continue
                }
                if (value is String) {
                    continue
                }
                if (value is Account) {
                    continue
                }
                throw IllegalArgumentException("unexpected value type: " + value.javaClass.name)
            }
        }

        fun getProvider(uri: Uri?): ContentProvider? {
            if (uri == null || ContentResolver.SCHEME_CONTENT != uri.scheme) {
                return null
            }
            return getProvider(uri.authority)
        }

        private fun getProvider(authority: String): ContentProvider? {
            if (!providers.containsKey(authority)) {
                val manifest = shadowOf(RuntimeEnvironment.application).appManifest
                if (manifest != null) {
                    for (providerData in manifest.contentProviders) {
                        if (providerData.authority == authority) {
                            providers.put(providerData.authority, createAndInitialize(providerData))
                        }
                    }
                }
            }
            return providers[authority]
        }

        fun registerProvider(authority: String, provider: ContentProvider) {
            providers.put(authority, provider)
        }

        @JvmOverloads fun getStatus(account: Account, authority: String, create: Boolean = false): Status {
            var map: MutableMap<Account, Status>? = syncableAccounts[authority]
            if (map == null) {
                map = HashMap<Account, Status>()
                syncableAccounts.put(authority, map)
            }
            var status: Status? = map[account]
            if (status == null && create) {
                status = Status()
                map.put(account, status)
            }
            return status
        }

        private fun createAndInitialize(providerData: ContentProviderData): ContentProvider {
            try {
                val provider = Class.forName(providerData.className).newInstance() as ContentProvider
                provider.onCreate()
                return provider
            } catch (e: InstantiationException) {
                throw RuntimeException("Error instantiating class " + providerData.className)
            } catch (e: ClassNotFoundException) {
                throw RuntimeException("Error instantiating class " + providerData.className)
            } catch (e: IllegalAccessException) {
                throw RuntimeException("Error instantiating class " + providerData.className)
            }

        }
    }
}
