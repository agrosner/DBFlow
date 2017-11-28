package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.`public static final`
import com.grosner.kpoet.`return`
import com.grosner.kpoet.code
import com.grosner.kpoet.constructor
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.param
import com.grosner.kpoet.public
import com.grosner.kpoet.statement
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.PackagePrivateScopeColumnAccessor
import com.raizlabs.android.dbflow.processor.definition.column.ReferenceColumnDefinition
import com.raizlabs.android.dbflow.processor.utils.ElementUtility
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.processor.utils.`override fun`
import com.raizlabs.android.dbflow.processor.utils.getPackage
import com.raizlabs.android.dbflow.processor.utils.toClassName
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

    fun writeConstructor(typeBuilder: TypeSpec.Builder) {
        typeBuilder.apply {
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
    }

    fun writeGetModelClass(typeBuilder: TypeSpec.Builder, modelClassName: ClassName?) = typeBuilder.apply {
        `override fun`(ParameterizedTypeName.get(ClassName.get(Class::class.java), modelClassName), "getModelClass") {
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
