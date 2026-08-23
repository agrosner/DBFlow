package com.dbflow5.compiler.ir

import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.ReferenceHolderModel
import com.dbflow5.codegen.shared.SingleFieldModel
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.codegen.shared.properties.NotNullProperties
import com.dbflow5.codegen.shared.properties.ReferenceHolderProperties
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.parentAsClass

internal class IrPropertyParser : Parser<IrProperty, FieldModel> {
    override fun parse(input: IrProperty): FieldModel {
        val originating = IrOriginatingSource(
            (input.parent as? IrClass)?.kotlinFqName()?.asString().orEmpty()
        )
        val primaryKey = input.annotation(IrTypeFqNames.PrimaryKey)
        val fieldType = if (primaryKey != null) {
            val props = primaryKey.toAnnotationMap()
            FieldModel.FieldType.Primary(
                isAutoIncrement = props.arg("autoincrement") ?: false,
                isRowId = props.arg("rowID") ?: false,
            )
        } else {
            FieldModel.FieldType.Normal
        }
        val irType = input.getter?.returnType ?: input.backingField?.type
            ?: error("Property ${input.name} has no type")
        val ksClassType = IrClassType(irType)
        val isInlineClass = ksClassType.declaration.isValue
        val isEnum = ksClassType.declaration.closestClassDeclaration?.isEnum == true
        val foreignKey = input.annotation(IrTypeFqNames.ForeignKey)
        val columnMapKey = input.annotation(IrTypeFqNames.ColumnMap)
        val notNull = input.annotation(IrTypeFqNames.NotNull)?.toAnnotationMap()
            ?.toNotNullProperties()
            ?: if (!ksClassType.isMarkedNullable) NotNullProperties() else null
        val classType = irType.toPoetTypeName()
        val name = NameModel(
            packageName = input.parentAsClass.poetPackageName(),
            shortName = input.name.asString(),
            nullable = classType.isNullable,
        )
        val isVal = input.setter == null
        val indexProperties = input.annotation(IrTypeFqNames.Index)
            ?.toAnnotationMap()?.toIndexProperties()
        val properties = input.annotation(IrTypeFqNames.Column)
            ?.toAnnotationMap()?.toFieldProperties()
        val uniqueProperties = input.annotation(IrTypeFqNames.Unique)
            ?.toAnnotationMap()?.toUniqueProperties()
        val enclosing = input.parentAsClass.poetClassName()

        if (foreignKey != null || columnMapKey != null) {
            return ReferenceHolderModel(
                name = name,
                classType = classType,
                fieldType = fieldType,
                properties = properties,
                referenceHolderProperties = (foreignKey ?: columnMapKey)
                    ?.toAnnotationMap()?.toReferenceHolderProperties()
                    ?: ReferenceHolderProperties(
                        referencesType = ReferenceHolderProperties.ReferencesType.All,
                        referencedTableTypeName = Any::class.asTypeName(),
                        deferred = false,
                        saveForeignKeyModel = false,
                    ),
                enclosingClassType = enclosing,
                type = if (foreignKey != null) {
                    ReferenceHolderModel.Type.ForeignKey
                } else {
                    ReferenceHolderModel.Type.Computed
                },
                isInlineClass = isInlineClass,
                ksClassType = ksClassType,
                isVal = isVal,
                isColumnMap = columnMapKey != null,
                isEnum = isEnum,
                originatingSource = originating,
                indexProperties = indexProperties,
                notNullProperties = notNull,
                uniqueProperties = uniqueProperties,
            )
        }
        return SingleFieldModel(
            name = name,
            classType = classType,
            fieldType = fieldType,
            properties = properties,
            enclosingClassType = enclosing,
            isInlineClass = isInlineClass,
            isVal = isVal,
            isEnum = isEnum,
            ksClassType = ksClassType,
            originatingSource = originating,
            indexProperties = indexProperties,
            notNullProperties = notNull,
            uniqueProperties = uniqueProperties,
        )
    }
}

private fun IrClass.kotlinFqName() = fqNameWhenAvailable

private fun IrClass.poetClassName() = toPoetClassNameFromClass()

internal fun IrClass.toPoetClassNameFromClass() = toPoetClassName()

internal val IrProperty.isIgnoredColumn: Boolean
    get() = hasAnnotation(IrTypeFqNames.ColumnIgnore) ||
        getter?.hasAnnotation(IrTypeFqNames.ColumnIgnore) == true ||
        setter?.hasAnnotation(IrTypeFqNames.ColumnIgnore) == true

internal val IrProperty.isAbstract: Boolean
    get() = modality == Modality.ABSTRACT
