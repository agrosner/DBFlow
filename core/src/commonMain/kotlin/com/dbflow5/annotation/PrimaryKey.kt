package com.dbflow5.annotation

/**
 * Description:
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class PrimaryKey(
    /**
     * Specifies if the column is autoincrementing or not
     */
    val autoincrement: Boolean = false,
    /**
     * Specifies the column to be treated as a ROWID but is not an [.autoincrement]. This
     * overrides [.autoincrement] and is mutually exclusive.
     */
    val rowID: Boolean = false,
)
