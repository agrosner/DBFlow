package com.raizlabs.android.dbflow.annotation

/**
 * Description: Specifies that a [Column] is not null.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class NotNull(
        /**
         * Defines how to handle conflicts for not null column
         *
         * @return a [com.raizlabs.android.dbflow.annotation.ConflictAction] enum
         */
        val onNullConflict: ConflictAction = ConflictAction.FAIL)
