package com.dbflow5.model.interop

/**
 * Description:
 */
interface ClassDeclaration {

    val isEnum: Boolean

    val properties: List<PropertyDeclaration>
}