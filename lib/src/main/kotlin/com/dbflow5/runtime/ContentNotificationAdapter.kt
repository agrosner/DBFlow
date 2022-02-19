package com.dbflow5.runtime

import com.dbflow5.adapter.makeLazySQLObjectAdapter


fun <Table : Any> ModelNotification<Table>.toContentNotification(
    authority: String,
): ContentNotification<Table> = when (this) {
    is ModelNotification.ModelChange<Table> -> ContentNotification.ModelChange(
        changedFields = changedFields,
        adapter = makeLazySQLObjectAdapter(table),
        action = action,
        authority = authority,
    )
    is ModelNotification.TableChange<Table> -> ContentNotification.TableChange(
        table = table,
        action = action,
        authority = authority,
    )
}