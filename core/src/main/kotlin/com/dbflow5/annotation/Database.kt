package com.dbflow5.annotation

import kotlin.reflect.KClass

/**
 * Description: Creates a new database to use in the application.
 *
 *
 * If we specify one DB, then all models do not need to specify a DB. As soon as we specify two, then each
 * model needs to define what DB it points to.
 *
 *
 *
 * Models will specify which DB it belongs to,
 * but they currently can only belong to one DB.
 *
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class Database(
    /**
     * @return The current version of the DB. Increment it to trigger a DB update.
     */
    val version: Int,
    /**
     * @return If true, SQLite will throw exceptions when [ForeignKey] constraints are not respected.
     * Default is false and will not throw exceptions.
     */
    val foreignKeyConstraintsEnforced: Boolean = false,
    /**
     * @return Checks for consistency in the DB, if true it will recopy over the prepackage database.
     */
    val consistencyCheckEnabled: Boolean = false,
    /**
     * @return Keeps a backup for whenever the database integrity fails a "PRAGMA quick_check(1)" that will
     * replace the corrupted DB
     */
    val backupEnabled: Boolean = false,
    /**
     * @return Global default insert conflict that can be applied to any table when it leaves
     * its [ConflictAction] as NONE.
     */
    val insertConflict: ConflictAction = ConflictAction.NONE,
    /**
     * @return Global update conflict that can be applied to any table when it leaves its
     * [ConflictAction] as NONE
     */
    val updateConflict: ConflictAction = ConflictAction.NONE,

    @property:DBFlowKSPOnly
    val tables: Array<KClass<*>> = [],

    @property:DBFlowKSPOnly
    val views: Array<KClass<*>> = [],

    @property:DBFlowKSPOnly
    val queries: Array<KClass<*>> = [],

    @property:DBFlowKSPOnly
    val migrations: Array<KClass<*>> = [],
)
