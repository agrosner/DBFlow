package com.dbflow5.processor.definition

import com.grosner.kpoet.`for`
import com.grosner.kpoet.`if`
import com.grosner.kpoet.end
import com.grosner.kpoet.statement
import com.grosner.kpoet.typeName
import com.dbflow5.annotation.OneToMany
import com.dbflow5.annotation.OneToManyMethod
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.column.ColumnAccessor
import com.dbflow5.processor.definition.column.GetterSetter
import com.dbflow5.processor.definition.column.PrivateScopeColumnAccessor
import com.dbflow5.processor.definition.column.VisibleScopeColumnAccessor
import com.dbflow5.processor.definition.column.modelBlock
import com.dbflow5.processor.definition.column.wrapperCommaIfBaseModel
import com.dbflow5.processor.definition.column.wrapperIfBaseModel
import com.dbflow5.processor.utils.ModelUtils
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.isSubclass
import com.dbflow5.processor.utils.simpleString
import com.dbflow5.processor.utils.statement
import com.dbflow5.processor.utils.toTypeElement
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

    var variableName: String

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
        variableName = oneToMany.variableName
        if (variableName.isEmpty()) {
            variableName = _methodName.replace("get", "")
            variableName = variableName.substring(0, 1).toLowerCase() + variableName.substring(1)
        }

        val privateAccessor = PrivateScopeColumnAccessor(variableName, object : GetterSetter {
            override val getterName: String = ""
            override val setterName: String = ""
        }, optionalGetterParam = if (hasWrapper) ModelUtils.wrapper else "")

        var isVariablePrivate = false
        val referencedElement = parentElements.firstOrNull { it.simpleString == variableName }
        if (referencedElement == null) {
            // check on setter. if setter exists, we can reference it safely since a getter has already been defined.
            if (!parentElements.any { it.simpleString == privateAccessor.setterNameElement }) {
                manager.logError(OneToManyDefinition::class,
                    "@OneToMany definition $elementName Cannot find referenced variable $variableName.")
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
                if (name == com.dbflow5.processor.ClassNames.DATABASE_WRAPPER) {
                    hasWrapper = true
                } else {
                    manager.logError(OneToManyDefinition::class, "OneToMany Methods can only specify a ${com.dbflow5.processor.ClassNames.DATABASE_WRAPPER} as its parameter.")
                }
            }
        }

        columnAccessor = if (isVariablePrivate) privateAccessor else VisibleScopeColumnAccessor(variableName)

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
                extendsModel = referencedType.isSubclass(manager.processingEnvironment, com.dbflow5.processor.ClassNames.MODEL)
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
                        ParameterizedTypeName.get(com.dbflow5.processor.ClassNames.MODEL_ADAPTER, referencedTableType),
                        com.dbflow5.processor.ClassNames.FLOW_MANAGER, referencedTableType)
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

