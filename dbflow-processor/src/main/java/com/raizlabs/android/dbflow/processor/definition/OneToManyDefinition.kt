package com.raizlabs.android.dbflow.processor.definition

import com.google.common.collect.Lists
import com.grosner.kpoet.`for`
import com.grosner.kpoet.`if`
import com.grosner.kpoet.end
import com.grosner.kpoet.statement
import com.raizlabs.android.dbflow.annotation.OneToMany
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.definition.column.*
import com.raizlabs.android.dbflow.processor.utils.*
import com.squareup.javapoet.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Description: Represents the [OneToMany] annotation.
 */
class OneToManyDefinition(typeElement: ExecutableElement,
                          processorManager: ProcessorManager) : BaseDefinition(typeElement, processorManager) {

    private var _methodName: String

    private var _variableName: String

    var methods: MutableList<OneToMany.Method> = Lists.newArrayList<OneToMany.Method>()

    val isLoad: Boolean
        get() = isAll || methods.contains(OneToMany.Method.LOAD)

    val isAll: Boolean
        get() = methods.contains(OneToMany.Method.ALL)

    val isDelete: Boolean
        get() = isAll || methods.contains(OneToMany.Method.DELETE)

    val isSave: Boolean
        get() = isAll || methods.contains(OneToMany.Method.SAVE)

    private var columnAccessor: ColumnAccessor
    private var extendsBaseModel: Boolean = false
    private var extendsModel: Boolean = false
    private var referencedTableType: TypeName? = null
    private var referencedType: TypeElement? = null

    init {

        val oneToMany = typeElement.annotation<OneToMany>()!!

        _methodName = typeElement.simpleName.toString()
        _variableName = oneToMany.variableName
        if (_variableName.isEmpty()) {
            _variableName = _methodName.replace("get", "")
            _variableName = _variableName.substring(0, 1).toLowerCase() + _variableName.substring(1)
        }
        methods.addAll(oneToMany.methods)

        if (oneToMany.isVariablePrivate) {
            columnAccessor = PrivateScopeColumnAccessor(_variableName, object : GetterSetter {
                override val getterName: String = ""
                override val setterName: String = ""
            })
        } else {
            columnAccessor = VisibleScopeColumnAccessor(_variableName)
        }

        extendsBaseModel = false
        val returnType = typeElement.returnType
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
                extendsBaseModel = referencedType.isSubclass(manager.processingEnvironment, ClassNames.BASE_MODEL)
                extendsModel = referencedType.isSubclass(manager.processingEnvironment, ClassNames.MODEL)
            }
        }
    }

    private val methodName = String.format("%1s.%1s()", ModelUtils.variable, _methodName)


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
            writeLoopWithMethod(method, "delete", useWrapper && extendsBaseModel)
            method.statement(columnAccessor.set(CodeBlock.of("null"), modelBlock))
        }
    }

    fun writeSave(codeBuilder: MethodSpec.Builder, useWrapper: Boolean) {
        if (isSave) writeLoopWithMethod(codeBuilder, "save", useWrapper && extendsBaseModel)
    }

    fun writeUpdate(codeBuilder: MethodSpec.Builder, useWrapper: Boolean) {
        if (isSave) writeLoopWithMethod(codeBuilder, "update", useWrapper && extendsBaseModel)
    }

    fun writeInsert(codeBuilder: MethodSpec.Builder, useWrapper: Boolean) {
        if (isSave) writeLoopWithMethod(codeBuilder, "insert", useWrapper && (extendsBaseModel || !extendsModel))
    }

    private fun writeLoopWithMethod(codeBuilder: MethodSpec.Builder, methodName: String, useWrapper: Boolean) {
        val oneToManyMethodName = this@OneToManyDefinition.methodName
        codeBuilder.apply {
            `if`("($oneToManyMethodName != null)") {
                val loopClass: ClassName? = if (extendsBaseModel) ClassNames.BASE_MODEL else ClassName.get(referencedType)

                // need to load adapter for non-model classes

                if (!extendsModel) {
                    statement("\$T adapter = \$T.getModelAdapter(\$T.class)",
                            ParameterizedTypeName.get(ClassNames.MODEL_ADAPTER, referencedTableType),
                            ClassNames.FLOW_MANAGER, referencedTableType)

                    statement("adapter.${methodName}All($oneToManyMethodName\$L)",
                            if (useWrapper) ", " + ModelUtils.wrapper else "")
                } else {
                    `for`("(\$T value: $oneToManyMethodName)", loopClass) {
                        statement("value.$methodName(\$L)", if (useWrapper) ModelUtils.wrapper else "")
                    }
                }
            }.end()
        }
    }

}
