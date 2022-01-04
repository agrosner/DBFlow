package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.IndexProperties
import com.dbflow5.ksp.model.properties.ManyToManyProperties
import com.dbflow5.ksp.model.properties.NotNullProperties
import com.dbflow5.ksp.model.properties.ReferenceHolderProperties
import com.dbflow5.ksp.model.properties.SimpleClassProperties
import com.dbflow5.ksp.model.properties.nameWithFallback
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
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
     * The database it references.
     */
    val databaseTypeName: TypeName,
    val properties: ManyToManyProperties,
    val ksType: KSType,
    override val originatingFile: KSFile?,
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
                fieldType = FieldModel.FieldType.PrimaryAuto(
                    isAutoIncrement = true,
                    isRowId = false,
                ),
                properties = null,
                enclosingClassType = generatedName.className,
                isInlineClass = false,
                ksClassType = ksType,
                isVal = true,
                isEnum = false,
                originatingFile = originatingFile,
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
            } else FieldModel.FieldType.PrimaryAuto(
                isAutoIncrement = false,
                isRowId = false,
                quickCheckPrimaryKey = true,
            ),
            properties = null,
            referenceHolderProperties = ReferenceHolderProperties(
                referencesType = ReferenceHolderProperties.ReferencesType.All,
                referencedTableTypeName = classType,
                deferred = false,
            ),
            enclosingClassType = generatedName.className,
            type = ReferenceHolderModel.Type.ForeignKey,
            isInlineClass = false,
            ksClassType = ksType,
            isVal = true,
            isColumnMap = false,
            isEnum = false,
            originatingFile = originatingFile,
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
            } else FieldModel.FieldType.PrimaryAuto(
                isAutoIncrement = false,
                isRowId = false,
                quickCheckPrimaryKey = true,
            ),
            properties = null,
            referenceHolderProperties = ReferenceHolderProperties(
                referencesType = ReferenceHolderProperties.ReferencesType.All,
                referencedTableTypeName = properties.referencedTableType,
                deferred = false,
            ),
            enclosingClassType = generatedName.className,
            type = ReferenceHolderModel.Type.ForeignKey,
            isInlineClass = false,
            isColumnMap = false,
            isEnum = false,
            isVal = true,
            ksClassType = ksType,
            originatingFile = originatingFile,
            indexProperties = properties.generateAutoIncrement.takeIf { !it }
                ?.let { IndexProperties(listOf()) },
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
            type = ClassModel.ClassType.Normal.Normal,
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
            indexGroups = listOf(
                IndexGroupModel(
                    //
                    name = name.shortName,
                    // only primary fields on index and if they have index property (safeguard)
                    fields = fields.filter {
                        it.fieldType is FieldModel.FieldType.PrimaryAuto &&
                            it.indexProperties != null
                    },
                    unique = false,
                    tableTypeName = generatedName.className,
                )
            ),
            uniqueGroups = listOf(),
            implementsLoadFromCursorListener = false,
            implementsSQLiteStatementListener = false,
        )
}