package com.raizlabs.dbflow5.annotation

import com.raizlabs.dbflow5.sql.ConflictAction

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
