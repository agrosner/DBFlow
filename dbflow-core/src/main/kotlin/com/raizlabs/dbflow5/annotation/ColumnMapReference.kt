package com.raizlabs.dbflow5.annotation

/**
 * Description: Allows a [ColumnMap] to specify a reference override for its fields. Anything not
 * defined here will not be used.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD)
annotation class ColumnMapReference(
    /**
     * @return The local column name that will be referenced in the DB
     */
    val columnName: String,
    /**
     * @return The column name in the referenced table
     */
    val columnMapFieldName: String,
    /**
     * @return The default value for the reference column. Same as [Column.defaultValue]
     */
    val defaultValue: String = "",
    /**
     * @return Specify the [NotNull] annotation here and it will get pasted into the reference definition.
     */
    val notNull: NotNull = NotNull(onNullConflict = ConflictAction.NONE))
