package com.dbflow5.database

import com.dbflow5.database.config.DBSettings

@Suppress("UNUSED_PARAMETER")
inline fun <reified DB : DBFlowDatabase<DB>> createDB(
    noinline settings: DBSettings.() -> DBSettings = { this },
): DB {
    throw createDbNotRewritten(DB::class)
}
