package com.dbflow5.compiler.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetClass
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val CREATE_DB_PACKAGE = FqName("com.dbflow5.database")
private val CREATE_DB_NAME = Name.identifier("createDB")
private val PLATFORM_SETTINGS_ID = ClassId.topLevel(
    FqName("com.dbflow5.database.config.DBPlatformSettings"),
)
private const val GENERATED_SUFFIX = "_Database"
private const val ANDROID_CONTEXT = "android.content.Context"

/**
 * Rewrites [com.dbflow5.database.createDB] to `{Name}_Database.create(...)`.
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class CreateDbIrTransformer(
    private val pluginContext: IrPluginContext,
) {

    fun transform(element: IrElement) {
        val replacements = mutableMapOf<IrCall, IrExpression>()
        element.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitCall(expression: IrCall) {
                expression.acceptChildrenVoid(this)
                rewrite(expression)?.let { replacements[expression] = it }
            }
        })
        if (replacements.isEmpty()) return
        IrCallReplacer(replacements).replaceIn(element)
    }

    private fun rewrite(expression: IrCall): IrExpression? {
        val callee = expression.symbol.owner
        if (!callee.isCreateDbIntrinsic()) return null

        val databaseClass = expression.databaseClass()
        if (databaseClass == null) {
            pluginContext.messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "createDB requires a concrete @Database type argument.",
            )
            return null
        }

        val generatedId = databaseClass.generatedDatabaseClassId()
        val generatedClass = pluginContext.referenceClass(generatedId)
        if (generatedClass == null) {
            pluginContext.messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "Generated ${generatedId.asFqNameString()} was not on the compilation classpath. " +
                    "Compile DBFlow generated sources with this compilation, then call createDB.",
            )
            return null
        }

        val companion = generatedClass.owner.declarations
            .filterIsInstance<IrClass>()
            .firstOrNull { it.kind == ClassKind.OBJECT && it.name.asString() == "Companion" }
        if (companion == null) {
            pluginContext.messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "${generatedId.asFqNameString()} is missing a companion create() factory.",
            )
            return null
        }

        val createSymbol = pluginContext.referenceFunctions(
            CallableId(
                companion.classId ?: generatedId.createNestedClassId(Name.identifier("Companion")),
                Name.identifier("create"),
            ),
        ).firstOrNull { symbol ->
            val regular = symbol.owner.regularParameters
            regular.size == 2 &&
                regular[0].type.classFqName?.asString() ==
                PLATFORM_SETTINGS_ID.asFqNameString()
        } ?: companion.declarations
            .filterIsInstance<IrSimpleFunction>()
            .firstOrNull { function ->
                function.name.asString() == "create" && function.regularParameters.size == 2
            }?.symbol

        if (createSymbol == null) {
            pluginContext.messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "Could not find ${generatedId.asFqNameString()}.create(platformSettings, settings).",
            )
            return null
        }

        val platformArg = expression.platformSettingsArgument()
        if (platformArg == null) {
            pluginContext.messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "createDB could not build DBPlatformSettings for this overload.",
            )
            return null
        }

        val create = createSymbol.owner
        val replacement = IrCallImpl(
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
            type = expression.type,
            symbol = createSymbol,
            typeArgumentsCount = create.typeParameters.size,
        )
        create.parameters.forEachIndexed { index, parameter ->
            replacement.arguments[index] = when (parameter.kind) {
                IrParameterKind.DispatchReceiver -> IrGetObjectValueImpl(
                    startOffset = expression.startOffset,
                    endOffset = expression.endOffset,
                    type = companion.symbol.defaultType,
                    symbol = companion.symbol,
                )
                IrParameterKind.Regular -> when (parameter.name.asString()) {
                    "platformSettings" -> platformArg
                    "settingsFn", "settings" -> expression.regularArgument("settings")
                    else -> null
                }
                else -> null
            }
        }
        return replacement
    }

    private fun IrCall.platformSettingsArgument(): IrExpression? {
        regularArgument("platformSettings")?.let { return it }
        val contextArg = regularArgument("context")
        val constructor = pluginContext.referenceConstructors(PLATFORM_SETTINGS_ID)
            .firstOrNull { symbol ->
                val regular = symbol.owner.regularParameters
                if (contextArg != null) {
                    regular.any { it.type.classFqName?.asString() == ANDROID_CONTEXT }
                } else {
                    regular.all { it.hasDefaultValue() }
                }
            } ?: return null
        val call = IrConstructorCallImpl(
            startOffset = startOffset,
            endOffset = endOffset,
            type = constructor.owner.returnType,
            symbol = constructor,
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0,
        )
        if (contextArg != null) {
            val contextIndex = constructor.owner.parameters.indexOfFirst {
                it.kind == IrParameterKind.Regular &&
                    it.type.classFqName?.asString() == ANDROID_CONTEXT
            }
            if (contextIndex >= 0) {
                call.arguments[contextIndex] = contextArg
            }
        }
        return call
    }
}

private fun IrSimpleFunction.isCreateDbIntrinsic(): Boolean =
    name == CREATE_DB_NAME && fqNameWhenAvailable?.parent() == CREATE_DB_PACKAGE

private fun IrCall.databaseClass(): IrClass? {
    typeArguments.firstOrNull()?.classOrNull?.owner?.let { return it }
    val typeArg = regularArgument("type") ?: return null
    return when (typeArg) {
        is IrClassReference -> typeArg.classType.classOrNull?.owner
        is IrGetClass -> typeArg.argument.type.classOrNull?.owner
        else -> typeArg.type.classOrNull?.owner
    }
}

private fun IrCall.regularArgument(name: String): IrExpression? {
    val function = symbol.owner
    val index = function.parameters.indexOfFirst {
        it.kind == IrParameterKind.Regular && it.name.asString() == name
    }
    if (index < 0) return null
    return arguments.getOrNull(index)
}

private fun IrClass.generatedDatabaseClassId(): ClassId {
    val id = classId ?: ClassId.topLevel(kotlinFqName)
    return ClassId(id.packageFqName, Name.identifier("${id.shortClassName.asString()}$GENERATED_SUFFIX"))
}

/**
 * Replaces [IrCall] nodes after the collect pass so we do not mutate while walking.
 */
private class IrCallReplacer(
    private val replacements: Map<IrCall, IrExpression>,
) : IrElementTransformerVoid() {
    fun replaceIn(element: IrElement) {
        element.transform(this, null)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val replacement = replacements[expression]
        if (replacement != null) return replacement
        return super.visitCall(expression)
    }
}
