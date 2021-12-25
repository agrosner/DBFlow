package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.ManyToManyProperties
import com.dbflow5.ksp.model.properties.ReferenceHolderProperties
import com.dbflow5.ksp.model.properties.SimpleClassProperties
import com.dbflow5.ksp.model.properties.nameWithFallback
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
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
) : ObjectModel {

    val dbName = properties.nameWithFallback(
        "${name.shortName}_${properties.referencedTableType.simpleName}",
    )

    private val generatedName = name.copy(
        shortName = dbName
    )

    /**
     * Returns the generated class model to use.
     */
    val classModel: ClassModel =
        ClassModel(
            name = generatedName,
            classType = generatedName.className,
            type = ClassModel.ClassType.Normal,
            properties = SimpleClassProperties(
                allFields = true,
                database = databaseTypeName,
                orderedCursorLookup = true,
                assignDefaultValuesFromCursor = true,
            ),
            fields = listOf(
                ReferenceHolderModel(
                    name = name.copy(shortName = properties.thisTableColumnName
                        .nameWithFallback(
                            name.shortName.replaceFirstChar { it.lowercase() }
                        )
                    ),
                    classType = classType,
                    fieldType = FieldModel.FieldType.PrimaryAuto(
                        isAutoIncrement = false,
                        isRowId = false,
                        quickCheckPrimaryKey = true,
                    ),
                    properties = null,
                    referenceHolderProperties = ReferenceHolderProperties(
                        referencesType = ReferenceHolderProperties.ReferencesType.All,
                        referencedTableTypeName = classType,
                    ),
                    enclosingClassType = generatedName.className,
                    type = ReferenceHolderModel.Type.ForeignKey,
                    isInlineClass = false,
                    ksClassType = ksType,
                    isVal = true,
                    isColumnMap = false,
                    isEnum = false,
                ),
                ReferenceHolderModel(
                    name = name.copy(
                        shortName = properties.referencedTableColumnName
                            .nameWithFallback(
                                properties.referencedTableType.simpleName.replaceFirstChar { it.lowercase() },
                            ),
                    ),
                    classType = properties.referencedTableType,
                    fieldType = FieldModel.FieldType.PrimaryAuto(
                        isAutoIncrement = false,
                        isRowId = false,
                        quickCheckPrimaryKey = true,
                    ),
                    properties = null,
                    referenceHolderProperties = ReferenceHolderProperties(
                        referencesType = ReferenceHolderProperties.ReferencesType.All,
                        referencedTableTypeName = properties.referencedTableType,
                    ),
                    enclosingClassType = generatedName.className,
                    type = ReferenceHolderModel.Type.ForeignKey,
                    isInlineClass = false,
                    isColumnMap = false,
                    isEnum = false,
                    isVal = true,
                    ksClassType = ksType,
                )
            ),
            hasPrimaryConstructor = true,
            isInternal = false,
        )
}