package com.dbflow5.annotation

import com.dbflow5.converter.TypeConverter
import kotlin.reflect.KClass

/**
 * Description: Allows a [ColumnMap] to specify a reference override for its fields. Anything not
 * defined here will not be used.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS)
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
         * @return A custom type converter that's only used for this field. It will be created and used in
         * the Adapter associated with this table.
         */
        val typeConverter: KClass<out TypeConverter<*, *>> = TypeConverter::class,

        /**
         * @return Specify the [NotNull] annotation here and it will get pasted into the reference definition.
         */
        val notNull: NotNull = NotNull(onNullConflict = ConflictAction.NONE))
