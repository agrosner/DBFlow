package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ModelView
import com.raizlabs.android.dbflow.annotation.ModelViewQuery
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorUtils
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.method.*
import com.raizlabs.android.dbflow.processor.handler.DatabaseHandler
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.ElementUtility
import com.raizlabs.android.dbflow.processor.utils.StringUtils
import com.raizlabs.android.dbflow.processor.validator.ColumnValidator
import com.squareup.javapoet.*
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException

/**
 * Description: Used in writing ModelViewAdapters
 */
class ModelViewDefinition(manager: ProcessorManager, element: Element) : BaseTableDefinition(element, manager), Comparable<ModelViewDefinition> {

    internal val implementsLoadFromCursorListener: Boolean

    var databaseName: TypeName? = null

    private var queryFieldName: String? = null

    private var name: String? = null

    private var modelReferenceClass: ClassName? = null

    private val methods: Array<MethodDefinition>

    var allFields: Boolean = false

    var priority: Int = 0

    init {

        val modelView = element.getAnnotation(ModelView::class.java)
        if (modelView != null) {
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

        var typeAdapterInterface: DeclaredType? = null
        val modelViewType = manager.typeUtils.getDeclaredType(
                manager.elements.getTypeElement(ClassNames.MODEL_VIEW.toString()),
                manager.typeUtils.getWildcardType(manager.elements.getTypeElement(ClassName.OBJECT.toString()).asType(), null))


        for (superType in manager.typeUtils.directSupertypes(element.asType())) {
            if (manager.typeUtils.isAssignable(superType, modelViewType)) {
                typeAdapterInterface = superType as DeclaredType
                break
            }
        }

        if (typeAdapterInterface != null) {
            val typeArguments = typeAdapterInterface.typeArguments
            modelReferenceClass = ClassName.get(manager.elements.getTypeElement(typeArguments[0].toString()))
        }

        if (element is TypeElement) {
            implementsLoadFromCursorListener = ProcessorUtils.implementsClass(manager.processingEnvironment,
                    ClassNames.LOAD_FROM_CURSOR_LISTENER.toString(), element)
        } else {
            implementsLoadFromCursorListener = false
        }

        methods = arrayOf(LoadFromCursorMethod(this), ExistenceMethod(this), PrimaryConditionMethod(this))
    }

    override fun prepareForWrite() {
        classElementLookUpMap.clear()
        columnDefinitions.clear()
        queryFieldName = null

        val modelView = element.getAnnotation(ModelView::class.java)
        if (modelView != null) {
            databaseDefinition = manager.getDatabaseHolderDefinition(databaseName).databaseDefinition
            setOutputClassName(databaseDefinition?.classSeparator + DBFLOW_MODEL_VIEW_TAG)

            typeElement?.let { createColumnDefinitions(it) }
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

            if (variableElement.getAnnotation(Column::class.java) != null || isValidAllFields) {

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
            } else if (variableElement.getAnnotation(ModelViewQuery::class.java) != null) {
                if (!StringUtils.isNullOrEmpty(queryFieldName)) {
                    manager.logError("Found duplicate ")
                }
                if (!variableElement.modifiers.contains(Modifier.PUBLIC)) {
                    manager.logError("The ModelViewQuery must be public")
                }
                if (!variableElement.modifiers.contains(Modifier.STATIC)) {
                    manager.logError("The ModelViewQuery must be static")
                }

                if (!variableElement.modifiers.contains(Modifier.FINAL)) {
                    manager.logError("The ModelViewQuery must be final")
                }

                val element = manager.elements.getTypeElement(variableElement.asType().toString())
                if (!ProcessorUtils.implementsClass(manager.processingEnvironment, ClassNames.QUERY.toString(), element)) {
                    manager.logError("The field %1s must implement %1s", variableElement.simpleName.toString(), ClassNames.QUERY.toString())
                }

                queryFieldName = variableElement.simpleName.toString()
            }
        }

        if (StringUtils.isNullOrEmpty(queryFieldName)) {
            manager.logError("%1s is missing the @ModelViewQuery field.", elementClassName)
        }
    }

    override val primaryColumnDefinitions: List<ColumnDefinition>
        get() = columnDefinitions

    override val propertyClassName: ClassName
        get() = outputClassName

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(ClassNames.MODEL_VIEW_ADAPTER, modelReferenceClass, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {

        typeBuilder.addField(FieldSpec.builder(ClassName.get(String::class.java),
                "VIEW_NAME", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer("\$S", name!!).build())
        elementClassName?.let {
            columnDefinitions.forEach {
                columnDefinition ->
                columnDefinition.addPropertyDefinition(typeBuilder, it)
            }
        }

        val customTypeConverterPropertyMethod = CustomTypeConverterPropertyMethod(this)
        customTypeConverterPropertyMethod.addToType(typeBuilder)

        val constructorCode = CodeBlock.builder()
        constructorCode.addStatement("super(databaseDefinition)")
        customTypeConverterPropertyMethod.addCode(constructorCode)

        typeBuilder.addMethod(MethodSpec.constructorBuilder().addParameter(ClassNames.DATABASE_HOLDER, "holder").addParameter(ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME, "databaseDefinition").addCode(constructorCode.build()).addModifiers(Modifier.PUBLIC).build())

        for (method in methods) {
            val methodSpec = method.methodSpec
            if (methodSpec != null) {
                typeBuilder.addMethod(methodSpec)
            }
        }

        InternalAdapterHelper.writeGetModelClass(typeBuilder, elementClassName)

        typeBuilder.addMethod(MethodSpec.methodBuilder("getCreationQuery")
                .addAnnotation(Override::class.java)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return \$T.\$L.getQuery()", elementClassName, queryFieldName)
                .returns(ClassName.get(String::class.java)).build())

        typeBuilder.addMethod(MethodSpec.methodBuilder("getViewName")
                .addAnnotation(Override::class.java)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return \$S", name!!)
                .returns(ClassName.get(String::class.java)).build())

        typeBuilder.addMethod(MethodSpec.methodBuilder("newInstance")
                .addAnnotation(Override::class.java)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return new \$T()", elementClassName)
                .returns(elementClassName).build())
    }

    override fun compareTo(other: ModelViewDefinition): Int {
        return Integer.valueOf(priority)!!.compareTo(other.priority)
    }

    companion object {

        private val DBFLOW_MODEL_VIEW_TAG = "ViewTable"
    }
}
