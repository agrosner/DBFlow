package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.NotNullProperties
import com.dbflow5.ksp.model.properties.OneToManyProperties
import com.dbflow5.ksp.model.properties.ReferenceHolderProperties
import com.dbflow5.ksp.model.properties.SimpleClassProperties
import com.dbflow5.ksp.model.properties.nameWithFallback
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
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
    val ksType: KSType,
    override val originatingFile: KSFile?,
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
            properties = SimpleClassProperties(
                allFields = true,
                database = databaseTypeName,
                orderedCursorLookup = true,
                assignDefaultValuesFromCursor = true,
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