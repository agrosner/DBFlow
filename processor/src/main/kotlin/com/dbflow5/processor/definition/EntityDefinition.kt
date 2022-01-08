package com.dbflow5.processor.definition

import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.behavior.AssociationalBehavior
import com.dbflow5.processor.definition.behavior.CursorHandlingBehavior
import com.dbflow5.processor.definition.behavior.PrimaryKeyColumnBehavior
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.definition.column.PackagePrivateScopeColumnAccessor
import com.dbflow5.processor.definition.column.ReferenceColumnDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.ModelUtils
import com.dbflow5.processor.utils.`override fun`
import com.dbflow5.processor.utils.getPackage
import com.dbflow5.processor.utils.implementsClass
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
import javax.annotation.processing.FilerException
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

fun Collection<EntityDefinition>.safeWritePackageHelper(processorManager: ProcessorManager) =
    forEach {
        try {
            it.writePackageHelper(processorManager.processingEnvironment)
        } catch (e: FilerException) { /*Ignored intentionally to allow multi-round table generation*/
        }
    }

/**
 * Description: Used to write Models and ModelViews
 */
abstract class EntityDefinition(typeElement: TypeElement, processorManager: ProcessorManager) :
    BaseDefinition(typeElement, processorManager) {

    var columnDefinitions: MutableList<ColumnDefinition> = arrayListOf()
        protected set

    val sqlColumnDefinitions
        get() = columnDefinitions.filter { it.type !is ColumnDefinition.Type.RowId }

    val associatedTypeConverters = hashMapOf<ClassName, MutableList<ColumnDefinition>>()
    val globalTypeConverters = hashMapOf<ClassName, MutableList<ColumnDefinition>>()
    val packagePrivateList = arrayListOf<ColumnDefinition>()

    val classElementLookUpMap: MutableMap<String, Element> = mutableMapOf()

    val modelClassName = typeElement.simpleName.toString()
    val databaseDefinition: DatabaseDefinition by lazy {
        manager.getDatabaseHolderDefinition(associationalBehavior.databaseTypeName)?.databaseDefinition
            ?: throw RuntimeException(
                "DatabaseDefinition not found for DB element named ${associationalBehavior.name}" +
                    " for db type: ${associationalBehavior.databaseTypeName}."
            )
    }

    val hasGlobalTypeConverters
        get() = globalTypeConverters.isNotEmpty()

    val implementsLoadFromCursorListener = typeElement.implementsClass(
        manager.processingEnvironment,
        ClassNames.LOAD_FROM_CURSOR_LISTENER
    )

    abstract val associationalBehavior: AssociationalBehavior
    abstract val cursorHandlingBehavior: CursorHandlingBehavior
    open var primaryKeyColumnBehavior: PrimaryKeyColumnBehavior =
        PrimaryKeyColumnBehavior(
            hasRowID = false,
            associatedColumn = null,
            hasAutoIncrement = false
        )

    abstract val methods: Array<MethodDefinition>

    protected abstract fun createColumnDefinitions(typeElement: TypeElement)

    abstract val primaryColumnDefinitions: List<ColumnDefinition>

    fun prepareForWrite() {
        classElementLookUpMap.clear()
        columnDefinitions.clear()
        packagePrivateList.clear()

        prepareForWriteInternal()
    }

    protected abstract fun prepareForWriteInternal()

    val parameterClassName: TypeName?
        get() = elementClassName

    fun addColumnForCustomTypeConverter(
        columnDefinition: ColumnDefinition,
        typeConverterName: ClassName
    ): String {
        val columnDefinitions =
            associatedTypeConverters.getOrPut(typeConverterName) { arrayListOf() }
        columnDefinitions.add(columnDefinition)
        return "typeConverter${typeConverterName.simpleName()}"
    }

    fun addColumnForTypeConverter(
        columnDefinition: ColumnDefinition,
        typeConverterName: ClassName
    ): String {
        val columnDefinitions = globalTypeConverters.getOrPut(typeConverterName) { arrayListOf() }
        columnDefinitions.add(columnDefinition)
        return "global_typeConverter${typeConverterName.simpleName()}"
    }

    fun TypeSpec.Builder.writeConstructor() {
        val customTypeConverterPropertyMethod =
            CustomTypeConverterPropertyMethod(this@EntityDefinition)
        customTypeConverterPropertyMethod.addToType(this)

        constructor {
            if (hasGlobalTypeConverters) {
                addParameter(param(ClassNames.DATABASE_HOLDER, "holder").build())
            }
            addParameter(
                param(
                    ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME,
                    "databaseDefinition"
                ).build()
            )
            modifiers(public)
            statement("super(databaseDefinition)")
            code {
                customTypeConverterPropertyMethod.addCode(this)
            }
        }
    }

    fun writeGetModelClass(typeBuilder: TypeSpec.Builder, modelClassName: ClassName?) =
        typeBuilder.apply {
            `override fun`(
                ParameterizedTypeName.get(
                    ClassName.get(KClass::class.java),
                    modelClassName
                ), "getTable"
            ) {
                modifiers(public, final)
                `return`(
                    "\$T.getKotlinClass(\$T.class)",
                    ClassNames.JVM_CLASS_MAPPING,
                    modelClassName
                )
            }
        }

    @Throws(IOException::class)
    fun writePackageHelper(processingEnvironment: ProcessingEnvironment) {
        var count = 0

        if (!packagePrivateList.isEmpty()) {
            val typeBuilder = TypeSpec.classBuilder("${elementClassName?.simpleName()}_Helper")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

            for (columnDefinition in packagePrivateList) {
                var helperClassName = "${columnDefinition.element.getPackage()}.${
                    columnDefinition.element.enclosingElement.toClassName()?.simpleName()
                }_Helper"
                if (columnDefinition is ReferenceColumnDefinition) {
                    val tableDefinition: TableDefinition? =
                        databaseDefinition.objectHolder?.tableDefinitionMap?.get(columnDefinition.referencedClassName as TypeName)
                    if (tableDefinition != null) {
                        helperClassName = "${tableDefinition.element.getPackage()}.${
                            ClassName.get(tableDefinition.element as TypeElement).simpleName()
                        }_Helper"
                    }
                }
                val className = ElementUtility.getClassName(helperClassName, manager)

                if (className != null && PackagePrivateScopeColumnAccessor.containsColumn(
                        className,
                        columnDefinition.columnName
                    )
                ) {
                    typeBuilder.apply {
                        val samePackage = ElementUtility.isInSamePackage(
                            manager,
                            columnDefinition.element,
                            this@EntityDefinition.element
                        )
                        val methodName = columnDefinition.columnName.capitalize()

                        `public static final`(
                            columnDefinition.elementTypeName!!, "get$methodName",
                            param(elementTypeName!!, ModelUtils.variable)
                        ) {
                            if (samePackage) {
                                `return`("${ModelUtils.variable}.${columnDefinition.elementName}")
                            } else {
                                `return`("\$T.get$methodName(${ModelUtils.variable})", className)
                            }
                        }

                        `public static final`(
                            TypeName.VOID, "set$methodName",
                            param(elementTypeName, ModelUtils.variable),
                            param(columnDefinition.elementTypeName, "var")
                        ) {
                            if (samePackage) {
                                statement("${ModelUtils.variable}.${columnDefinition.elementName} = var")
                            } else {
                                statement(
                                    "\$T.set$methodName(${ModelUtils.variable}, var)",
                                    className
                                )
                            }
                        }
                    }

                    count++
                } else if (className == null) {
                    manager.logError(
                        EntityDefinition::class,
                        "Could not find classname for: $helperClassName"
                    )
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
    internal fun checkInheritancePackagePrivate(
        isPackagePrivateNotInSamePackage: Boolean,
        element: Element
    ): Boolean {
        if (isPackagePrivateNotInSamePackage && !manager.elementBelongsInTable(element)) {
            manager.logError(
                "Package private inheritance on non-table/querymodel/view " +
                    "is not supported without a @InheritedColumn annotation." +
                    " Make $element from ${element.enclosingElement} public or private."
            )
            return true
        }
        return false
    }

}
