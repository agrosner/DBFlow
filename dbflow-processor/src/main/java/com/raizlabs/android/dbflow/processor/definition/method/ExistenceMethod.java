package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class ExistenceMethod implements MethodDefinition {

    public static final String PARAM_MODEL = "model";

    private final BaseTableDefinition tableDefinition;
    private final boolean isModelContainerAdapter;

    public ExistenceMethod(BaseTableDefinition tableDefinition, boolean isModelContainerAdapter) {
        this.tableDefinition = tableDefinition;
        this.isModelContainerAdapter = isModelContainerAdapter;
    }

    @Override
    public MethodSpec getMethodSpec() {

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("exists")
                .addAnnotation(Override.class)
                .addParameter(tableDefinition.getParameterClassName(isModelContainerAdapter),
                        ModelUtils.getVariable(isModelContainerAdapter))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(TypeName.BOOLEAN);
        // only quick check if enabled.
        if (tableDefinition.hasAutoIncrement()) {
            ColumnDefinition columnDefinition = tableDefinition.getAutoIncrementColumn();
            methodBuilder.addCode("return $L > 0", columnDefinition.getColumnAccessString(isModelContainerAdapter, false));
        }

        if (!tableDefinition.hasAutoIncrement() || !tableDefinition.getAutoIncrementColumn().isQuickCheckPrimaryKeyAutoIncrement) {
            CodeBlock.Builder selectBuilder = CodeBlock.builder();
            java.util.List<ColumnDefinition> primaryDefinitionList = tableDefinition.getPrimaryColumnDefinitions();
            for (int i = 0; i < primaryDefinitionList.size(); i++) {
                ColumnDefinition columnDefinition = primaryDefinitionList.get(i);
                if (i > 0) {
                    selectBuilder.add(", ");
                }
                selectBuilder.add("$L.$L", tableDefinition.getPropertyClassName(), columnDefinition.columnName);
            }
            if (tableDefinition.hasAutoIncrement()) {
                methodBuilder.addCode(" && ");
            } else {
                methodBuilder.addCode("return ");
            }
            methodBuilder.addCode("new $T($L).from($T.class).where(getPrimaryConditionClause($L)).hasData()",
                    ClassNames.SELECT, selectBuilder.build(), tableDefinition.elementClassName, ModelUtils.getVariable(isModelContainerAdapter));
        }
        methodBuilder.addCode(";\n");

        return methodBuilder.build();
    }
}
