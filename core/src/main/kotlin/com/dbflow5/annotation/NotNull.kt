package com.dbflow5.annotation

import com.dbflow5.annotation.ConflictAction

/**
 * Description: Specifies that a [Column] is not null.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class NotNull(
        /**
         * Defines how to handle conflicts for not null column
         *
         * @return a [com.dbflow5.annotation.ConflictAction] enum
         */
        val onNullConflict: ConflictAction = ConflictAction.FAIL)
