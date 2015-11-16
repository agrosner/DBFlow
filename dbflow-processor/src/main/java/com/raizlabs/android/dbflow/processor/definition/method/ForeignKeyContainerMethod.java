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
 * Description: Writes a Model into a ForeignKeyContainer for convenience.
 */
public class ForeignKeyContainerMethod implements MethodDefinition {

    private final TableDefinition tableDefinition;

    public ForeignKeyContainerMethod(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }


    @Override
    public MethodSpec getMethodSpec() {

        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.addStatement("$T $L = new $T<>($T.class)", ClassNames.FOREIGN_KEY_CONTAINER,
                ModelUtils.getVariable(true), ClassNames.FOREIGN_KEY_CONTAINER, tableDefinition.elementClassName);
        for (ColumnDefinition columnDefinition : tableDefinition.getPrimaryColumnDefinitions()) {
            codeBuilder.add(columnDefinition.getForeignKeyContainerMethod(tableDefinition.getPropertyClassName()));
        }
        codeBuilder.addStatement("return $L", ModelUtils.getVariable(true));


        return MethodSpec.methodBuilder("toForeignKeyContainer")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(tableDefinition.elementClassName, ModelUtils.getVariable(false))
                .addCode(codeBuilder.build())
                .returns(ParameterizedTypeName.get(ClassNames.FOREIGN_KEY_CONTAINER, tableDefinition.elementClassName))
                .build();
    }
}
