package com.dbflow5.content

import com.dbflow5.observing.notifications.ModelNotification

fun <Table : Any> ModelNotification<Table>.toContentNotification(
    authority: String,
): ContentNotification = when (this) {
    is ModelNotification.ModelChange<Table> -> ContentNotification.ModelChange<Table>(
        changedFields = changedFields,
        tableName = adapter.name,
        action = action,
        authority = authority,
    )
    is ModelNotification.TableChange<Table> -> ContentNotification.TableChange(
        tableName = adapter.name,
        action = action,
        authority = authority,
    )
}