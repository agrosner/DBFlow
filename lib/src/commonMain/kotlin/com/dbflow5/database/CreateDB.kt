package com.dbflow5.database

import com.dbflow5.database.config.DBPlatformSettings
import com.dbflow5.database.config.DBSettings
import kotlin.reflect.KClass

/**
 * Opens [DB]. The compiler plugin rewrites this to the generated `{Name}_Database.create` call.
 */
@Suppress("UNUSED_PARAMETER")
inline fun <reified DB : DBFlowDatabase<DB>> createDB(
    platformSettings: DBPlatformSettings,
    noinline settings: DBSettings.() -> DBSettings = { this },
): DB {
    throw createDbNotRewritten(DB::class)
}

/**
 * Non-reified entry the compiler plugin replaces. Prefer the reified overload.
 */
@Suppress("UNUSED_PARAMETER")
fun <DB : DBFlowDatabase<DB>> createDB(
    type: KClass<DB>,
    platformSettings: DBPlatformSettings,
    settings: DBSettings.() -> DBSettings = { this },
): DB {
    throw createDbNotRewritten(type)
}

@PublishedApi
internal fun createDbNotRewritten(type: KClass<*>): Nothing =
    throw IllegalStateException(
        "createDB(${type.simpleName}) was not rewritten by the DBFlow compiler plugin. " +
            "Apply the com.dbflow5 Gradle plugin."
    )
