package com.dbflow5.compiler.ir

import com.dbflow5.compiler.fir.DbFlowColumnPropertyKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Fills plugin-generated companion column property getters.
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class CompanionPropertyIrTransformer(
    private val pluginContext: IrPluginContext,
) {
    fun transform(element: IrElement) {
        element.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitProperty(declaration: IrProperty) {
                declaration.acceptChildrenVoid(this)
                if (declaration.isPluginGeneratedColumn()) {
                    fillColumn(declaration)
                }
            }
        })
    }

    private fun IrProperty.isPluginGeneratedColumn(): Boolean {
        val origin = origin
        return origin is IrDeclarationOrigin.GeneratedByPlugin &&
            origin.pluginKey == DbFlowColumnPropertyKey
    }

    private fun fillColumn(property: IrProperty) {
        val getter = property.getter ?: return
        val thisReceiver = getter.dispatchReceiverParameter ?: return
        val function = pluginContext.referenceFunctions(
            CallableId(
                FqName("com.dbflow5.query.operations"),
                Name.identifier("generatedColumnProperty"),
            )
        ).firstOrNull() ?: return
        val call = IrCallImpl(
            startOffset = property.startOffset,
            endOffset = property.endOffset,
            type = getter.returnType,
            symbol = function,
            typeArgumentsCount = function.owner.typeParameters.size,
        )
        val owner = function.owner
        owner.typeParameters.forEachIndexed { index, _ ->
            val argument = getter.returnType.typeArguments.getOrNull(index)
            if (argument != null) {
                call.typeArguments[index] = argument
            }
        }
        owner.parameters.forEachIndexed { index, parameter ->
            call.arguments[index] = when (parameter.kind) {
                IrParameterKind.ExtensionReceiver, IrParameterKind.DispatchReceiver ->
                    IrGetValueImpl(
                        startOffset = property.startOffset,
                        endOffset = property.endOffset,
                        type = thisReceiver.type,
                        symbol = thisReceiver.symbol,
                    )
                IrParameterKind.Regular -> IrConstImpl.string(
                    startOffset = property.startOffset,
                    endOffset = property.endOffset,
                    type = pluginContext.irBuiltIns.stringType,
                    value = property.name.asString(),
                )
                else -> null
            }
        }
        val body = pluginContext.irFactory.createBlockBody(property.startOffset, property.endOffset)
        body.statements += IrReturnImpl(
            startOffset = property.startOffset,
            endOffset = property.endOffset,
            type = getter.returnType,
            returnTargetSymbol = getter.symbol,
            value = call,
        )
        getter.body = body
    }
}

private val org.jetbrains.kotlin.ir.types.IrType.typeArguments: List<org.jetbrains.kotlin.ir.types.IrType?>
    get() = (this as? org.jetbrains.kotlin.ir.types.IrSimpleType)
        ?.arguments
        ?.map { projection ->
            (projection as? org.jetbrains.kotlin.ir.types.IrTypeProjection)?.type
        }
        ?: emptyList()
