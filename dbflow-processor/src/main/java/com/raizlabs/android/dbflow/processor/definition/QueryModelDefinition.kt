package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorUtils
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.CustomTypeConverterPropertyMethod
import com.raizlabs.android.dbflow.processor.definition.LoadFromCursorMethod
import com.raizlabs.android.dbflow.processor.definition.MethodDefinition
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.ElementUtility
import com.raizlabs.android.dbflow.processor.ColumnValidator
import com.squareup.javapoet.*
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

/**
 * Description:
 */
class QueryModelDefinition(typeElement: Element, processorManager: ProcessorManager)
: BaseTableDefinition(typeElement, processorManager) {

    var databaseTypeName: TypeName? = null

    var allFields: Boolean = false

    var implementsLoadFromCursorListener = false

    internal var methods: Array<MethodDefinition>

    init {

        val queryModel = typeElement.getAnnotation(QueryModel::class.java)
        if (queryModel != null) {
            try {
                queryModel.database
            } catch (mte: MirroredTypeException) {
                databaseTypeName = TypeName.get(mte.typeMirror)
            }

        }

        elementClassName?.let { databaseTypeName?.let { it1 -> processorManager.addModelToDatabase(it, it1) } }

        if (element is TypeElement) {
            implementsLoadFromCursorListener = ProcessorUtils.implementsClass(manager.processingEnvironment, ClassNames.LOAD_FROM_CURSOR_LISTENER.toString(),
                    element as TypeElement)
        }


        methods = arrayOf<MethodDefinition>(LoadFromCursorMethod(this))

    }

    override fun prepareForWrite() {
        classElementLookUpMap.clear()
        columnDefinitions.clear()
        packagePrivateList.clear()

        val queryModel = typeElement?.getAnnotation(QueryModel::class.java)
        if (queryModel != null) {
            databaseDefinition = manager.getDatabaseHolderDefinition(databaseTypeName)?.databaseDefinition
            setOutputClassName(databaseDefinition?.classSeparator + DBFLOW_QUERY_MODEL_TAG)
            allFields = queryModel.allFields

            typeElement?.let { createColumnDefinitions(it) }
        }
    }

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(ClassNames.QUERY_MODEL_ADAPTER, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        elementClassName?.let { className -> columnDefinitions.forEach { it.addPropertyDefinition(typeBuilder, className) } }

        val customTypeConverterPropertyMethod = CustomTypeConverterPropertyMethod(this)
        customTypeConverterPropertyMethod.addToType(typeBuilder)

        val constructorCode = CodeBlock.builder()
        constructorCode.addStatement("super(databaseDefinition)")
        customTypeConverterPropertyMethod.addCode(constructorCode)

        InternalAdapterHelper.writeGetModelClass(typeBuilder, elementClassName)

        typeBuilder.addMethod(MethodSpec.constructorBuilder().addParameter(ClassNames.DATABASE_HOLDER, "holder").addParameter(ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME, "databaseDefinition").addCode(constructorCode.build()).addModifiers(Modifier.PUBLIC).build())

        for (method in methods) {
            val methodSpec = method.methodSpec
            if (methodSpec != null) {
                typeBuilder.addMethod(methodSpec)
            }
        }

        typeBuilder.addMethod(MethodSpec.methodBuilder("newInstance")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(elementClassName)
                .addStatement("return new \$T()", elementClassName).build())
    }

    override fun createColumnDefinitions(typeElement: TypeElement) {
        val variableElements = ElementUtility.getAllElements(typeElement, manager)

        for (element in variableElements) {
            classElementLookUpMap.put(element.simpleName.toString(), element)
        }

        val columnValidator = ColumnValidator()
        for (variableElement in variableElements) {

            // no private static or final fields
            val isAllFields = ElementUtility.isValidAllFields(allFields, element)
            // package private, will generate helper
            val isPackagePrivate = ElementUtility.isPackagePrivate(element)
            val isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, element, this.element)

            if (variableElement.getAnnotation(Column::class.java) != null || isAllFields) {

                val columnDefinition = ColumnDefinition(manager, variableElement, this, isPackagePrivateNotInSamePackage)
                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition)

                    if (isPackagePrivate) {
                        packagePrivateList.add(columnDefinition)
                    }
                }
            }
        }
    }

    override // Shouldn't include any
    val primaryColumnDefinitions: List<ColumnDefinition>
        get() = ArrayList()

    override val propertyClassName: ClassName
        get() = outputClassName

    companion object {

        private val DBFLOW_QUERY_MODEL_TAG = "QueryTable"
    }

}
