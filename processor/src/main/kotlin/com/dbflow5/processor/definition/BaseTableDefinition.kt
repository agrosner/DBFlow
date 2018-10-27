package com.dbflow5.processor.definition

import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.definition.column.PackagePrivateScopeColumnAccessor
import com.dbflow5.processor.definition.column.ReferenceColumnDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.ModelUtils
import com.dbflow5.processor.utils.`override fun`
import com.dbflow5.processor.utils.getPackage
import com.dbflow5.processor.utils.toClassName
import com.grosner.kpoet.`public static final`
import com.grosner.kpoet.`return`
import com.grosner.kpoet.code
import com.grosner.kpoet.constructor
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.param
import com.grosner.kpoet.public
import com.grosner.kpoet.statement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.io.IOException
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

    val sqlColumnDefinitions
        get() = columnDefinitions.filter { it.type !is ColumnDefinition.Type.RowId }

    var hasAutoIncrement: Boolean = false

    var hasRowID: Boolean = false

    var autoIncrementColumn: ColumnDefinition? = null

    var associatedTypeConverters = hashMapOf<ClassName, MutableList<ColumnDefinition>>()
    var globalTypeConverters = hashMapOf<ClassName, MutableList<ColumnDefinition>>()
    val packagePrivateList = arrayListOf<ColumnDefinition>()

    var orderedCursorLookUp: Boolean = false
    var assignDefaultValuesFromCursor = true

    var classElementLookUpMap: MutableMap<String, Element> = mutableMapOf()

    val modelClassName = typeElement.simpleName.toString()
    var databaseDefinition: DatabaseDefinition? = null

    var databaseTypeName: TypeName? = null

    val hasGlobalTypeConverters
        get() = globalTypeConverters.isNotEmpty()

    init {
        columnDefinitions = arrayListOf()
    }

    protected abstract fun createColumnDefinitions(typeElement: TypeElement)

    abstract val primaryColumnDefinitions: List<ColumnDefinition>

    abstract fun prepareForWrite()

    val parameterClassName: TypeName?
        get() = elementClassName

    fun addColumnForCustomTypeConverter(columnDefinition: ColumnDefinition, typeConverterName: ClassName): String {
        val columnDefinitions = associatedTypeConverters.getOrPut(typeConverterName) { arrayListOf() }
        columnDefinitions.add(columnDefinition)
        return "typeConverter${typeConverterName.simpleName()}"
    }

    fun addColumnForTypeConverter(columnDefinition: ColumnDefinition, typeConverterName: ClassName): String {
        val columnDefinitions = globalTypeConverters.getOrPut(typeConverterName) { arrayListOf() }
        columnDefinitions.add(columnDefinition)
        return "global_typeConverter${typeConverterName.simpleName()}"
    }

    fun TypeSpec.Builder.writeConstructor() {
        val customTypeConverterPropertyMethod = CustomTypeConverterPropertyMethod(this@BaseTableDefinition)
        customTypeConverterPropertyMethod.addToType(this)

        constructor {
            if (hasGlobalTypeConverters) {
                addParameter(param(ClassNames.DATABASE_HOLDER, "holder").build())
            }
            addParameter(param(ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME, "databaseDefinition").build())
            modifiers(public)
            statement("super(databaseDefinition)")
            code {
                customTypeConverterPropertyMethod.addCode(this)
            }
        }
    }

    fun writeGetModelClass(typeBuilder: TypeSpec.Builder, modelClassName: ClassName?) = typeBuilder.apply {
        `override fun`(ParameterizedTypeName.get(ClassName.get(Class::class.java), modelClassName), "getTable") {
            modifiers(public, final)
            `return`("\$T.class", modelClassName)
        }
    }

    @Throws(IOException::class)
    fun writePackageHelper(processingEnvironment: ProcessingEnvironment) {
        var count = 0

        if (!packagePrivateList.isEmpty()) {
            val classSeparator = databaseDefinition?.classSeparator
            val typeBuilder = TypeSpec.classBuilder("${elementClassName?.simpleName()}${classSeparator}Helper")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

            for (columnDefinition in packagePrivateList) {
                var helperClassName = "${columnDefinition.element.getPackage()}.${columnDefinition.element.enclosingElement.toClassName()?.simpleName()}${classSeparator}Helper"
                if (columnDefinition is ReferenceColumnDefinition) {
                    val tableDefinition: TableDefinition? = databaseDefinition?.objectHolder?.tableDefinitionMap?.get(columnDefinition.referencedClassName as TypeName)
                    if (tableDefinition != null) {
                        helperClassName = "${tableDefinition.element.getPackage()}.${ClassName.get(tableDefinition.element as TypeElement).simpleName()}${classSeparator}Helper"
                    }
                }
                val className = ElementUtility.getClassName(helperClassName, manager)

                if (className != null && PackagePrivateScopeColumnAccessor.containsColumn(className, columnDefinition.columnName)) {
                    typeBuilder.apply {
                        val samePackage = ElementUtility.isInSamePackage(manager, columnDefinition.element, this@BaseTableDefinition.element)
                        val methodName = columnDefinition.columnName.capitalize()

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
                                statement("\$T.set$methodName(${ModelUtils.variable}, var)", className)
                            }
                        }
                    }

                    count++
                } else if (className == null) {
                    manager.logError(BaseTableDefinition::class, "Could not find classname for: $helperClassName")
                }
            }

            // only write class if we have referenced fields.
            if (count > 0) {
                val javaFileBuilder = JavaFile.builder(packageName, typeBuilder.build())
                javaFileBuilder.build().writeTo(processingEnvironment.filer)
            }
        }
    }

    /**
     * Do not support inheritance on package private fields without having ability to generate code for it in
     * same package.
     */
    internal fun checkInheritancePackagePrivate(isPackagePrivateNotInSamePackage: Boolean, element: Element): Boolean {
        if (isPackagePrivateNotInSamePackage && !manager.elementBelongsInTable(element)) {
            manager.logError("Package private inheritance on non-table/querymodel/view " +
                    "is not supported without a @InheritedColumn annotation." +
                    " Make $element from ${element.enclosingElement} public or private.")
            return true
        }
        return false
    }

}
