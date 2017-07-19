package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.`for`
import com.grosner.kpoet.`if`
import com.grosner.kpoet.end
import com.grosner.kpoet.statement
import com.grosner.kpoet.typeName
import com.raizlabs.android.dbflow.annotation.OneToMany
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

    var methods = mutableListOf<OneToMany.Method>()

    val isLoad
        get() = isAll || methods.contains(OneToMany.Method.LOAD)

    val isAll
        get() = methods.contains(OneToMany.Method.ALL)

    val isDelete: Boolean
        get() = isAll || methods.contains(OneToMany.Method.DELETE)

    val isSave: Boolean
        get() = isAll || methods.contains(OneToMany.Method.SAVE)

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

        var isVariablePrivate = false
        val referencedElement = parentElements.firstOrNull { it.simpleName.toString() == _variableName }
        if (referencedElement == null) {
            manager.logError(OneToManyDefinition::class,
                "@OneToMany definition $elementName Cannot find referenced variable $_variableName.")
        } else {
            isVariablePrivate = referencedElement.modifiers.contains(Modifier.PRIVATE)
        }

        methods.addAll(oneToMany.methods)

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
            columnAccessor = PrivateScopeColumnAccessor(_variableName, object : GetterSetter {
                override val getterName: String = ""
                override val setterName: String = ""
            }, optionalGetterParam = if (hasWrapper) ModelUtils.wrapper else "")
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

    fun writeWrapperStatement(method: MethodSpec.Builder) {
        method.statement("\$T ${ModelUtils.wrapper} = \$T.getWritableDatabaseForTable(\$T.class)",
            ClassNames.DATABASE_WRAPPER, ClassNames.FLOW_MANAGER, referencedTableType)
    }

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
    fun writeDelete(method: MethodSpec.Builder, useWrapper: Boolean) {
        if (isDelete) {
            writeLoopWithMethod(method, "delete", useWrapper)
            method.statement(columnAccessor.set(CodeBlock.of("null"), modelBlock))
        }
    }

    fun writeSave(codeBuilder: MethodSpec.Builder, useWrapper: Boolean) {
        if (isSave) writeLoopWithMethod(codeBuilder, "save", useWrapper)
    }

    fun writeUpdate(codeBuilder: MethodSpec.Builder, useWrapper: Boolean) {
        if (isSave) writeLoopWithMethod(codeBuilder, "update", useWrapper)
    }

    fun writeInsert(codeBuilder: MethodSpec.Builder, useWrapper: Boolean) {
        if (isSave) writeLoopWithMethod(codeBuilder, "insert", useWrapper)
    }

    private fun writeLoopWithMethod(codeBuilder: MethodSpec.Builder, methodName: String, useWrapper: Boolean) {
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
                    statement("adapter.${methodName}All($oneToManyMethodName${wrapperCommaIfBaseModel(useWrapper)})")
                } else {
                    `for`("\$T value: $oneToManyMethodName", ClassName.get(referencedType)) {
                        if (!extendsModel) {
                            statement("adapter.$methodName(value${wrapperCommaIfBaseModel(useWrapper)})")
                        } else {
                            statement("value.$methodName(${wrapperIfBaseModel(useWrapper)})")
                        }
                        this
                    }
                }
            }
        }.end()
    }
}

