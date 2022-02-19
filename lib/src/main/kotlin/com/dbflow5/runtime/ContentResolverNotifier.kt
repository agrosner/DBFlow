package com.dbflow5.runtime

import android.content.ContentResolver
import android.content.ContentResolver.NOTIFY_SYNC_TO_NETWORK
import android.content.Context
import android.os.Build
import com.dbflow5.database.DatabaseWrapper

/**
 * The default use case, it notifies via the [ContentResolver] system.
 *
 * @param [authority] Specify the content URI authority you wish to use here. This will get propagated
 * everywhere that changes get called from in a specific database.
 */
class ContentResolverNotifier(
    private val context: Context,
    private val authority: String,
    override val db: DatabaseWrapper,
) : ModelNotifier {

    override suspend fun <Table : Any> onChange(notification: ModelNotification<Table>) {
        if (FlowContentObserver.shouldNotify()) {
            notifyChanges(notification.toContentNotification(authority))
        }
    }

    private fun <Table : Any> notifyChanges(notification: ContentNotification<Table>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.contentResolver.notifyChange(
                notification.uri, null, NOTIFY_SYNC_TO_NETWORK
            )
        } else {
            @Suppress("DEPRECATION")
            context.contentResolver.notifyChange(
                notification.uri, null, true
            )
        }
    }
}
