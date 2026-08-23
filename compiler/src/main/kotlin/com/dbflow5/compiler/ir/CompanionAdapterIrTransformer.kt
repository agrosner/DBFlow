package com.dbflow5.compiler.ir

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.properties.CreatableScopeProperties
import com.dbflow5.compiler.fir.DbFlowClassIds
import com.dbflow5.compiler.fir.DbFlowCompanionKey
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import com.squareup.kotlinpoet.NameAllocator
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.isFileClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

/**
 * Wires plugin-generated companions to extend [ModelAdapterImpl] / [ViewAdapterImpl] /
 * [QueryAdapterImpl] using generated adapter helpers from `*_Adapter.kt`.
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class CompanionAdapterIrTransformer(
    private val pluginContext: IrPluginContext,
    private val moduleFragment: IrModuleFragment,
    private val classModels: List<ClassModel>,
    private val nameAllocator: NameAllocator,
) {
    private val modelsByClassName = classModels.associateBy { it.classType }
    private val topLevelProperties = mutableMapOf<Pair<String, String>, IrProperty>()
    private val topLevelFunctions = mutableMapOf<Pair<String, String>, IrSimpleFunction>()

    init {
        moduleFragment.files.forEach { file ->
            val packageName = file.packageFqName.asString()
            collectTopLevelDeclarations(file, packageName)
        }
    }

    private fun collectTopLevelDeclarations(container: IrDeclarationContainer, packageName: String) {
        container.declarations.forEach { declaration ->
            when (declaration) {
                is IrClass -> if (declaration.isFileClass) {
                    collectTopLevelDeclarations(declaration, packageName)
                }
                is IrProperty ->
                    topLevelProperties[packageName to declaration.name.asString()] = declaration
                is IrSimpleFunction ->
                    topLevelFunctions[packageName to declaration.name.asString()] = declaration
            }
        }
    }

    fun transform(element: IrElement) {
        fun visitModelClass(modelClass: IrClass) {
            if (modelClass.isDbFlowTableViewOrQuery()) {
                val companion = modelClass.companionObject()
                if (companion != null && companion.isDbFlowPluginCompanion()) {
                    wireCompanionAdapter(companion, modelClass)
                }
            }
            modelClass.declarations.forEach { declaration ->
                if (declaration is IrClass) {
                    visitModelClass(declaration)
                }
            }
        }
        moduleFragment.files.forEach { file ->
            file.declarations.forEach { declaration ->
                if (declaration is IrClass) {
                    visitModelClass(declaration)
                }
            }
        }
    }

    private fun wireCompanionAdapter(companion: IrClass, modelClass: IrClass) {
        companion.declarations
            .filterIsInstance<IrConstructor>()
            .forEach(::fillCompanionConstructor)
    }

    private fun IrClass.isDbFlowPluginCompanion(): Boolean {
        val companionOrigin = origin
        return kind == ClassKind.OBJECT &&
            name == SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT &&
            companionOrigin is IrDeclarationOrigin.GeneratedByPlugin &&
            companionOrigin.pluginKey == DbFlowCompanionKey
    }

    private fun IrClass.isDbFlowModelCompanion(): Boolean {
        if (kind != ClassKind.OBJECT || name != SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT) return false
        val owner = parent as? IrClass ?: return false
        return owner.isDbFlowTableViewOrQuery()
    }

    private fun IrClass.isDbFlowTableViewOrQuery(): Boolean =
        hasAnnotation(IrTypeFqNames.Table) ||
            hasAnnotation(IrTypeFqNames.Query) ||
            hasAnnotation(IrTypeFqNames.ModelView)

    private fun fillCompanionConstructor(constructor: IrConstructor) {
        val companion = constructor.parent as? IrClass ?: return
        if (!companion.isDbFlowModelCompanion()) return
        val modelClass = companion.parent as? IrClass ?: return
        val model = modelsByClassName[modelClass.toPoetClassName()] ?: return
        val adapterImplClass = adapterImplClass(model) ?: return
        if (constructor.hasAdapterImplDelegatingCall(adapterImplClass)) return
        val superConstructor = adapterImplClass.primaryConstructor ?: return

        val startOffset = constructor.startOffset
        val endOffset = constructor.endOffset
        val delegatingCall = IrDelegatingConstructorCallImpl(
            startOffset = startOffset,
            endOffset = endOffset,
            type = pluginContext.irBuiltIns.unitType,
            symbol = superConstructor.symbol,
            typeArgumentsCount = 1,
        )
        delegatingCall.typeArguments[0] = modelClass.symbol.defaultType

        val filled = when {
            model.isNormal -> fillModelAdapterImplArguments(model, modelClass, delegatingCall, startOffset, endOffset)
            model.isView -> fillViewAdapterImplArguments(model, modelClass, delegatingCall, startOffset, endOffset)
            model.isQuery -> fillQueryAdapterImplArguments(model, modelClass, delegatingCall, startOffset, endOffset)
            else -> false
        }
        if (!filled) return

        replaceCompanionSupertype(companion, adapterImplClass, modelClass)

        val body = pluginContext.irFactory.createBlockBody(startOffset, endOffset)
        body.statements += delegatingCall
        body.statements += IrInstanceInitializerCallImpl(
            startOffset,
            endOffset,
            companion.symbol,
            pluginContext.irBuiltIns.unitType,
        )
        constructor.body = body
    }

    private fun IrConstructor.hasAdapterImplDelegatingCall(adapterImplClass: IrClass): Boolean {
        val blockBody = body as? IrBlockBody ?: return false
        return blockBody.statements.any { statement ->
            statement is IrDelegatingConstructorCall &&
                statement.symbol.owner.parentAsClass == adapterImplClass
        }
    }

    private fun adapterKey(model: ClassModel): String =
        runCatching { nameAllocator[model.generatedClassName] }.getOrElse {
            nameAllocator.newName(
                suggestion = model.generatedFieldName,
                tag = model.generatedClassName,
            )
        }

    private fun fillModelAdapterImplArguments(
        model: ClassModel,
        modelClass: IrClass,
        call: IrDelegatingConstructorCallImpl,
        startOffset: Int,
        endOffset: Int,
    ): Boolean {
        val adapterKey = adapterKey(model)
        call.arguments[0] = classReference(modelClass, startOffset, endOffset)
        call.arguments[1] = filePropertyReference(
            packageName = model.name.packageName,
            name = "${adapterKey}_companionOps",
            startOffset = startOffset,
            endOffset = endOffset,
        ) ?: return false
        call.arguments[2] = filePropertyReference(
            packageName = model.name.packageName,
            name = "${adapterKey}_propertyGetter",
            startOffset = startOffset,
            endOffset = endOffset,
        ) ?: return false
        call.arguments[3] = stringConstant(model.dbName, startOffset, endOffset)
        call.arguments[4] = functionCall(
            packageName = model.name.packageName,
            name = "${adapterKey}_creationSQL",
            startOffset = startOffset,
            endOffset = endOffset,
        ) ?: return false
        call.arguments[5] = booleanConstant(
            (model.properties as CreatableScopeProperties).createWithDatabase,
            startOffset,
            endOffset,
        )
        call.arguments[6] = filePropertyReference(
            packageName = model.name.packageName,
            name = "${adapterKey}_primaryModelClauseGetter",
            startOffset = startOffset,
            endOffset = endOffset,
        ) ?: return false
        return true
    }

    private fun fillViewAdapterImplArguments(
        model: ClassModel,
        modelClass: IrClass,
        call: IrDelegatingConstructorCallImpl,
        startOffset: Int,
        endOffset: Int,
    ): Boolean {
        val adapterKey = adapterKey(model)
        call.arguments[0] = classReference(modelClass, startOffset, endOffset)
        call.arguments[1] = filePropertyReference(
            packageName = model.name.packageName,
            name = "${adapterKey}_companionOps",
            startOffset = startOffset,
            endOffset = endOffset,
        ) ?: return false
        call.arguments[2] = filePropertyReference(
            packageName = model.name.packageName,
            name = "${adapterKey}_propertyGetter",
            startOffset = startOffset,
            endOffset = endOffset,
        ) ?: return false
        call.arguments[3] = stringConstant(model.dbName, startOffset, endOffset)
        call.arguments[4] = functionCall(
            packageName = model.name.packageName,
            name = "${adapterKey}_creationSQL",
            startOffset = startOffset,
            endOffset = endOffset,
        ) ?: return false
        call.arguments[5] = booleanConstant(
            (model.properties as CreatableScopeProperties).createWithDatabase,
            startOffset,
            endOffset,
        )
        return true
    }

    private fun fillQueryAdapterImplArguments(
        model: ClassModel,
        modelClass: IrClass,
        call: IrDelegatingConstructorCallImpl,
        startOffset: Int,
        endOffset: Int,
    ): Boolean {
        val adapterKey = adapterKey(model)
        call.arguments[0] = classReference(modelClass, startOffset, endOffset)
        call.arguments[1] = filePropertyReference(
            packageName = model.name.packageName,
            name = "${adapterKey}_companionOps",
            startOffset = startOffset,
            endOffset = endOffset,
        ) ?: return false
        return true
    }

    private fun replaceCompanionSupertype(
        companion: IrClass,
        adapterImplClass: IrClass,
        modelClass: IrClass,
    ) {
        val implType = adapterImplClass.symbol.typeWith(modelClass.symbol.defaultType)
        val anyType = pluginContext.irBuiltIns.anyType
        companion.superTypes = buildList {
            add(implType)
            companion.superTypes.forEach { superType ->
                if (superType == anyType) return@forEach
                if (superType.classOrNull?.owner == adapterImplClass) return@forEach
                add(superType)
            }
        }
    }

    private fun adapterImplClass(model: ClassModel): IrClass? {
        val classId = when {
            model.isNormal -> DbFlowClassIds.ModelAdapterImpl
            model.isView -> DbFlowClassIds.ViewAdapterImpl
            else -> DbFlowClassIds.QueryAdapterImpl
        }
        return pluginContext.referenceClass(classId)?.owner
    }

    private fun classReference(
        modelClass: IrClass,
        startOffset: Int,
        endOffset: Int,
    ) = IrClassReferenceImpl(
        startOffset = startOffset,
        endOffset = endOffset,
        type = pluginContext.irBuiltIns.kClassClass.typeWith(modelClass.symbol.defaultType),
        symbol = modelClass.symbol,
        classType = modelClass.symbol.defaultType,
    )

    private fun stringConstant(value: String, startOffset: Int, endOffset: Int) =
        IrConstImpl.string(
            startOffset = startOffset,
            endOffset = endOffset,
            type = pluginContext.irBuiltIns.stringType,
            value = value,
        )

    private fun booleanConstant(value: Boolean, startOffset: Int, endOffset: Int) =
        IrConstImpl.boolean(
            startOffset = startOffset,
            endOffset = endOffset,
            type = pluginContext.irBuiltIns.booleanType,
            value = value,
        )

    private fun filePropertyReference(
        packageName: String,
        name: String,
        startOffset: Int,
        endOffset: Int,
    ): org.jetbrains.kotlin.ir.expressions.IrExpression? {
        val property = topLevelProperties[packageName to name]
            ?: pluginContext.referenceProperties(
                CallableId(ClassId.topLevel(org.jetbrains.kotlin.name.FqName(packageName)), Name.identifier(name)),
            ).firstOrNull()?.owner
            ?: return null
        val getter = property.getter ?: return null
        return org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl(
            startOffset = startOffset,
            endOffset = endOffset,
            type = getter.returnType,
            symbol = getter.symbol,
            typeArgumentsCount = getter.typeParameters.size,
        )
    }

    private fun functionCall(
        packageName: String,
        name: String,
        startOffset: Int,
        endOffset: Int,
    ): org.jetbrains.kotlin.ir.expressions.IrExpression? {
        val function = topLevelFunctions[packageName to name]
            ?: pluginContext.referenceFunctions(
                CallableId(ClassId.topLevel(org.jetbrains.kotlin.name.FqName(packageName)), Name.identifier(name)),
            ).firstOrNull()?.owner
            ?: return null
        return org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl(
            startOffset = startOffset,
            endOffset = endOffset,
            type = function.returnType,
            symbol = function.symbol,
            typeArgumentsCount = function.typeParameters.size,
        )
    }
}
