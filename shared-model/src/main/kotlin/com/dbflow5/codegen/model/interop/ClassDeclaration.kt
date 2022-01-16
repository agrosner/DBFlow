package com.dbflow5.codegen.model.interop

import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
interface ClassDeclaration {

    val isEnum: Boolean

    /**
     * Returns non-inherited members.
     */
    val properties: List<PropertyDeclaration>

    val containingFile: OriginatingFileType?

    fun asStarProjectedType(): ClassDeclaration

    /**
     * Run through each super types to do something.
     *
     * This is necessary due to how KAPT vs KSP work.
     */
    val superTypes: Sequence<TypeName>
}
