package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ContainerKeyDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.WildcardTypeName;

import javax.lang.model.element.Modifier;

/**
 * Description: Writes how to convert a ModelContainer into a Model.
 */
public class ToModelMethod implements MethodDefinition {

    private TableDefinition tableDefinition;

    public ToModelMethod(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    @Override
    public MethodSpec getMethodSpec() {

        CodeBlock.Builder codeBuilder = CodeBlock.builder()
                .addStatement("$T $L = new $T()", tableDefinition.elementClassName, ModelUtils.getVariable(false),
                        tableDefinition.elementClassName);
        for (ColumnDefinition columnDefinition : tableDefinition.getColumnDefinitions()) {
            codeBuilder.add(columnDefinition.getToModelMethod());
        }
        for (ContainerKeyDefinition containerKeyDefinition : tableDefinition.containerKeyDefinitions) {
            codeBuilder.add(containerKeyDefinition.getToModelMethod());
        }
        codeBuilder.addStatement("return $L", ModelUtils.getVariable(false));

        return MethodSpec.methodBuilder("toModel")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ParameterizedTypeName.get(ClassNames.MODEL_CONTAINER,
                        tableDefinition.elementClassName, WildcardTypeName.get(tableDefinition.manager
                                .getTypeUtils().getWildcardType(null, null))),
                        ModelUtils.getVariable(true))
                .addCode(codeBuilder.build())
                .returns(tableDefinition.elementClassName).build();

    }
}
