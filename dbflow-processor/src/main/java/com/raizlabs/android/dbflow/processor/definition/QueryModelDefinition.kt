package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.*
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ColumnValidator
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.ProcessorUtils
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.utils.ElementUtility
import com.raizlabs.android.dbflow.processor.utils.`override fun`
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



        InternalAdapterHelper.writeGetModelClass(typeBuilder, elementClassName)

        typeBuilder.constructor(param(ClassNames.DATABASE_HOLDER, "holder"),
            param(ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME, "databaseDefinition")) {
            modifiers(public)
            statement("super(databaseDefinition)")
            code {
                customTypeConverterPropertyMethod.addCode(this)
            }
        }

        methods.mapNotNull { it.methodSpec }
            .forEach { typeBuilder.addMethod(it) }

        typeBuilder.apply {
            `override fun`(elementClassName!!, "newInstance") {
                modifiers(public, final)
                `return`("new \$T()", elementClassName)
            }
        }
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

    companion object {

        private val DBFLOW_QUERY_MODEL_TAG = "QueryTable"
    }

}
