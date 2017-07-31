package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.`return`
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.public
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ColumnMap
import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ColumnValidator
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.ReferenceColumnDefinition
import com.raizlabs.android.dbflow.processor.utils.ElementUtility
import com.raizlabs.android.dbflow.processor.utils.`override fun`
import com.raizlabs.android.dbflow.processor.utils.annotation
import com.raizlabs.android.dbflow.processor.utils.implementsClass
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

/**
 * Description:
 */
class QueryModelDefinition(typeElement: Element, processorManager: ProcessorManager)
    : BaseTableDefinition(typeElement, processorManager) {

    var allFields: Boolean = false

    var implementsLoadFromCursorListener = false

    internal var methods: Array<MethodDefinition>

    init {

        typeElement.annotation<QueryModel>()?.let { queryModel ->
            try {
                queryModel.database
            } catch (mte: MirroredTypeException) {
                databaseTypeName = TypeName.get(mte.typeMirror)
            }
        }

        elementClassName?.let { elementClassName -> databaseTypeName?.let { processorManager.addModelToDatabase(elementClassName, it) } }

        if (element is TypeElement) {
            implementsLoadFromCursorListener =
                (element as TypeElement).implementsClass(manager.processingEnvironment, ClassNames.LOAD_FROM_CURSOR_LISTENER)
        }


        methods = arrayOf<MethodDefinition>(LoadFromCursorMethod(this))

    }

    override fun prepareForWrite() {
        classElementLookUpMap.clear()
        columnDefinitions.clear()
        packagePrivateList.clear()

        val queryModel = typeElement.annotation<QueryModel>()
        if (queryModel != null) {
            allFields = queryModel.allFields
        } else {
            allFields = true
        }

        databaseDefinition = manager.getDatabaseHolderDefinition(databaseTypeName)?.databaseDefinition
        setOutputClassName("${databaseDefinition?.classSeparator}QueryTable")

        typeElement?.let { createColumnDefinitions(it) }
    }

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(ClassNames.QUERY_MODEL_ADAPTER, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        typeBuilder.apply {
            elementClassName?.let { className -> columnDefinitions.forEach { it.addPropertyDefinition(this, className) } }

            writeGetModelClass(typeBuilder, elementClassName)

            writeConstructor(this)

            `override fun`(elementClassName!!, "newInstance") {
                modifiers(public, final)
                `return`("new \$T()", elementClassName)
            }
        }

        methods.mapNotNull { it.methodSpec }
            .forEach { typeBuilder.addMethod(it) }
    }

    override fun createColumnDefinitions(typeElement: TypeElement) {
        val variableElements = ElementUtility.getAllElements(typeElement, manager)

        for (element in variableElements) {
            classElementLookUpMap.put(element.simpleName.toString(), element)
        }

        val columnValidator = ColumnValidator()
        for (variableElement in variableElements) {

            // no private static or final fields
            val isAllFields = ElementUtility.isValidAllFields(allFields, variableElement)
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

                if (columnDefinition.isPrimaryKey || columnDefinition.isPrimaryKeyAutoIncrement || columnDefinition.isRowId) {
                    manager.logError("QueryModel $elementName cannot have primary keys")
                }
            }
        }
    }

    override // Shouldn't include any
    val primaryColumnDefinitions: List<ColumnDefinition>
        get() = ArrayList()

}
