package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;

import javax.lang.model.element.Modifier;

/**
 * Description: Writes how to convert a ModelContainer into a Model.
 */
public class ToModelMethod implements MethodDefinition {

    private TableDefinition tableDefinition;
    private boolean isModelContainerDefinition;

    public ToModelMethod(TableDefinition tableDefinition, boolean isModelContainerDefinition) {
        this.tableDefinition = tableDefinition;
        this.isModelContainerDefinition = isModelContainerDefinition;
    }

    @Override
    public MethodSpec getMethodSpec() {

        CodeBlock.Builder codeBuilder = CodeBlock.builder()
                .addStatement("$T $L = new $T()", tableDefinition.elementClassName, ModelUtils.getVariable(false),
                        tableDefinition.elementClassName);
        for (ColumnDefinition columnDefinition : tableDefinition.getColumnDefinitions()) {
            codeBuilder.add(columnDefinition.getToModelMethod(isModelContainerDefinition));
        }
        codeBuilder.addStatement("return $L", ModelUtils.getVariable(false));

        return MethodSpec.methodBuilder("tomModel")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ParameterizedTypeName.get(ClassNames.MODEL_CONTAINER, tableDefinition.elementTypeName),
                        ModelUtils.getVariable(true))
                .addCode(codeBuilder.build()).build();

    }
}
