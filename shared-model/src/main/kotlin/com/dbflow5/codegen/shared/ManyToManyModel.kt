package com.dbflow5.codegen.shared

import com.dbflow5.codegen.shared.interop.ClassType
import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.dbflow5.codegen.shared.properties.GeneratedClassProperties
import com.dbflow5.codegen.shared.properties.IndexProperties
import com.dbflow5.codegen.shared.properties.ManyToManyProperties
import com.dbflow5.codegen.shared.properties.NotNullProperties
import com.dbflow5.codegen.shared.properties.ReferenceHolderProperties
import com.dbflow5.codegen.shared.properties.nameWithFallback
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.TypeName

/**
 * Description: Generates a [ClassModel] to use in DB.
 */
data class ManyToManyModel(
    /**
     * Declared type of referenced class.
     */
    val classType: ClassName,
    /**
     * Name of the referenced class.
     */
    val name: NameModel,
    /**
     * The database it references, could be Any if inferred from DB inclusion.
     */
    val databaseTypeName: TypeName,
    val properties: ManyToManyProperties,
    val ksType: ClassType,
    override val originatingSource: OriginatingSource?,
) : ObjectModel {

    val dbName = properties.nameWithFallback(
        "${name.shortName}_${properties.referencedTableType.simpleName}",
    )

    private val generatedName = name.copy(
        shortName = dbName
    )

    val fields = listOfNotNull(
        if (properties.generateAutoIncrement) {
            SingleFieldModel(
                name = name.copy(
                    shortName = "id",
                ),
                classType = INT,
                fieldType = FieldModel.FieldType.Primary(
                    isAutoIncrement = true,
                    isRowId = false,
                ),
                properties = null,
                enclosingClassType = generatedName.className,
                isInlineClass = false,
                ksClassType = ksType,
                isVal = true,
                isEnum = false,
                originatingSource = originatingSource,
                /**
                 * Index these for faster retrieval by default.
                 */
                indexProperties = IndexProperties(listOf()),
                notNullProperties = NotNullProperties(),
                uniqueProperties = null,
            )
        } else null,
        ReferenceHolderModel(
            name = name.copy(shortName = properties.thisTableColumnName
                .nameWithFallback(
                    name.shortName.replaceFirstChar { it.lowercase() }
                )
            ),
            classType = classType,
            fieldType = if (properties.generateAutoIncrement) {
                FieldModel.FieldType.Normal
            } else FieldModel.FieldType.Primary(
                isAutoIncrement = false,
                isRowId = false,
            ),
            properties = null,
            referenceHolderProperties = ReferenceHolderProperties(
                referencesType = ReferenceHolderProperties.ReferencesType.All,
                referencedTableTypeName = classType,
                deferred = false,
                saveForeignKeyModel = properties.saveForeignKeyModels,
            ),
            enclosingClassType = generatedName.className,
            type = ReferenceHolderModel.Type.ForeignKey,
            isInlineClass = false,
            ksClassType = ksType,
            isVal = true,
            isColumnMap = false,
            isEnum = false,
            originatingSource = originatingSource,
            indexProperties = properties.generateAutoIncrement.takeIf { !it }
                ?.let { IndexProperties(listOf()) },
            notNullProperties = NotNullProperties(),
            uniqueProperties = null,
        ),
        ReferenceHolderModel(
            name = name.copy(
                shortName = properties.referencedTableColumnName
                    .nameWithFallback(
                        properties.referencedTableType.simpleName.replaceFirstChar { it.lowercase() },
                    ),
            ),
            classType = properties.referencedTableType,
            fieldType = if (properties.generateAutoIncrement) {
                FieldModel.FieldType.Normal
            } else FieldModel.FieldType.Primary(
                isAutoIncrement = false,
                isRowId = false,
            ),
            properties = null,
            referenceHolderProperties = ReferenceHolderProperties(
                referencesType = ReferenceHolderProperties.ReferencesType.All,
                referencedTableTypeName = properties.referencedTableType,
                deferred = false,
                saveForeignKeyModel = properties.saveForeignKeyModels,
            ),
            enclosingClassType = generatedName.className,
            type = ReferenceHolderModel.Type.ForeignKey,
            isInlineClass = false,
            isColumnMap = false,
            isEnum = false,
            isVal = true,
            ksClassType = ksType,
            originatingSource = originatingSource,
            indexProperties = properties.generateAutoIncrement.takeIf { !it }
                ?.let { IndexProperties(listOf()) },
            notNullProperties = NotNullProperties(),
            uniqueProperties = null,
        )
    )

    /**
     * Returns the generated class model to use.
     */
    /**
     * Returns the generated class model to use.
     */
    val classModel: ClassModel = ClassModel(
        name = generatedName,
        classType = generatedName.className,
        type = ClassModel.Type.Table.Normal,
        properties = GeneratedClassProperties(
            allFields = true,
            database = databaseTypeName,
            orderedCursorLookup = true,
            assignDefaultValuesFromCursor = true,
            generatedFromClassType = classType,
        ),
        fields = fields,
        hasImmutableConstructor = true,
        isInternal = false,
        originatingSource = originatingSource,
        indexGroups = listOf(
            IndexGroupModel(
                //
                name = name.shortName,
                // only primary fields on index and if they have index property (safeguard)
                fields = fields.filter {
                    it.fieldType is FieldModel.FieldType.Primary &&
                        it.indexProperties != null
                },
                unique = false,
                tableTypeName = generatedName.className,
            )
        ),
        uniqueGroups = listOf(),
        implementsLoadFromCursorListener = false,
        implementsDatabaseStatementListener = false,
        ksClassType = ksType,
        granularNotifications = false,
        isDataClass = true,
    )
}