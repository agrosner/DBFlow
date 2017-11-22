package com.raizlabs.android.dbflow.annotation

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
        /**
         * @return When true, we simple do {columnName} &gt; 0 when checking for it's existence if [.autoincrement]
         * is true. If not, we do a full database SELECT exists.
         */
        val quickCheckAutoIncrement: Boolean = false)
