package com.dbflow5.codegen.shared.properties

import com.dbflow5.annotation.ConflictAction
import com.squareup.kotlinpoet.TypeName


interface ClassProperties : DatabaseScopedProperties, ReadableScopeProperties {
    val allFields: Boolean
}

/**
 * Used by ManyToMany / OneToMany for bare-bones use of common fields.
 */
data class GeneratedClassProperties(
    override val allFields: Boolean,
    override val database: TypeName,
    override val orderedCursorLookup: Boolean,
    override val assignDefaultValuesFromCursor: Boolean,
    /**
     * Which class it came from.
     */
    val generatedFromClassType: TypeName,
) : ClassProperties

/**
 * Description: Holder for Table Values.
 */
data class TableProperties(
    override val name: String,
    override val database: TypeName,
    override val allFields: Boolean,
    override val orderedCursorLookup: Boolean,
    override val assignDefaultValuesFromCursor: Boolean,
    override val createWithDatabase: Boolean,
    val updateConflict: ConflictAction,
    val insertConflict: ConflictAction,
    val primaryKeyConflict: ConflictAction,
    val temporary: Boolean,
    val indexGroupProperties: List<IndexGroupProperties>,
    val uniqueGroupProperties: List<UniqueGroupProperties>,
) : ClassProperties, CreatableScopeProperties, NamedProperties

/**
 * Description:
 */
data class ViewProperties(
    override val name: String,
    override val database: TypeName,
    override val allFields: Boolean,
    override val orderedCursorLookup: Boolean,
    override val assignDefaultValuesFromCursor: Boolean,
    override val createWithDatabase: Boolean,
) : ClassProperties, CreatableScopeProperties, NamedProperties

/**
 * Description:
 */
data class QueryProperties(
    override val database: TypeName,
    override val allFields: Boolean,
    override val orderedCursorLookup: Boolean,
    override val assignDefaultValuesFromCursor: Boolean,
    override val createWithDatabase: Boolean = false,
) : ClassProperties, CreatableScopeProperties

