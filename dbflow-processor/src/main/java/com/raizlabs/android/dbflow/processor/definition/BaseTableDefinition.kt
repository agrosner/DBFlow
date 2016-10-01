package com.raizlabs.android.dbflow.processor.definition

import com.google.common.collect.Lists
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.PackagePrivateAccess
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.ElementUtility
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.processor.utils.StringUtils
import com.squareup.javapoet.*
import java.io.IOException
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Description: Used to write Models and ModelViews
 */
abstract class BaseTableDefinition(typeElement: Element, processorManager: ProcessorManager) : BaseDefinition(typeElement, processorManager) {

    var columnDefinitions: MutableList<ColumnDefinition>
        protected set

    protected var associatedTypeConverters: MutableMap<ClassName, MutableList<ColumnDefinition>> = HashMap()
    protected var globalTypeConverters: MutableMap<ClassName, MutableList<ColumnDefinition>> = HashMap()
    protected val packagePrivateList: MutableList<ColumnDefinition> =
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

    abstract val propertyClassName: ClassName

    abstract fun prepareForWrite()

    val parameterClassName: TypeName
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
            val typeBuilder = TypeSpec.classBuilder(elementClassName.simpleName() + databaseDefinition!!.classSeparator + "Helper").addModifiers(Modifier.PUBLIC, Modifier.FINAL)

            for (columnDefinition in packagePrivateList) {
                var helperClassName = manager.elements.getPackageOf(columnDefinition.element).toString() + "." + ClassName.get(columnDefinition.element.enclosingElement as TypeElement).simpleName()
                databaseDefinition!!.classSeparator + "Helper"
                if (columnDefinition is ForeignKeyColumnDefinition) {
                    val tableDefinition = databaseDefinition!!.holderDefinition.tableDefinitionMap[columnDefinition.referencedTableClassName]
                    if (tableDefinition != null) {
                        helperClassName = manager.elements.getPackageOf(tableDefinition.element).toString() + "." + ClassName.get(tableDefinition.element as TypeElement).simpleName()
                        databaseDefinition!!.classSeparator + "Helper"
                    }
                }
                val className = ClassName.bestGuess(helperClassName)

                if (PackagePrivateAccess.containsColumn(className, columnDefinition.columnName)) {

                    var method: MethodSpec.Builder = MethodSpec.methodBuilder("get" + StringUtils.capitalize(columnDefinition.columnName)).addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).addParameter(elementTypeName, ModelUtils.getVariable()).returns(columnDefinition.elementTypeName)
                    val samePackage = ElementUtility.isInSamePackage(manager, columnDefinition.element, this.element)

                    if (samePackage) {
                        method.addStatement("return \$L.\$L", ModelUtils.getVariable(), columnDefinition.elementName)
                    } else {
                        method.addStatement("return \$T.get\$L(\$L)", className,
                                StringUtils.capitalize(columnDefinition.columnName),
                                ModelUtils.getVariable())
                    }

                    typeBuilder.addMethod(method.build())

                    method = MethodSpec.methodBuilder("set" + StringUtils.capitalize(columnDefinition.columnName))
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .addParameter(elementTypeName, ModelUtils.getVariable())
                            .addParameter(columnDefinition.elementTypeName, "var")

                    if (samePackage) {
                        method.addStatement("\$L.\$L = \$L", ModelUtils.getVariable(),
                                columnDefinition.elementName, "var")
                    } else {

                        method.addStatement("\$T.set\$L(\$L, \$L)", className,
                                StringUtils.capitalize(columnDefinition.columnName),
                                ModelUtils.getVariable(), "var")
                    }
                    typeBuilder.addMethod(method.build())
                    count++
                }
            }

            // only write class if we have referenced fields.
            if (count > 0) {
                val javaFileBuilder = JavaFile.builder(packageName, typeBuilder.build())
                javaFileBuilder.build().writeTo(processingEnvironment.filer)
            }
        }
    }


    open fun hasAutoIncrement(): Boolean {
        return false
    }

    open fun hasRowID(): Boolean {
        return false
    }

    open val autoIncrementColumn: ColumnDefinition?
        get() = null

}
