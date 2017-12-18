package com.raizlabs.dbflow5.annotation

/**
 * Description: Used inside of [ForeignKey.references], describes the
 * local column name, type, and referencing table column name.
 *
 *
 * Note: the type of the local column must match the
 * column type of the referenced column. By using a field as a Model object,
 * you will need to ensure the same types are used.
 */
@Retention(AnnotationRetention.SOURCE)
annotation class ForeignKeyReference(
        /**
         * @return The local column name that will be referenced in the DB
         */
        val columnName: String,
        /**
         * @return The column name in the referenced table
         */
        val foreignKeyColumnName: String,
        /**
         * @return The default value for the reference column. Same as [Column.defaultValue]
         */
        val defaultValue: String = "",

        /**
         * @return Specify the [NotNull] annotation here and it will get pasted into the reference definition.
         */
        val notNull: NotNull = NotNull(onNullConflict = ConflictAction.NONE))
