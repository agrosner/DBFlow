package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.column.BaseColumnAccess;
import com.raizlabs.android.dbflow.processor.definition.column.PrivateColumnAccess;
import com.raizlabs.android.dbflow.processor.definition.column.SimpleColumnAccess;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.CodeBlock;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

/**
 * Description: Represents the {@link OneToMany} annotation.
 */
public class OneToManyDefinition extends BaseDefinition {

    public String methodName;

    public String variableName;

    public List<OneToMany.Method> methods = Lists.newArrayList();

    private BaseColumnAccess columnAccess;

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
    public void writeDelete(CodeBlock.Builder codeBuilder) {
        if (isDelete()) {
            writeLoopWithMethod(codeBuilder, "delete");

            codeBuilder.addStatement(columnAccess.setColumnAccessString(null, variableName, variableName,
                false, ModelUtils.getVariable(false), CodeBlock.of("null"), false));
        }
    }

    public void writeSave(CodeBlock.Builder codeBuilder) {
        if (isSave()) {
            writeLoopWithMethod(codeBuilder, "save");
        }
    }

    public void writeUpdate(CodeBlock.Builder codeBuilder) {
        if (isSave()) {
            writeLoopWithMethod(codeBuilder, "update");
        }
    }

    public void writeInsert(CodeBlock.Builder codeBuilder) {
        if (isSave()) {
            writeLoopWithMethod(codeBuilder, "insert");
        }
    }

    private void writeLoopWithMethod(CodeBlock.Builder codeBuilder, String methodName) {
        codeBuilder
            .beginControlFlow("if ($L != null) ", getMethodName())
            .beginControlFlow("for ($T value: $L) ", ClassNames.MODEL, getMethodName())
            .addStatement("value.$L()", methodName)
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
