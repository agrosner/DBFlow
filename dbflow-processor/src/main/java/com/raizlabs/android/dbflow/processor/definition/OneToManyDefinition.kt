package com.raizlabs.android.dbflow.processor.definition

import com.google.common.collect.Lists
import com.raizlabs.android.dbflow.annotation.OneToMany
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorUtils
import com.raizlabs.android.dbflow.processor.definition.column.BaseColumnAccess
import com.raizlabs.android.dbflow.processor.definition.column.PrivateColumnAccess
import com.raizlabs.android.dbflow.processor.definition.column.SimpleColumnAccess
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import java.util.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Description: Represents the [OneToMany] annotation.
 */
class OneToManyDefinition(typeElement: ExecutableElement,
                          processorManager: ProcessorManager) : BaseDefinition(typeElement, processorManager) {

    private var methodName: String

    private var _variableName: String? = null

    var methods: MutableList<OneToMany.Method> = Lists.newArrayList<OneToMany.Method>()

    private var columnAccess: BaseColumnAccess? = null
    private var extendsBaseModel: Boolean = false
    private var extendsModel: Boolean = false
    private var referencedTableType: TypeName? = null
    private var referencedType: TypeElement? = null

    init {

        val oneToMany = typeElement.getAnnotation(OneToMany::class.java)

        methodName = typeElement.simpleName.toString()
        _variableName = oneToMany.variableName
        if (_variableName == null || _variableName!!.isEmpty()) {
            _variableName = methodName.replace("get", "")
            _variableName = _variableName!!.substring(0, 1).toLowerCase() + _variableName!!.substring(1)
        }
        methods.addAll(Arrays.asList<OneToMany.Method>(*oneToMany.methods))

        if (oneToMany.isVariablePrivate) {
            columnAccess = PrivateColumnAccess(false)
        } else {
            columnAccess = SimpleColumnAccess()
        }

        extendsBaseModel = false
        val returnType = typeElement.returnType
        val typeName = TypeName.get(returnType)
        if (typeName is ParameterizedTypeName) {
            val typeArguments = typeName.typeArguments
            if (typeArguments.size == 1) {
                referencedTableType = typeArguments[0]
                referencedType = manager.elements.getTypeElement(referencedTableType?.toString())
                extendsBaseModel = ProcessorUtils.isSubclass(manager.processingEnvironment,
                        ClassNames.BASE_MODEL.toString(), referencedType)
                extendsModel = ProcessorUtils.isSubclass(manager.processingEnvironment,
                        ClassNames.MODEL.toString(), referencedType)
            }
        }
    }

    val isLoad: Boolean
        get() = isAll || methods.contains(OneToMany.Method.LOAD)

    val isAll: Boolean
        get() = methods.contains(OneToMany.Method.ALL)

    val isDelete: Boolean
        get() = isAll || methods.contains(OneToMany.Method.DELETE)

    val isSave: Boolean
        get() = isAll || methods.contains(OneToMany.Method.SAVE)

    /**
     * Writes the method to the specified builder for loading from DB.
     */
    fun writeLoad(codeBuilder: CodeBlock.Builder) {
        if (isLoad) {
            codeBuilder.addStatement(getMethodName())
        }
    }

    /**
     * Writes a delete method that will delete all related objects.

     * @param codeBuilder
     */
    fun writeDelete(codeBuilder: CodeBlock.Builder, useWrapper: Boolean) {
        if (isDelete) {
            writeLoopWithMethod(codeBuilder, "delete", useWrapper && extendsBaseModel)

            columnAccess?.let {
                codeBuilder.add(it.setColumnAccessString(null, getVariableName(), getVariableName(),
                        ModelUtils.variable, CodeBlock.builder().add("null").build()).toBuilder().add(";\n").build())
            }
        }
    }

    fun writeSave(codeBuilder: CodeBlock.Builder, useWrapper: Boolean) {
        if (isSave) {
            writeLoopWithMethod(codeBuilder, "save", useWrapper && extendsBaseModel)
        }
    }

    fun writeUpdate(codeBuilder: CodeBlock.Builder, useWrapper: Boolean) {
        if (isSave) {
            writeLoopWithMethod(codeBuilder, "update", useWrapper && extendsBaseModel)
        }
    }

    fun writeInsert(codeBuilder: CodeBlock.Builder, useWrapper: Boolean) {
        if (isSave) {
            writeLoopWithMethod(codeBuilder, "insert", useWrapper && extendsBaseModel || useWrapper && !extendsModel)
        }
    }

    private fun writeLoopWithMethod(codeBuilder: CodeBlock.Builder, methodName: String, useWrapper: Boolean) {
        codeBuilder.beginControlFlow("if (\$L != null) ", getMethodName())
        var loopClass: ClassName?
        if (extendsBaseModel) {
            loopClass = ClassNames.BASE_MODEL
        } else {
            loopClass = ClassName.get(referencedType)
        }

        // need to load adapter for non-model classes
        if (!extendsModel) {
            codeBuilder.addStatement("\$T adapter = \$T.getModelAdapter(\$T.class)",
                    ParameterizedTypeName.get(ClassNames.MODEL_ADAPTER, referencedTableType),
                    ClassNames.FLOW_MANAGER, referencedTableType)

            codeBuilder.addStatement("adapter.\$LAll(\$L\$L)", methodName, getMethodName(),
                    if (useWrapper) ", " + ModelUtils.wrapper else "")
        } else {
            codeBuilder.beginControlFlow("for (\$T value: \$L) ", loopClass, getMethodName())
            codeBuilder.addStatement("value.\$L(\$L)", methodName, if (useWrapper) ModelUtils.wrapper else "")
            codeBuilder.endControlFlow()
        }

        codeBuilder.endControlFlow()
    }

    private fun getMethodName(): String {
        return String.format("%1s.%1s()", ModelUtils.variable, methodName)
    }

    private fun getVariableName(): String {
        return String.format("%1s.%1s", ModelUtils.variable, _variableName)
    }

}
