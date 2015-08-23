package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class ExistenceMethod implements MethodDefinition {

    public static final String PARAM_MODEL = "model";

    private final TableDefinition tableDefinition;

    public ExistenceMethod(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    @Override
    public MethodSpec getMethodSpec() {

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("exists")
                .addAnnotation(Override.class)
                .addParameter(tableDefinition.elementClassName, PARAM_MODEL)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(TypeName.BOOLEAN);
        if (tableDefinition.hasAutoIncrementColumn) {
            ColumnDefinition columnDefinition = tableDefinition.primaryDefinitionList.get(0);
            methodBuilder.addStatement("return (($T) $L).longValue() > 0", ClassName.get(Number.class), columnDefinition.getColumnAccessString());
        } else {
            CodeBlock.Builder selectBuilder = CodeBlock.builder();
            java.util.List<ColumnDefinition> primaryDefinitionList = tableDefinition.primaryDefinitionList;
            for (int i = 0; i < primaryDefinitionList.size(); i++) {
                ColumnDefinition columnDefinition = primaryDefinitionList.get(i);
                if (i > 0) {
                    selectBuilder.add(", ");
                }
                selectBuilder.add("$L.$L", tableDefinition.propertyClassName, columnDefinition.name);
            }
            methodBuilder.addStatement("return new $T($L).from($T.class).where(getPrimaryConditionClause($L)).hasResults()",
                    ClassNames.SELECT, selectBuilder.build(), tableDefinition.elementClassName, PARAM_MODEL);
        }

        return methodBuilder.build();
    }
}
