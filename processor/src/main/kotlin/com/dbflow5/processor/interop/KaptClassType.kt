package com.dbflow5.processor.interop

import com.dbflow5.codegen.model.interop.ClassType
import com.dbflow5.codegen.model.interop.Declaration
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.TypeElement

/**
 * Description:
 */
class KaptClassType(
    private val typeElement: TypeElement,
) : ClassType {

    override fun makeNotNullable(): ClassType {
        // nullability not usable in Kapt without meta
        return KaptClassType(typeElement)
    }

    override val declaration: Declaration =
        KaptDeclaration(typeElement)

    override fun toTypeName(): TypeName {
        TODO("Not yet implemented")
    }

    // TODO: use annotation inference to try to gauge.
    override val isMarkedNullable: Boolean = false
}