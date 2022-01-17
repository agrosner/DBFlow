package com.dbflow5.processor.interop

import com.dbflow5.codegen.model.interop.ClassType
import com.dbflow5.codegen.model.interop.Declaration
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

/**
 * Description:
 */
data class KaptClassType(
    private val typeMirror: TypeMirror,
    private val element: Element,
) : ClassType {

    override fun makeNotNullable(): ClassType {
        // nullability not usable in Kapt without meta
        return KaptClassType(typeMirror, element)
    }

    override val declaration: Declaration =
        KaptDeclaration(typeMirror, element)

    override fun toTypeName(): TypeName = typeMirror.asTypeName()

    // TODO: use annotation inference to try to gauge.
    override val isMarkedNullable: Boolean = false
}