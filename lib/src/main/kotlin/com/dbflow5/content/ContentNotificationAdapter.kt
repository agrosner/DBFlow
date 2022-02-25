package com.dbflow5.content

import com.dbflow5.runtime.ModelNotification

fun <Table : Any> ModelNotification<Table>.toContentNotification(
    authority: String,
): ContentNotification<Table> = when (this) {
    is ModelNotification.ModelChange<Table> -> ContentNotification.ModelChange(
        changedFields = changedFields,
        dbRepresentable = adapter,
        action = action,
        authority = authority,
    )
    is ModelNotification.TableChange<Table> -> ContentNotification.TableChange(
        dbRepresentable = adapter,
        action = action,
        authority = authority,
    )
}