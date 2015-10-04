package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * Description: Represents the {@link OneToMany} annotation.
 */
public class OneToManyDefinition extends BaseDefinition {

    public String methodName;

    public String variableName;

    public List<OneToMany.Method> methods = Lists.newArrayList();

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
            codeBuilder.addStatement("new $T<>($T.withModels($L)).onExecute()",
                    ClassNames.DELETE_MODEL_LIST_TRANSACTION,
                    ClassNames.PROCESS_MODEL_INFO, getMethodName());

            codeBuilder.addStatement("$L = null", getVariableName());
        }
    }

    public void writeSave(CodeBlock.Builder codeBuilder) {
        if (isSave()) {
            codeBuilder.addStatement("new $T<>($T.withModels($L)).onExecute()",
                    ClassNames.SAVE_MODEL_LIST_TRANSACTION,
                    ClassNames.PROCESS_MODEL_INFO, getMethodName());
        }
    }

    public void writeUpdate(CodeBlock.Builder codeBuilder) {
        if (isSave()) {
            codeBuilder.addStatement("new $T<>($T.withModels($L)).onExecute()",
                    ClassNames.UPDATE_MODEL_LIST_TRANSACTION,
                    ClassNames.PROCESS_MODEL_INFO, getMethodName());
        }
    }

    public void writeInsert(CodeBlock.Builder codeBuilder) {
        if (isSave()) {
            codeBuilder.addStatement("new $T<>($T.withModels($L)).onExecute()",
                    ClassNames.INSERT_MODEL_LIST_TRANSACTION,
                    ClassNames.PROCESS_MODEL_INFO, getMethodName());
        }
    }
    private String getMethodName() {
        return String.format("%1s.%1s()", ModelUtils.getVariable(false), methodName);
    }

    private String getVariableName() {
        return String.format("%1s.%1s", ModelUtils.getVariable(false), variableName);
    }

}
