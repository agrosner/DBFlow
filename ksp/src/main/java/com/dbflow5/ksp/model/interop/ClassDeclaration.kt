package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.model.interop.ClassDeclaration
import com.dbflow5.codegen.model.interop.OriginatingFileType
import com.dbflow5.codegen.model.interop.PropertyDeclaration
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

class KSPClassDeclaration(
    val ksClassDeclaration: KSClassDeclaration?
) : ClassDeclaration {
    override val isEnum: Boolean = ksClassDeclaration?.classKind == ClassKind.ENUM_CLASS

    override val properties: List<PropertyDeclaration>
        get() = ksClassDeclaration?.getAllProperties()?.map { KSPPropertyDeclaration(it) }
            ?.toList()
            ?: listOf()

    override val containingFile: OriginatingFileType? =
        ksClassDeclaration?.containingFile?.let { KSPOriginatingFile(it) }

    override val superTypes: Sequence<TypeName>
        get() = ksClassDeclaration?.superTypes?.map { it.resolve().toTypeName() }
            ?: emptySequence()

    override fun asStarProjectedType(): ClassDeclaration {
        return KSPClassDeclaration(ksClassDeclaration?.asStarProjectedType()?.declaration?.closestClassDeclaration())
    }
}
