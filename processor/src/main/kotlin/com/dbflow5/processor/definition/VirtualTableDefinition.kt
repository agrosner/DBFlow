package com.dbflow5.processor.definition

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.VirtualTable
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ColumnValidator
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.behavior.AssociationalBehavior
import com.dbflow5.processor.definition.behavior.CursorHandlingBehavior
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.dbflow5.processor.utils.isNullOrEmpty
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

    private val createWithDatabase: Boolean = virtualTable.createWithDatabase
    val type: VirtualTable.Type = virtualTable.type
    var contentTableDefinition: TableDefinition? = null

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
                        columnDefinitions.add(columnDefinition)
                        if (isPackagePrivate) {
                            packagePrivateList.add(columnDefinition)
                        }
                    }

                    if (columnDefinition.type.isPrimaryField) {
                        manager.logError("Virtual $type Table of type $elementName cannot have primary keys")
                    }
                }
            }
        }
    }

    override // Shouldn't include any
    val primaryColumnDefinitions: List<ColumnDefinition>
        get() = arrayListOf()

    override fun prepareForWriteInternal() {
        typeElement?.let { createColumnDefinitions(typeElement) }
    }

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(ClassNames.VIRTUAL_TABLE_ADAPTER, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        typeBuilder.apply {
            writeGetModelClass(this, elementClassName)
            this.writeConstructor()


        }
    }
}