package com.dbflow5.ksp.model.interop

import com.dbflow5.ksp.model.invoke
import com.dbflow5.codegen.model.NameModel
import com.dbflow5.codegen.model.interop.ClassDeclaration
import com.dbflow5.codegen.model.interop.Declaration
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.Modifier

/**
 * Description:
 */
data class KSPDeclaration(
    val declaration: KSDeclaration
) : Declaration {
    override val closestClassDeclaration: ClassDeclaration
        get() = KSPClassDeclaration(declaration.closestClassDeclaration())

    override val simpleName: NameModel = NameModel(
        declaration.simpleName,
        declaration.packageName
    )

    override fun hasValueModifier(): Boolean =
        declaration.modifiers.any { it == Modifier.VALUE }
}
