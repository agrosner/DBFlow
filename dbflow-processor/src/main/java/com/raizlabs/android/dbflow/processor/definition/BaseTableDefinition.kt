package com.raizlabs.android.dbflow.processor.definition

import com.google.common.collect.Lists
import com.grosner.kpoet.`public static final`
import com.grosner.kpoet.`return`
import com.grosner.kpoet.param
import com.grosner.kpoet.statement
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.PackagePrivateScopeColumnAccessor
import com.raizlabs.android.dbflow.processor.utils.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Description: Used to write Models and ModelViews
 */
abstract class BaseTableDefinition(typeElement: Element, processorManager: ProcessorManager)
    : BaseDefinition(typeElement, processorManager) {

    var columnDefinitions: MutableList<ColumnDefinition>
        protected set

    var hasAutoIncrement: Boolean = false

    var hasRowID: Boolean = false

    var autoIncrementColumn: ColumnDefinition? = null

    var associatedTypeConverters: MutableMap<ClassName, MutableList<ColumnDefinition>> = HashMap()
    var globalTypeConverters: MutableMap<ClassName, MutableList<ColumnDefinition>> = HashMap()
    val packagePrivateList: MutableList<ColumnDefinition> =
            Lists.newArrayList<ColumnDefinition>()

    var orderedCursorLookUp: Boolean = false
    var assignDefaultValuesFromCursor = true

    var classElementLookUpMap: MutableMap<String, Element> = mutableMapOf()

    val modelClassName: String
    var databaseDefinition: DatabaseDefinition? = null

    init {
        this.modelClassName = typeElement.simpleName.toString()
        columnDefinitions = ArrayList<ColumnDefinition>()
    }

    protected abstract fun createColumnDefinitions(typeElement: TypeElement)

    abstract val primaryColumnDefinitions: List<ColumnDefinition>

    abstract fun prepareForWrite()

    val parameterClassName: TypeName?
        get() = elementClassName

    fun addColumnForCustomTypeConverter(columnDefinition: ColumnDefinition, typeConverterName: ClassName): String {
        var columnDefinitions: MutableList<ColumnDefinition>? = associatedTypeConverters[typeConverterName]
        if (columnDefinitions == null) {
            columnDefinitions = ArrayList<ColumnDefinition>()
            associatedTypeConverters.put(typeConverterName, columnDefinitions)
        }
        columnDefinitions.add(columnDefinition)

        return "typeConverter" + typeConverterName.simpleName()
    }

    fun addColumnForTypeConverter(columnDefinition: ColumnDefinition, typeConverterName: ClassName): String {
        var columnDefinitions: MutableList<ColumnDefinition>? = globalTypeConverters[typeConverterName]
        if (columnDefinitions == null) {
            columnDefinitions = ArrayList<ColumnDefinition>()
            globalTypeConverters.put(typeConverterName, columnDefinitions)
        }
        columnDefinitions.add(columnDefinition)

        return "global_typeConverter" + typeConverterName.simpleName()
    }


    @Throws(IOException::class)
    fun writePackageHelper(processingEnvironment: ProcessingEnvironment) {
        var count = 0

        if (!packagePrivateList.isEmpty()) {
            val classSeparator = databaseDefinition?.classSeparator
            val typeBuilder = TypeSpec.classBuilder("${elementClassName?.simpleName()}${classSeparator}Helper")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

            for (columnDefinition in packagePrivateList) {
                var helperClassName = "${columnDefinition.element.getPackage()}.${columnDefinition.element.enclosingElement.toClassName().simpleName()}${classSeparator}Helper"
                if (columnDefinition is ForeignKeyColumnDefinition) {
                    val tableDefinition: TableDefinition? = databaseDefinition?.objectHolder?.tableDefinitionMap?.get(columnDefinition.referencedTableClassName as TypeName)
                    if (tableDefinition != null) {
                        helperClassName = "${tableDefinition.element.getPackage()}.${ClassName.get(tableDefinition.element as TypeElement).simpleName()}${classSeparator}Helper"
                    }
                }
                val className = ElementUtility.getClassName(helperClassName, manager)

                if (className != null && PackagePrivateScopeColumnAccessor.containsColumn(className, columnDefinition.columnName)) {
                    typeBuilder.apply {
                        val samePackage = ElementUtility.isInSamePackage(manager, columnDefinition.element, this@BaseTableDefinition.element)
                        val methodName = columnDefinition.columnName.capitalizeFirstLetter()

                        `public static final`(columnDefinition.elementTypeName!!, "get$methodName",
                                param(elementTypeName!!, ModelUtils.variable)) {
                            if (samePackage) {
                                `return`("${ModelUtils.variable}.${columnDefinition.elementName}")
                            } else {
                                `return`("\$T.get$methodName(${ModelUtils.variable})", className)
                            }
                        }

                        `public static final`(TypeName.VOID, "set$methodName",
                                param(elementTypeName!!, ModelUtils.variable),
                                param(columnDefinition.elementTypeName!!, "var")) {
                            if (samePackage) {
                                statement("${ModelUtils.variable}.${columnDefinition.elementName} = var")
                            } else {
                                statement("\$T.set$methodName(${ModelUtils.variable}, var")
                            }
                        }
                    }

                    count++
                } else if (className == null) {
                    manager.logError(BaseTableDefinition::class, "Could not find classname for:" + helperClassName)
                }
            }

            // only write class if we have referenced fields.
            if (count > 0) {
                val javaFileBuilder = JavaFile.builder(packageName, typeBuilder.build())
                javaFileBuilder.build().writeTo(processingEnvironment.filer)
            }
        }
    }


}
