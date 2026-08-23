package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.interop.PropertyDeclaration
import com.dbflow5.ksp.model.invoke
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
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
        nullable = typeName.isNullable,
    )
    override val isAbstract: Boolean = property.isAbstract()
}


data class KSPFunctionDeclaration(
    val func: KSFunctionDeclaration,
) : PropertyDeclaration {
    override val typeName: TypeName = func.returnType!!.toTypeName()
    override val simpleName: NameModel = NameModel(
        func.simpleName,
        func.packageName,
        nullable = typeName.isNullable,
    )

    override val isAbstract: Boolean = func.modifiers.contains(Modifier.ABSTRACT)
}
