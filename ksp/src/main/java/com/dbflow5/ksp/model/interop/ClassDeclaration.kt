package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.model.interop.ClassDeclaration
import com.dbflow5.codegen.model.interop.PropertyDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration

class KSPClassDeclaration(
    val ksClassDeclaration: KSClassDeclaration?
) : ClassDeclaration {
    override val isEnum: Boolean = ksClassDeclaration?.classKind == ClassKind.ENUM_CLASS

    override val properties: List<PropertyDeclaration>
        get() = ksClassDeclaration?.getAllProperties()?.map { KSPPropertyDeclaration(it) }
            ?.toList()
            ?: listOf()
}
