package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.column.BaseColumnAccess;
import com.raizlabs.android.dbflow.processor.definition.column.PrivateColumnAccess;
import com.raizlabs.android.dbflow.processor.definition.column.SimpleColumnAccess;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Description: Represents the {@link OneToMany} annotation.
 */
public class OneToManyDefinition extends BaseDefinition {

    public String methodName;

    public String variableName;

    public List<OneToMany.Method> methods = Lists.newArrayList();

    private BaseColumnAccess columnAccess;
    private boolean extendsBaseModel;

    public OneToManyDefinition(ExecutableElement typeElement,
                               ProcessorManager processorManager) {
        super(typeElement, processorManager);

        OneToMany oneToMany = typeElement.getAnnotation(OneToMany.class);

        methodName = typeElement.getSimpleName().toString();
        variableName = oneToMany.variableName();
        if (variableName == null || variableName.isEmpty()) {
            variableName = methodName.replace("get", "");
            variableName = variableName.substring(0, 1).toLowerCase() + variableName.substring(1);
        }
        methods.addAll(Arrays.asList(oneToMany.methods()));

        if (oneToMany.isVariablePrivate()) {
            columnAccess = new PrivateColumnAccess(false);
        } else {
            columnAccess = new SimpleColumnAccess();
        }

        extendsBaseModel = false;
        TypeMirror returnType = typeElement.getReturnType();
        TypeName typeName = TypeName.get(returnType);
        if (typeName instanceof ParameterizedTypeName) {
            List<TypeName> typeArguments = ((ParameterizedTypeName) typeName).typeArguments;
            if (typeArguments.size() == 1) {
                TypeName returnTypeName = typeArguments.get(0);
                extendsBaseModel = ProcessorUtils.isSubclass(manager.getProcessingEnvironment(),
                        ClassNames.BASE_MODEL.toString(), manager.getElements().getTypeElement(returnTypeName.toString()));
            }
        }
    }

    public boolean isLoad() {
        return isAll() || methods.contains(OneToMany.Method.LOAD);
    }

    public boolean isAll() {
        return methods.contains(OneToMany.Method.ALL);
    }

    public boolean isDelete() {
        return isAll() || methods.contains(OneToMany.Method.DELETE);
    }

    public boolean isSave() {
        return isAll() || methods.contains(OneToMany.Method.SAVE);
    }

    /**
     * Writes the method to the specified builder for loading from DB.
     */
    public void writeLoad(CodeBlock.Builder codeBuilder) {
        if (isLoad()) {
            codeBuilder.addStatement(getMethodName());
        }
    }

    /**
     * Writes a delete method that will delete all related objects.
     *
     * @param codeBuilder
     */
    public void writeDelete(CodeBlock.Builder codeBuilder, boolean useWrapper) {
        if (isDelete()) {
            writeLoopWithMethod(codeBuilder, "delete", useWrapper && extendsBaseModel);

            codeBuilder.addStatement(columnAccess.setColumnAccessString(null, variableName, variableName,
                    false, ModelUtils.getVariable(false), CodeBlock.builder().add("null").build(),
                    false));
        }
    }

    public void writeSave(CodeBlock.Builder codeBuilder, boolean useWrapper) {
        if (isSave()) {
            writeLoopWithMethod(codeBuilder, "save", useWrapper && extendsBaseModel);
        }
    }

    public void writeUpdate(CodeBlock.Builder codeBuilder, boolean useWrapper) {
        if (isSave()) {
            writeLoopWithMethod(codeBuilder, "update", useWrapper && extendsBaseModel);
        }
    }

    public void writeInsert(CodeBlock.Builder codeBuilder, boolean useWrapper) {
        if (isSave()) {
            writeLoopWithMethod(codeBuilder, "insert", useWrapper && extendsBaseModel);
        }
    }

    private void writeLoopWithMethod(CodeBlock.Builder codeBuilder, String methodName, boolean useWrapper) {
        codeBuilder
                .beginControlFlow("if ($L != null) ", getMethodName())
                .beginControlFlow("for ($T value: $L) ", extendsBaseModel ? ClassNames.BASE_MODEL : ClassNames.MODEL, getMethodName())
                .addStatement("value.$L($L)", methodName, useWrapper ? ModelUtils.getWrapper() : "")
                .endControlFlow()
                .endControlFlow();
    }

    private String getMethodName() {
        return String.format("%1s.%1s()", ModelUtils.getVariable(false), methodName);
    }

    private String getVariableName() {
        return String.format("%1s.%1s", ModelUtils.getVariable(false), variableName);
    }

}
