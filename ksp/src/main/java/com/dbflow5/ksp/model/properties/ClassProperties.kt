package com.dbflow5.ksp.model.properties

import com.dbflow5.annotation.ConflictAction
import com.squareup.kotlinpoet.TypeName


interface ClassProperties : DatabaseScopedProperties, ReadableScopeProperties {
    val allFields: Boolean
}

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

