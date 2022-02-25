package com.dbflow5.content

import android.content.ContentResolver
import android.content.ContentResolver.NOTIFY_SYNC_TO_NETWORK
import android.content.Context
import android.os.Build
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.runtime.ModelNotification
import com.dbflow5.runtime.ModelNotifier

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
    private val uriEncoder: ContentNotificationEncoder = defaultContentEncoder(),
) : ModelNotifier {

    override suspend fun <Table : Any> onChange(notification: ModelNotification<Table>) {
        if (FlowContentObserver.shouldNotify()) {
            notifyChanges(notification.toContentNotification(authority))
        }
    }

    private fun notifyChanges(notification: ContentNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.contentResolver.notifyChange(
                uriEncoder.encode(notification), null, NOTIFY_SYNC_TO_NETWORK
            )
        } else {
            @Suppress("DEPRECATION")
            context.contentResolver.notifyChange(
                uriEncoder.encode(notification), null, true
            )
        }
    }
}
