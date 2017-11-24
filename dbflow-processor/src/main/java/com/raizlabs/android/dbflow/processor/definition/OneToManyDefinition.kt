package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.`for`
import com.grosner.kpoet.`if`
import com.grosner.kpoet.end
import com.grosner.kpoet.statement
import com.grosner.kpoet.typeName
import com.raizlabs.android.dbflow.annotation.OneToMany
import com.raizlabs.android.dbflow.annotation.OneToManyMethod
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.definition.column.ColumnAccessor
import com.raizlabs.android.dbflow.processor.definition.column.GetterSetter
import com.raizlabs.android.dbflow.processor.definition.column.PrivateScopeColumnAccessor
import com.raizlabs.android.dbflow.processor.definition.column.VisibleScopeColumnAccessor
import com.raizlabs.android.dbflow.processor.definition.column.modelBlock
import com.raizlabs.android.dbflow.processor.definition.column.wrapperCommaIfBaseModel
import com.raizlabs.android.dbflow.processor.definition.column.wrapperIfBaseModel
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.processor.utils.annotation
import com.raizlabs.android.dbflow.processor.utils.isSubclass
import com.raizlabs.android.dbflow.processor.utils.simpleString
import com.raizlabs.android.dbflow.processor.utils.statement
import com.raizlabs.android.dbflow.processor.utils.toTypeElement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.WildcardTypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Description: Represents the [OneToMany] annotation.
 */
class OneToManyDefinition(executableElement: ExecutableElement,
                          processorManager: ProcessorManager,
                          parentElements: Collection<Element>) : BaseDefinition(executableElement, processorManager) {

    private var _methodName: String

    private var _variableName: String

    var methods = mutableListOf<OneToManyMethod>()

    val isLoad
        get() = isAll || methods.contains(OneToManyMethod.LOAD)

    val isAll
        get() = methods.contains(OneToManyMethod.ALL)

    val isDelete: Boolean
        get() = isAll || methods.contains(OneToManyMethod.DELETE)

    val isSave: Boolean
        get() = isAll || methods.contains(OneToManyMethod.SAVE)

    var referencedTableType: TypeName? = null
    var hasWrapper = false

    private var columnAccessor: ColumnAccessor
    private var extendsModel = false
    private var referencedType: TypeElement? = null

    private var efficientCodeMethods = false

    init {

        val oneToMany = executableElement.annotation<OneToMany>()!!

        efficientCodeMethods = oneToMany.efficientMethods

        _methodName = executableElement.simpleName.toString()
        _variableName = oneToMany.variableName
        if (_variableName.isEmpty()) {
            _variableName = _methodName.replace("get", "")
            _variableName = _variableName.substring(0, 1).toLowerCase() + _variableName.substring(1)
        }

        val privateAccessor = PrivateScopeColumnAccessor(_variableName, object : GetterSetter {
            override val getterName: String = ""
            override val setterName: String = ""
        }, optionalGetterParam = if (hasWrapper) ModelUtils.wrapper else "")

        var isVariablePrivate = false
        val referencedElement = parentElements.firstOrNull { it.simpleString == _variableName }
        if (referencedElement == null) {
            // check on setter. if setter exists, we can reference it safely since a getter has already been defined.
            if (!parentElements.any { it.simpleString == privateAccessor.setterNameElement }) {
                manager.logError(OneToManyDefinition::class,
                        "@OneToMany definition $elementName Cannot find referenced variable $_variableName.")
            } else {
                isVariablePrivate = true
            }
        } else {
            isVariablePrivate = referencedElement.modifiers.contains(Modifier.PRIVATE)
        }

        methods.addAll(oneToMany.oneToManyMethods)

        val parameters = executableElement.parameters
        if (parameters.isNotEmpty()) {
            if (parameters.size > 1) {
                manager.logError(OneToManyDefinition::class, "OneToMany Methods can only have one parameter and that be the DatabaseWrapper.")
            } else {
                val param = parameters[0]
                val name = param.asType().typeName
                if (name == ClassNames.DATABASE_WRAPPER) {
                    hasWrapper = true
                } else {
                    manager.logError(OneToManyDefinition::class, "OneToMany Methods can only specify a ${ClassNames.DATABASE_WRAPPER} as its parameter.")
                }
            }
        }

        if (isVariablePrivate) {
            columnAccessor = privateAccessor
        } else {
            columnAccessor = VisibleScopeColumnAccessor(_variableName)
        }

        val returnType = executableElement.returnType
        val typeName = TypeName.get(returnType)
        if (typeName is ParameterizedTypeName) {
            val typeArguments = typeName.typeArguments
            if (typeArguments.size == 1) {
                var refTableType = typeArguments[0]
                if (refTableType is WildcardTypeName) {
                    refTableType = refTableType.upperBounds[0]
                }
                referencedTableType = refTableType

                referencedType = referencedTableType.toTypeElement(manager)
                extendsModel = referencedType.isSubclass(manager.processingEnvironment, ClassNames.MODEL)
            }
        }

    }

    private val methodName = "${ModelUtils.variable}.$_methodName(${wrapperIfBaseModel(hasWrapper)})"

    /**
     * Writes the method to the specified builder for loading from DB.
     */
    fun writeLoad(codeBuilder: CodeBlock.Builder) {
        if (isLoad) {
            codeBuilder.addStatement(methodName)
        }
    }

    /**
     * Writes a delete method that will delete all related objects.
     */
    fun writeDelete(method: MethodSpec.Builder) {
        if (isDelete) {
            writeLoopWithMethod(method, "delete")
            method.statement(columnAccessor.set(CodeBlock.of("null"), modelBlock))
        }
    }

    fun writeSave(codeBuilder: MethodSpec.Builder) {
        if (isSave) writeLoopWithMethod(codeBuilder, "save")
    }

    fun writeUpdate(codeBuilder: MethodSpec.Builder) {
        if (isSave) writeLoopWithMethod(codeBuilder, "update")
    }

    fun writeInsert(codeBuilder: MethodSpec.Builder) {
        if (isSave) writeLoopWithMethod(codeBuilder, "insert")
    }

    private fun writeLoopWithMethod(codeBuilder: MethodSpec.Builder, methodName: String) {
        val oneToManyMethodName = this@OneToManyDefinition.methodName
        codeBuilder.apply {
            `if`("$oneToManyMethodName != null") {
                // need to load adapter for non-model classes
                if (!extendsModel || efficientCodeMethods) {
                    statement("\$T adapter = \$T.getModelAdapter(\$T.class)",
                            ParameterizedTypeName.get(ClassNames.MODEL_ADAPTER, referencedTableType),
                            ClassNames.FLOW_MANAGER, referencedTableType)
                }

                if (efficientCodeMethods) {
                    statement("adapter.${methodName}All($oneToManyMethodName${wrapperCommaIfBaseModel(true)})")
                } else {
                    `for`("\$T value: $oneToManyMethodName", ClassName.get(referencedType)) {
                        if (!extendsModel) {
                            statement("adapter.$methodName(value${wrapperCommaIfBaseModel(true)})")
                        } else {
                            statement("value.$methodName(${wrapperIfBaseModel(true)})")
                        }
                        this
                    }
                }
            }
        }.end()
    }
}

