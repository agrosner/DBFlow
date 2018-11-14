package com.dbflow5.processor.definition

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.VirtualTable
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ColumnValidator
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.behavior.AssociationalBehavior
import com.dbflow5.processor.definition.behavior.CreationQueryBehavior
import com.dbflow5.processor.definition.behavior.CursorHandlingBehavior
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.stripQuotes
import com.grosner.kpoet.`=`
import com.grosner.kpoet.`public static final field`
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.TypeElement

/**
 * Description:
 */
class VirtualTableDefinition(virtualTable: VirtualTable,
                             typeElement: TypeElement, processorManager: ProcessorManager)
    : EntityDefinition(typeElement, processorManager) {

    private val creationQueryBehavior = CreationQueryBehavior(createWithDatabase = virtualTable.createWithDatabase)
    val type: VirtualTable.Type = virtualTable.type
    var contentTableDefinition: TableDefinition? = null
    private val databaseTypeName: TypeName? = virtualTable.extractTypeNameFromAnnotation { it.database }
    private val contentTableClassName = virtualTable.extractTypeNameFromAnnotation { it.contentTable }

    override val associationalBehavior = AssociationalBehavior(
            name = if (virtualTable.name.isNullOrEmpty()) typeElement.simpleName.toString() else virtualTable.name,
            databaseTypeName = virtualTable.extractTypeNameFromAnnotation { it.database },
            allFields = virtualTable.allFields
    )

    override val cursorHandlingBehavior = CursorHandlingBehavior(
            orderedCursorLookup = virtualTable.orderedCursorLookUp,
            assignDefaultValuesFromCursor = virtualTable.assignDefaultValuesFromCursor)

    override val methods: Array<MethodDefinition> = arrayOf(
            VirtualCreationMethod(this),
            LoadFromCursorMethod(this),
            ExistenceMethod(this),
            PrimaryConditionMethod(this))

    init {
        setOutputClassName("_VirtualTable")
    }

    override fun createColumnDefinitions(typeElement: TypeElement) {
        val columnGenerator = BasicColumnGenerator(manager)
        val variableElements = ElementUtility.getAllElements(typeElement, manager)
        for (element in variableElements) {
            classElementLookUpMap[element.simpleName.toString()] = element
        }
        val columnValidator = ColumnValidator()
        for (variableElement in variableElements) {

            // no private static or final fields
            val isAllFields = ElementUtility.isValidAllFields(associationalBehavior.allFields, variableElement)
            // package private, will generate helper
            val isColumnMap = variableElement.annotation<ColumnMap>() != null

            if (variableElement.annotation<Column>() != null || isAllFields || isColumnMap) {
                val isPackagePrivate = ElementUtility.isPackagePrivate(element)
                columnGenerator.generate(variableElement, this)?.let { columnDefinition ->
                    if (columnValidator.validate(manager, columnDefinition)) {
                        if (type != VirtualTable.Type.FTS4 ||
                                columnDefinition.elementClassName == ClassName.get(String::class.java)) {
                            columnDefinitions.add(columnDefinition)
                            if (isPackagePrivate) {
                                packagePrivateList.add(columnDefinition)
                            }
                        } else {
                            if (type == VirtualTable.Type.FTS4) {
                                manager.logError("Virtual $type Table of type $elementName must only contain String columns")
                            }
                        }
                    }

                    if (columnDefinition.type.isPrimaryField) {
                        manager.logError("Virtual $type Table of type $elementName cannot have primary keys")
                    }
                }
            }
        }
    }

    override val primaryColumnDefinitions: List<ColumnDefinition>
        get() = columnDefinitions

    override fun prepareForWriteInternal() {
        contentTableDefinition = when {
            contentTableClassName != TypeName.OBJECT -> manager.getTableDefinition(databaseTypeName, contentTableClassName)
            else -> null
        }
        typeElement?.let { createColumnDefinitions(typeElement) }
    }

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(ClassNames.VIRTUAL_TABLE_ADAPTER, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        typeBuilder.apply {
            elementClassName?.let { elementClassName ->
                columnDefinitions.forEach { it.addPropertyDefinition(this, elementClassName) }
            }

            // generate implicit docid column if not defined by model.
            if (columnDefinitions.any { it.columnName.stripQuotes() != "docid" }) {
                `public static final field`(ParameterizedTypeName.get(ClassNames.PROPERTY, TypeName.INT.box()), "docid") {
                    addJavadoc("Generated docid for FTS4 tables")
                    `=`("new \$T(\$T.class, \$S)",
                            ParameterizedTypeName.get(ClassNames.PROPERTY, TypeName.INT.box()),
                            elementClassName, "docid")
                }
            }

            creationQueryBehavior.addToType(this)

            writeGetModelClass(this, elementClassName)
            associationalBehavior.writeTableName(this)
            this.writeConstructor()
            methods.mapNotNull { it.methodSpec }
                    .forEach { addMethod(it) }
        }


    }
}