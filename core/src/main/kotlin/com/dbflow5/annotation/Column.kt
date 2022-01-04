package com.dbflow5.annotation

import com.dbflow5.converter.TypeConverter
import kotlin.reflect.KClass

/**
 * Description: Marks a field as corresponding to a column in the DB.
 * When adding new columns or changing names, you need to define a new [Migration].
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class Column(
    /**
     * @return The name of the column. The default is the field name.
     */
    val name: String = "",
    /**
     * @return An optional column length
     */
    val length: Int = -1,
    /**
     * @return Marks the field as having a specified collation to use in it's creation.
     */
    val collate: Collate = Collate.NONE,
    /**
     * @return Adds a default value for this column when saving. This is a string representation
     * of the value.
     * Note this will place it in when saving
     * to the DB because we cannot know the intention of missing data from a query.
     */
    val defaultValue: String = "",
    /**
     * @return If private, by default this is get{Name}() for "name".
     * To define a custom one, this method specifies the name
     * of the method only, and not any specific params.
     * So for "getAnotherName()" you would use "AnotherName" as the param.
     * Not used in KSP.
     */
    @DBFlowKAPTOnly
    @Deprecated("Use property beans.")
    val getterName: String = "",
    /**
     * @return If private, by default this is set{Name}() for "name".
     * To define a custom one, this method specifies the name
     * of the method only, and not any specific params.
     * So for "setAnotherName(String name)" you would use "AnotherName" as the param.
     * The params must align exactly to an expected setter, otherwise a compile error ensues.
     * Not used in KSP.
     */
    @DBFlowKAPTOnly
    @Deprecated("Use property beans.")
    val setterName: String = "",
    /**
     * @return A custom type converter that's only used for this field. It will be created and used in
     * the Adapter associated with this table.
     */
    val typeConverter: KClass<out TypeConverter<*, *>> = TypeConverter::class
)
