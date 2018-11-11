package com.dbflow5.processor.definition

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.QueryModel
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ColumnValidator
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.definition.column.ReferenceColumnDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.dbflow5.processor.utils.implementsClass
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.TypeElement

/**
 * Description:
 */
class QueryModelDefinition(queryModel: QueryModel,
                           typeElement: TypeElement,
                           processorManager: ProcessorManager)
    : BaseTableDefinition(typeElement, processorManager) {

    private val implementsLoadFromCursorListener = typeElement.implementsClass(manager.processingEnvironment,
            ClassNames.LOAD_FROM_CURSOR_LISTENER)

    internal var methods: Array<MethodDefinition>

    override val associationalBehavior: AssociationalBehavior = AssociationalBehavior(
            name = typeElement.simpleName.toString(),
            databaseTypeName = queryModel.extractTypeNameFromAnnotation { it.database },
            allFields = queryModel.allFields
    )

    init {
        elementClassName?.let { elementClassName ->
            processorManager.addModelToDatabase(elementClassName, associationalBehavior.databaseTypeName)
        }

        methods = arrayOf(LoadFromCursorMethod(this))
    }

    override fun prepareForWrite() {
        classElementLookUpMap.clear()
        columnDefinitions.clear()
        packagePrivateList.clear()

        databaseDefinition = manager.getDatabaseHolderDefinition(associationalBehavior.databaseTypeName)?.databaseDefinition
        setOutputClassName("${databaseDefinition?.classSeparator}QueryTable")

        typeElement?.let { createColumnDefinitions(it) }
    }

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(com.dbflow5.processor.ClassNames.QUERY_MODEL_ADAPTER, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        typeBuilder.apply {
            elementClassName?.let { elementClassName ->
                columnDefinitions.forEach { it.addPropertyDefinition(this, elementClassName) }
            }

            writeGetModelClass(typeBuilder, elementClassName)
            this.writeConstructor()
        }

        methods.mapNotNull { it.methodSpec }
                .forEach { typeBuilder.addMethod(it) }
    }

    override fun createColumnDefinitions(typeElement: TypeElement) {
        val variableElements = ElementUtility.getAllElements(typeElement, manager)
        variableElements.forEach { classElementLookUpMap.put(it.simpleName.toString(), it) }

        val columnValidator = ColumnValidator()
        for (variableElement in variableElements) {

            // no private static or final fields
            val isAllFields = ElementUtility.isValidAllFields(associationalBehavior.allFields, variableElement)
            // package private, will generate helper
            val isPackagePrivate = ElementUtility.isPackagePrivate(variableElement)
            val isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, variableElement, this.element)
            val isColumnMap = variableElement.annotation<ColumnMap>() != null

            if (variableElement.annotation<Column>() != null || isAllFields || isColumnMap) {

                if (checkInheritancePackagePrivate(isPackagePrivateNotInSamePackage, variableElement)) return

                val columnDefinition = if (isColumnMap) {
                    ReferenceColumnDefinition(manager, this, variableElement, isPackagePrivateNotInSamePackage)
                } else {
                    ColumnDefinition(manager, variableElement, this, isPackagePrivateNotInSamePackage)
                }
                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition)

                    if (isPackagePrivate) {
                        packagePrivateList.add(columnDefinition)
                    }
                }

                if (columnDefinition.type.isPrimaryField) {
                    manager.logError("QueryModel $elementName cannot have primary keys")
                }
            }
        }
    }

    override // Shouldn't include any
    val primaryColumnDefinitions: List<ColumnDefinition>
        get() = arrayListOf()

}
