package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.*
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ModelView
import com.raizlabs.android.dbflow.annotation.ModelViewQuery
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ColumnValidator
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition
import com.raizlabs.android.dbflow.processor.utils.*
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

/**
 * Description: Used in writing ModelViewAdapters
 */
class ModelViewDefinition(manager: ProcessorManager, element: Element) : BaseTableDefinition(element, manager), Comparable<ModelViewDefinition> {

    internal val implementsLoadFromCursorListener: Boolean

    var databaseName: TypeName? = null

    private var queryFieldName: String? = null

    private var name: String? = null

    private val methods: Array<MethodDefinition> =
            arrayOf(LoadFromCursorMethod(this), ExistenceMethod(this), PrimaryConditionMethod(this))

    var allFields: Boolean = false

    var priority: Int = 0

    init {

        element.annotation<ModelView>()?.let { modelView ->
            try {
                modelView.database
            } catch (mte: MirroredTypeException) {
                this.databaseName = TypeName.get(mte.typeMirror)
            }

            allFields = modelView.allFields

            this.name = modelView.name
            if (name == null || name!!.isEmpty()) {
                name = modelClassName
            }
            this.priority = modelView.priority
        }

        if (element is TypeElement) {
            implementsLoadFromCursorListener = element.implementsClass(manager.processingEnvironment,
                    ClassNames.LOAD_FROM_CURSOR_LISTENER)
        } else {
            implementsLoadFromCursorListener = false
        }

    }

    override fun prepareForWrite() {
        classElementLookUpMap.clear()
        columnDefinitions.clear()
        queryFieldName = null

        val modelView = element.getAnnotation(ModelView::class.java)
        if (modelView != null) {
            databaseDefinition = manager.getDatabaseHolderDefinition(databaseName)?.databaseDefinition
            setOutputClassName("${databaseDefinition?.classSeparator}ViewTable")

            typeElement?.let { createColumnDefinitions(it) }
        } else {
            setOutputClassName("ViewTable")
        }
    }

    override fun createColumnDefinitions(typeElement: TypeElement) {
        val variableElements = ElementUtility.getAllElements(typeElement, manager)

        for (element in variableElements) {
            classElementLookUpMap.put(element.simpleName.toString(), element)
        }

        val columnValidator = ColumnValidator()
        for (variableElement in variableElements) {

            val isValidAllFields = ElementUtility.isValidAllFields(allFields, element)

            if (variableElement.annotation<Column>() != null || isValidAllFields) {

                // package private, will generate helper
                val isPackagePrivate = ElementUtility.isPackagePrivate(element)
                val isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, element, this.element)

                val columnDefinition = ColumnDefinition(manager, variableElement, this, isPackagePrivateNotInSamePackage)
                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition)

                    if (isPackagePrivate) {
                        columnDefinitions.add(columnDefinition)
                    }
                }

                if (columnDefinition.isPrimaryKey || columnDefinition is ForeignKeyColumnDefinition
                        || columnDefinition.isPrimaryKeyAutoIncrement || columnDefinition.isRowId) {
                    manager.logError("ModelViews cannot have primary or foreign keys")
                }
            } else if (variableElement.annotation<ModelViewQuery>() != null) {
                if (!queryFieldName.isNullOrEmpty()) {
                    manager.logError("Found duplicate queryField name: $queryFieldName for $elementClassName")
                }
                ensureVisibleStatic(variableElement, typeElement, "ModelViewQuery")

                val element = variableElement.toTypeErasedElement()
                if (!element.implementsClass(manager.processingEnvironment, ClassNames.QUERY)) {
                    manager.logError("The field ${variableElement.simpleName} must implement ${ClassNames.QUERY}")
                }

                queryFieldName = variableElement.simpleString
            }
        }

        if (queryFieldName.isNullOrEmpty()) {
            manager.logError("$elementClassName is missing the @ModelViewQuery field.")
        }
    }

    override val primaryColumnDefinitions: List<ColumnDefinition>
        get() = columnDefinitions

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(ClassNames.MODEL_VIEW_ADAPTER, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        typeBuilder.apply {
            `public static final field`(String::class, "VIEW_NAME") { `=`(name.S) }

            elementClassName?.let { elementClassName ->
                columnDefinitions.forEach { it.addPropertyDefinition(typeBuilder, elementClassName) }
            }

            writeConstructor(this)

            writeGetModelClass(typeBuilder, elementClassName)

            `override fun`(String::class, "getCreationQuery") {
                modifiers(public, final)
                `return`("\$T.\$L.getQuery()", elementClassName, queryFieldName)
            }
            `override fun`(String::class, "getViewName") {
                modifiers(public, final)
                `return`(name.S)
            }
            `override fun`(elementClassName!!, "newInstance") {
                modifiers(public, final)
                `return`("new \$T()", elementClassName)
            }

        }

        methods.mapNotNull { it.methodSpec }
                .forEach { typeBuilder.addMethod(it) }
    }

    override fun compareTo(other: ModelViewDefinition): Int {
        return Integer.valueOf(priority)!!.compareTo(other.priority)
    }
}