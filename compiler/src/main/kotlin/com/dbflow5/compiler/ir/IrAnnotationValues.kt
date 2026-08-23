package com.dbflow5.compiler.ir

import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.FqName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

internal typealias AnnotationMap = Map<String, Any?>

internal fun IrAnnotationContainer.annotation(fqName: FqName): IrConstructorCall? =
    annotations.firstOrNull { it.annotationFqName() == fqName }

internal fun IrAnnotationContainer.hasAnnotation(fqName: FqName): Boolean =
    annotation(fqName) != null

internal fun IrProperty.annotation(fqName: FqName): IrConstructorCall? =
    annotations.firstOrNull { it.annotationFqName() == fqName }
        ?: backingField?.annotation(fqName)
        ?: getter?.annotation(fqName)
        ?: setter?.annotation(fqName)
        ?: correspondingConstructorParameter()?.annotation(fqName)

internal fun IrProperty.hasAnnotation(fqName: FqName): Boolean =
    annotation(fqName) != null

internal fun IrProperty.correspondingConstructorParameter() =
    (parent as? IrClass)?.primaryConstructor?.regularParameters
        ?.firstOrNull { it.name == name }

internal fun IrConstructorCall.annotationFqName(): FqName? =
    type.classFqName ?: symbol.owner.parentAsClass.fqNameWhenAvailable

internal fun IrConstructorCall.toAnnotationMap(): AnnotationMap {
    return symbol.owner.parameters.zip(arguments).mapNotNull { (parameter, argument) ->
        val value = argument?.toAnnotationValue() ?: return@mapNotNull null
        parameter.name.asString() to value
    }.toMap()
}

internal fun IrExpression.toAnnotationValue(): Any? = when (this) {
    is IrConst -> value
    is IrClassReference -> classType.toPoetTypeName()
    is IrGetEnumValue -> symbol.owner.name.asString()
    is IrConstructorCall -> this
    is IrVararg -> elements.mapNotNull { element ->
        when (element) {
            is IrExpression -> element.toAnnotationValue()
            is IrSpreadElement -> null
            else -> null
        }
    }
    else -> null
}

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> AnnotationMap.arg(name: String): T? = get(name) as? T

internal inline fun <reified T> AnnotationMap.expectedArg(name: String): T =
    arg(name) ?: error("Missing annotation argument $name")

internal inline fun <reified T : Enum<T>> AnnotationMap.enumArg(
    name: String,
    defValue: T,
    valueOf: (String) -> T,
): T {
    val raw = get(name) ?: return defValue
    val shortName = when (raw) {
        is String -> raw.substringAfterLast('.')
        else -> raw.toString().substringAfterLast('.')
    }
    return valueOf(shortName)
}

internal fun AnnotationMap.typeName(name: String): TypeName? = get(name) as? TypeName

internal fun AnnotationMap.className(name: String): ClassName? =
    when (val value = get(name)) {
        is ClassName -> value
        is TypeName -> value as? ClassName
        else -> null
    }

internal fun AnnotationMap.classNameList(name: String): List<ClassName> =
    (get(name) as? List<*>)?.mapNotNull {
        when (it) {
            is ClassName -> it
            is TypeName -> it as? ClassName
            else -> null
        }
    } ?: emptyList()

internal fun AnnotationMap.nested(name: String): AnnotationMap =
    (get(name) as? IrConstructorCall)?.toAnnotationMap() ?: emptyMap()

internal fun AnnotationMap.nestedList(name: String): List<AnnotationMap> =
    (get(name) as? List<*>)?.mapNotNull {
        (it as? IrConstructorCall)?.toAnnotationMap()
    } ?: emptyList()

internal fun IrClass.findAnnotation(fqName: FqName): IrConstructorCall? = annotation(fqName)

internal object IrTypeFqNames {
    val Table = FqName("com.dbflow5.annotation.Table")
    val Query = FqName("com.dbflow5.annotation.Query")
    val ModelView = FqName("com.dbflow5.annotation.ModelView")
    val Database = FqName("com.dbflow5.annotation.Database")
    val TypeConverter = FqName("com.dbflow5.annotation.TypeConverter")
    val ManyToMany = FqName("com.dbflow5.annotation.ManyToMany")
    val MultipleManyToMany = FqName("com.dbflow5.annotation.MultipleManyToMany")
    val OneToMany = FqName("com.dbflow5.annotation.OneToMany")
    val Migration = FqName("com.dbflow5.annotation.Migration")
    val Fts3 = FqName("com.dbflow5.annotation.Fts3")
    val Fts4 = FqName("com.dbflow5.annotation.Fts4")
    val GranularNotifications = FqName("com.dbflow5.annotation.GranularNotifications")
    val Column = FqName("com.dbflow5.annotation.Column")
    val ColumnIgnore = FqName("com.dbflow5.annotation.ColumnIgnore")
    val ColumnMap = FqName("com.dbflow5.annotation.ColumnMap")
    val PrimaryKey = FqName("com.dbflow5.annotation.PrimaryKey")
    val ForeignKey = FqName("com.dbflow5.annotation.ForeignKey")
    val Index = FqName("com.dbflow5.annotation.Index")
    val Unique = FqName("com.dbflow5.annotation.Unique")
    val NotNull = FqName("com.dbflow5.annotation.NotNull")
}

internal fun IrConstructorCall.matches(fqName: FqName): Boolean =
    type.classFqName == fqName || annotationFqName() == fqName
