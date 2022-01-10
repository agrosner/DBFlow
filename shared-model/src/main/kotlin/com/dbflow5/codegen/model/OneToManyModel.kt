package com.dbflow5.codegen.model

import com.dbflow5.codegen.model.interop.ClassType
import com.dbflow5.codegen.model.interop.OriginatingFileType
import com.dbflow5.codegen.model.properties.GeneratedClassProperties
import com.dbflow5.codegen.model.properties.NotNullProperties
import com.dbflow5.codegen.model.properties.OneToManyProperties
import com.dbflow5.codegen.model.properties.ReferenceHolderProperties
import com.dbflow5.codegen.model.properties.nameWithFallback
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

/**
 * Description: Generates a [ClassModel] to use in DB.
 */
data class OneToManyModel(
    /**
     * The name of the parent
     */
    val name: NameModel,
    val classType: ClassName,
    val databaseTypeName: TypeName,
    val properties: OneToManyProperties,
    val ksType: ClassType,
    override val originatingFile: OriginatingFileType?,
) : ObjectModel {
    val dbName = properties.nameWithFallback(
        "${name.shortName}_${properties.childTableType.simpleName}",
    )

    private val generatedName = name.copy(
        shortName = dbName
    )

    val fields = listOf(
        ReferenceHolderModel(
            name = name.copy(shortName = properties.parentFieldName
                .nameWithFallback(
                    name.shortName.replaceFirstChar { it.lowercase() }
                )
            ),
            classType = classType,
            fieldType = FieldModel.FieldType.Normal,
            properties = null,
            referenceHolderProperties = ReferenceHolderProperties(
                referencesType = ReferenceHolderProperties.ReferencesType.All,
                referencedTableTypeName = classType,
                deferred = false,
                saveForeignKeyModel = false,
            ),
            enclosingClassType = generatedName.className,
            // we'll flatten this onto the object directly.
            type = ReferenceHolderModel.Type.Computed,
            isInlineClass = false,
            ksClassType = ksType,
            isVal = true,
            isColumnMap = false,
            isEnum = false,
            originatingFile = originatingFile,
            indexProperties = null,
            notNullProperties = NotNullProperties(),
            uniqueProperties = null,
        ),
        ReferenceHolderModel(
            name = name.copy(
                shortName = properties.childListFieldName
                    .nameWithFallback(
                        properties.childTableType.simpleName.replaceFirstChar { it.lowercase() },
                    ),
            ),
            classType = List::class.asClassName().parameterizedBy(properties.childTableType),
            fieldType = FieldModel.FieldType.Normal,
            properties = null,
            referenceHolderProperties = ReferenceHolderProperties(
                referencesType = ReferenceHolderProperties.ReferencesType.All,
                referencedTableTypeName = properties.childTableType,
                deferred = false,
                saveForeignKeyModel = false,
            ),
            enclosingClassType = generatedName.className,
            type = ReferenceHolderModel.Type.Reference,
            isInlineClass = false,
            isColumnMap = false,
            isEnum = false,
            isVal = true,
            ksClassType = ksType,
            originatingFile = originatingFile,
            indexProperties = null,
            notNullProperties = NotNullProperties(),
            uniqueProperties = null,
        )
    )

    /**
     * Returns the generated class model to use.
     */
    val classModel: ClassModel =
        ClassModel(
            name = generatedName,
            classType = generatedName.className,
            type = ClassModel.ClassType.Query,
            properties = GeneratedClassProperties(
                allFields = true,
                database = databaseTypeName,
                orderedCursorLookup = true,
                assignDefaultValuesFromCursor = true,
                generatedFromClassType = classType,
            ),
            fields = fields,
            hasPrimaryConstructor = true,
            isInternal = false,
            originatingFile = originatingFile,
            indexGroups = listOf(),
            uniqueGroups = listOf(),
            implementsLoadFromCursorListener = false,
            implementsSQLiteStatementListener = false,
        )
}