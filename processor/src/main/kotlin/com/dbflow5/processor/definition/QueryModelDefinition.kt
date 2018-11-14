package com.dbflow5.processor.definition

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.QueryModel
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ColumnValidator
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.behavior.AssociationalBehavior
import com.dbflow5.processor.definition.behavior.CursorHandlingBehavior
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.TypeElement

/**
 * Description:
 */
class QueryModelDefinition(override val associationalBehavior: AssociationalBehavior,
                           override val cursorHandlingBehavior: CursorHandlingBehavior,
                           typeElement: TypeElement,
                           processorManager: ProcessorManager)
    : EntityDefinition(typeElement, processorManager) {

    override val methods: Array<MethodDefinition> = arrayOf(LoadFromCursorMethod(this),
        ExistenceMethod(this),
        PrimaryConditionMethod(this))

    constructor(queryModel: QueryModel, typeElement: TypeElement,
                processorManager: ProcessorManager) : this(
        AssociationalBehavior(
            name = typeElement.simpleName.toString(),
            databaseTypeName = queryModel.extractTypeNameFromAnnotation { it.database },
            allFields = queryModel.allFields
        ),
        CursorHandlingBehavior(
            orderedCursorLookup = queryModel.orderedCursorLookUp,
            assignDefaultValuesFromCursor = queryModel.assignDefaultValuesFromCursor
        ),
        typeElement, processorManager)

    /**
     * [ColumnMap] constructor.
     */
    constructor(typeElement: TypeElement,
                databaseTypeName: TypeName,
                processorManager: ProcessorManager) : this(
        AssociationalBehavior(
            name = typeElement.simpleName.toString(),
            databaseTypeName = databaseTypeName,
            allFields = true
        ),
        CursorHandlingBehavior(),
        typeElement, processorManager)

    init {
        setOutputClassName("_QueryTable")
        processorManager.addModelToDatabase(elementClassName, associationalBehavior.databaseTypeName)
    }

    override fun prepareForWriteInternal() {
        typeElement?.let { createColumnDefinitions(it) }
    }

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(ClassNames.QUERY_MODEL_ADAPTER, elementClassName)

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
                        manager.logError("QueryModel $elementName cannot have primary keys")
                    }
                }
            }
        }
    }

    override val primaryColumnDefinitions: List<ColumnDefinition>
        get() = columnDefinitions

}
