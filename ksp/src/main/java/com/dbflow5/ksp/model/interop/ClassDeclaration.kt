package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.dbflow5.codegen.shared.interop.PropertyDeclaration
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

class KSPClassDeclaration(
    val ksClassDeclaration: KSClassDeclaration?
) : ClassDeclaration {
    override val isEnum: Boolean = ksClassDeclaration?.classKind == ClassKind.ENUM_CLASS
    override val isObject: Boolean = ksClassDeclaration?.classKind == ClassKind.OBJECT
    override val isInternal: Boolean = ksClassDeclaration?.isInternal() ?: false
    override val isData: Boolean = ksClassDeclaration?.modifiers?.contains(Modifier.DATA) ?: false
    override val properties: Sequence<PropertyDeclaration>
        get() = ksClassDeclaration?.getAllProperties()
            ?.map { KSPPropertyDeclaration(it) }
            ?.also { props ->
                val propsList = props.toMutableList<PropertyDeclaration>()
                ksClassDeclaration.superTypes.forEach { type ->
                    KSPClassType(type.resolve()).declaration.closestClassDeclaration
                        ?.properties?.onEach { propsList.add(it) }
                }
            }
            ?.asSequence()
            ?: emptySequence()

    override val functions: Sequence<PropertyDeclaration>
        get() = ksClassDeclaration?.getAllFunctions()
            ?.map { KSPFunctionDeclaration(it) }
            ?.also { funs ->
                val functionsList = funs.toMutableList<PropertyDeclaration>()
                ksClassDeclaration.superTypes.forEach { type ->
                    KSPClassType(type.resolve()).declaration.closestClassDeclaration
                        ?.functions?.onEach { functionsList.add(it) }
                }
            }?.asSequence()
            ?: emptySequence()
    override val containingFile: OriginatingSource? =
        ksClassDeclaration?.containingFile?.let { KSPOriginatingSource(it) }

    override val superTypes: Sequence<TypeName>
        get() = ksClassDeclaration?.superTypes?.map { it.resolve().toTypeName() }
            ?: emptySequence()

    override fun asStarProjectedType(): ClassDeclaration {
        return KSPClassDeclaration(ksClassDeclaration?.asStarProjectedType()?.declaration?.closestClassDeclaration())
    }

    override val hasDefaultConstructor: Boolean = ksClassDeclaration?.primaryConstructor != null
}
