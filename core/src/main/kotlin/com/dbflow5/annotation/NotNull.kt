package com.dbflow5.annotation

/**
 * Description: Specifies that a [Column] is not null.
 * Note this simply inserts the constraint into SQLite statements and does not enforce the column's
 * model field nullability, which is separate.
 *
 * In the case of a [ForeignKeyReference], this will make all references [NotNull] based on the
 * [ConflictAction] specified. If you need one to support null, then you need to be explicit
 * in the [ForeignKeyReference].
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
