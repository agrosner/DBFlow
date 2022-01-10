package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.model.interop.ClassType
import com.dbflow5.codegen.model.interop.Declaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Description:
 */
data class KSPClassType(
    val ksType: KSType,
) : ClassType {
    override fun makeNotNullable(): ClassType =
        copy(
            ksType = ksType.makeNotNullable(),
        )

    override val declaration: Declaration
        get() = KSPDeclaration(ksType.declaration)

    override fun toTypeName(): TypeName = ksType.toTypeName()

    override val isMarkedNullable: Boolean = ksType.isMarkedNullable
}
