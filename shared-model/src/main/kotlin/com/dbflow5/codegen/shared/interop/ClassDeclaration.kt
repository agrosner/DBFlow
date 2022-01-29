package com.dbflow5.codegen.shared.interop

import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
interface ClassDeclaration {

    val isEnum: Boolean

    val isInternal: Boolean

    val isObject: Boolean

    /**
     * Returns all members including inherited.
     */
    val properties: Sequence<PropertyDeclaration>

    val containingFile: OriginatingSource?

    fun asStarProjectedType(): ClassDeclaration

    /**
     * Run through each super types to do something.
     *
     * This is necessary due to how KAPT vs KSP work.
     */
    val superTypes: Sequence<TypeName>
}
