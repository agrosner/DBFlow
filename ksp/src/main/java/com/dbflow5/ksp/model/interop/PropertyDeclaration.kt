package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.model.NameModel
import com.dbflow5.codegen.model.interop.PropertyDeclaration
import com.dbflow5.ksp.model.invoke
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Description:
 */
data class KSPPropertyDeclaration(
    val property: KSPropertyDeclaration
) : PropertyDeclaration {
    override val typeName: TypeName = property.type.toTypeName()
    override val simpleName: NameModel = NameModel(
        property.simpleName,
        property.packageName,
    )
}
