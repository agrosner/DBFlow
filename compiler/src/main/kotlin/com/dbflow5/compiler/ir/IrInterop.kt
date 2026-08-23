package com.dbflow5.compiler.ir

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.Platforms
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.ClassNameResolver
import com.dbflow5.codegen.shared.interop.ClassType
import com.dbflow5.codegen.shared.interop.Declaration
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.dbflow5.codegen.shared.interop.PropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.ClassId

internal class IrOriginatingSource(
    val classFqName: String,
) : OriginatingSource

internal object IrOriginatingFileTypeSpecAdder : OriginatingFileTypeSpecAdder {
    override fun addOriginatingFile(typeSpec: TypeSpec.Builder, source: OriginatingSource) = Unit
    override fun addOriginatingFile(spec: PropertySpec.Builder, source: OriginatingSource) = Unit
    override fun addOriginatingFile(spec: FunSpec.Builder, source: OriginatingSource) = Unit
}

internal object CompilerPlatforms : Platforms {
    override val currentPlatform: String = Platforms.All
}

internal class IrClassType(
    val type: IrType,
) : ClassType {
    override fun makeNotNullable(): ClassType = IrClassType(type.makeNotNull())
    override val declaration: Declaration
        get() = IrDeclarationAdapter(type.classOrNull?.owner)
    override fun toTypeName(): TypeName = type.toPoetTypeName()
    override val isMarkedNullable: Boolean
        get() = type.isMarkedNullable()
}

internal class IrDeclarationAdapter(
    private val declaration: IrClass?,
) : Declaration {
    override val simpleName: NameModel
        get() = NameModel(
            packageName = declaration?.poetPackageName().orEmpty(),
            shortName = declaration?.name?.asString().orEmpty(),
            nullable = false,
        )
    override val closestClassDeclaration: ClassDeclaration?
        get() = declaration?.let(::IrClassDeclaration)
    override val isValue: Boolean
        get() = declaration?.isValue == true
    override val isAbstract: Boolean
        get() = declaration?.modality == Modality.ABSTRACT
}

internal class IrPropertyDeclaration(
    private val property: IrProperty,
) : PropertyDeclaration {
    override val simpleName: NameModel
        get() = NameModel(
            packageName = (property.parent as? IrClass)?.poetPackageName().orEmpty(),
            shortName = property.name.asString(),
            nullable = typeName.isNullable,
        )
    override val typeName: TypeName
        get() = (property.getter?.returnType ?: property.backingField?.type)
            ?.toPoetTypeName()
            ?: ClassName("kotlin", "Any")
    override val isAbstract: Boolean
        get() = property.modality == Modality.ABSTRACT
}

internal class IrFunctionDeclaration(
    private val function: IrFunction,
) : PropertyDeclaration {
    override val simpleName: NameModel
        get() = NameModel(
            packageName = (function.parent as? IrClass)?.poetPackageName().orEmpty(),
            shortName = function.name.asString(),
            nullable = typeName.isNullable,
        )
    override val typeName: TypeName
        get() = function.returnType.toPoetTypeName()
    override val isAbstract: Boolean
        get() = (function as? IrSimpleFunction)?.modality == Modality.ABSTRACT
}

internal class IrClassDeclaration(
    val irClass: IrClass,
) : ClassDeclaration {
    override val isEnum: Boolean
        get() = irClass.isEnumClass
    override val isInternal: Boolean
        get() = irClass.visibility.name == "internal"
    override val isObject: Boolean
        get() = irClass.isObject
    override val isData: Boolean
        get() = irClass.isData
    override val properties: Sequence<PropertyDeclaration>
        get() = irClass.allProperties().map(::IrPropertyDeclaration)
    override val functions: Sequence<PropertyDeclaration>
        get() = irClass.functions.map(::IrFunctionDeclaration)
    override val containingFile: OriginatingSource
        get() = IrOriginatingSource(irClass.kotlinFqName.asString())
    override val superTypes: Sequence<TypeName>
        get() = irClass.superTypes.asSequence().map { it.toPoetTypeName() }
    override val hasDefaultConstructor: Boolean
        get() {
            val primary = irClass.primaryConstructor
            return primary == null || primary.regularParameters.all { it.hasDefaultValue() }
        }

    override fun asStarProjectedType(): ClassDeclaration = this
}

internal class IrClassNameResolver(
    private val pluginContext: IrPluginContext,
    private val knownClasses: Map<ClassName, IrClass>,
) : ClassNameResolver {
    override fun classDeclarationByClassName(className: ClassName): ClassDeclaration? {
        val irClass = knownClasses[className] ?: resolve(className) ?: return null
        return IrClassDeclaration(irClass)
    }

    override fun classTypeByClassName(className: ClassName): ClassType {
        val irClass = knownClasses[className] ?: resolve(className)
            ?: error("Unable to resolve $className")
        return IrClassType(irClass.defaultType)
    }

    private fun resolve(className: ClassName): IrClass? {
        val classId = ClassId(
            org.jetbrains.kotlin.name.FqName(className.packageName),
            org.jetbrains.kotlin.name.FqName(className.simpleNames.joinToString(".")),
            false,
        )
        return pluginContext.referenceClass(classId)?.owner
    }
}

internal fun IrClass.allProperties(): Sequence<IrProperty> = sequence {
    val seen = mutableSetOf<String>()
    var current: IrClass? = this@allProperties
    while (current != null) {
        current.properties.forEach { property ->
            val name = property.name.asString()
            if (seen.add(name)) yield(property)
        }
        current = current.superClass()
    }
}

internal fun IrClass.superClass(): IrClass? =
    superTypes.firstNotNullOfOrNull { it.classOrNull?.owner }
