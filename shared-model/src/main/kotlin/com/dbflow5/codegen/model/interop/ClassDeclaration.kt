package com.dbflow5.codegen.model.interop

/**
 * Description:
 */
interface ClassDeclaration {

    val isEnum: Boolean

    val properties: List<PropertyDeclaration>
}