package com.dbflow5.annotation

import com.dbflow5.annotation.VirtualTable.Type
import kotlin.reflect.KClass

/**
 * Description: Creates a class using the SQLITE FTS4 [https://www.sqlite.org/fts3.html]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class VirtualTable(
    /**
     * @return The name of this FTS4 virtual table. Default is the class name.
     */
    val name: String = "",
    /**
     * @return Specify the class of the database to use.
     */
    val database: KClass<*>,
    /**
     * @return If true, all accessible, non-static, and non-final fields are treated as valid fields.
     * @see Table.allFields
     */
    val allFields: Boolean = true,

    /**
     * The type of the virtual table.
     */
    val type: Type,

    /**
     * @return When false, this table gets generated and associated with database, however it will not immediately
     * get created upon startup. This is useful for keeping around legacy tables for migrations.
     */
    val createWithDatabase: Boolean = true,

    /**
     * @return If true, we throw away checks for column indexing and simply assume that the cursor returns
     * all our columns in order. This may provide a slight performance boost.
     */
    val orderedCursorLookUp: Boolean = false,
    /**
     * @return When true, we reassign the corresponding Model's fields to default values when loading
     * from cursor. If false, we assign values only if present in Cursor.
     */
    val assignDefaultValuesFromCursor: Boolean = true,

    /**
     * Optionally points to a content table when [Type.FTS4] that fills the VIRTUAL TABLE with content.
     * The content option allows FTS4 to forego storing the text being indexed.
     */
    val contentTable: KClass<*> = Any::class
) {
    /**
     * Represents a type of virtual table to create. The choices are purposely limited and more
     * may be available in subsequent releases.
     */
    enum class Type {
        FTS4
    }
}