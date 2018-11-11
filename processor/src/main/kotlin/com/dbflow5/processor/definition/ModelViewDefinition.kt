package com.dbflow5.processor.definition

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.ModelViewQuery
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ColumnValidator
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.definition.column.ReferenceColumnDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.ModelUtils
import com.dbflow5.processor.utils.`override fun`
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.ensureVisibleStatic
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.dbflow5.processor.utils.implementsClass
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.processor.utils.simpleString
import com.dbflow5.processor.utils.toTypeElement
import com.dbflow5.processor.utils.toTypeErasedElement
import com.grosner.kpoet.S
import com.grosner.kpoet.`=`
import com.grosner.kpoet.`public static final field`
import com.grosner.kpoet.`return`
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.param
import com.grosner.kpoet.public
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Description: Used in writing ModelViewAdapters
 */
class ModelViewDefinition(modelView: ModelView,
                          manager: ProcessorManager, element: Element)
    : BaseTableDefinition(element, manager) {

    private val implementsLoadFromCursorListener: Boolean =
            typeElement.implementsClass(manager.processingEnvironment, ClassNames.LOAD_FROM_CURSOR_LISTENER)

    private var queryFieldName: String? = null

    override val methods: Array<MethodDefinition> = arrayOf(
            LoadFromCursorMethod(this),
            ExistenceMethod(this),
            PrimaryConditionMethod(this))

    val priority = modelView.priority

    override val associationalBehavior = AssociationalBehavior(
            name = if (modelView.name.isNullOrEmpty()) modelClassName else modelView.name,
            databaseTypeName = modelView.extractTypeNameFromAnnotation { it.database },
            allFields = modelView.allFields
    )

    override fun prepareForWrite() {
        classElementLookUpMap.clear()
        columnDefinitions.clear()
        queryFieldName = null

        val modelView = element.getAnnotation(ModelView::class.java)
        if (modelView != null) {
            databaseDefinition = manager.getDatabaseHolderDefinition(associationalBehavior.databaseTypeName)?.databaseDefinition
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

            val isValidAllFields = ElementUtility.isValidAllFields(associationalBehavior.allFields, variableElement)
            val isColumnMap = variableElement.annotation<ColumnMap>() != null

            if (variableElement.annotation<Column>() != null || isValidAllFields
                    || isColumnMap) {

                // package private, will generate helper
                val isPackagePrivate = ElementUtility.isPackagePrivate(variableElement)
                val isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, variableElement, this.element)

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
                    manager.logError("ModelView $elementName cannot have primary keys")
                }
            } else if (variableElement.annotation<ModelViewQuery>() != null) {
                if (!queryFieldName.isNullOrEmpty()) {
                    manager.logError("Found duplicate queryField name: $queryFieldName for $elementClassName")
                }

                val element = variableElement.toTypeErasedElement() as? ExecutableElement
                if (element != null) {
                    val returnElement = element.returnType.toTypeElement()
                    ensureVisibleStatic(element, typeElement, "ModelViewQuery")
                    if (!returnElement.implementsClass(manager.processingEnvironment, com.dbflow5.processor.ClassNames.QUERY)) {
                        manager.logError("The function ${variableElement.simpleName} must return ${com.dbflow5.processor.ClassNames.QUERY} from $elementName")
                    }
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
        get() = ParameterizedTypeName.get(com.dbflow5.processor.ClassNames.MODEL_VIEW_ADAPTER, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        typeBuilder.apply {
            `public static final field`(String::class, "VIEW_NAME") { `=`(associationalBehavior.name.S) }

            elementClassName?.let { elementClassName ->
                columnDefinitions.forEach { it.addPropertyDefinition(typeBuilder, elementClassName) }
            }

            this.writeConstructor()

            writeGetModelClass(typeBuilder, elementClassName)

            `override fun`(String::class, "getCreationQuery",
                    param(com.dbflow5.processor.ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)) {
                modifiers(public, final)
                `return`("\$T.\$L().getQuery()", elementClassName, queryFieldName)
            }
            `override fun`(String::class, "getViewName") {
                modifiers(public, final)
                `return`(associationalBehavior.name.S)
            }
        }

        methods.mapNotNull { it.methodSpec }
                .forEach { typeBuilder.addMethod(it) }
    }

}