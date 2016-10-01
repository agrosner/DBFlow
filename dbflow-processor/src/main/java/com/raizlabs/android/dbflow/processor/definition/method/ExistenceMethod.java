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
                .addParameter(tableDefinition.getParameterClassName(),
                        ModelUtils.getVariable())
                .addParameter(ClassNames.DATABASE_WRAPPER, "wrapper")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(TypeName.BOOLEAN);
        // only quick check if enabled.
        if (tableDefinition.hasAutoIncrement() || tableDefinition.hasRowID()) {
            ColumnDefinition columnDefinition = tableDefinition.getAutoIncrementColumn();
            CodeBlock.Builder incrementBuilder = CodeBlock.builder().add("return ");
            CodeBlock columnAccess = columnDefinition.getColumnAccessString(false);
            if (!columnDefinition.elementTypeName.isPrimitive()) {
                incrementBuilder.add("($L != null && ", columnAccess);
            }
            incrementBuilder.add("$L > 0", columnAccess);
            if (!columnDefinition.elementTypeName.isPrimitive()) {
                incrementBuilder.add(" || $L == null)", columnAccess);
            }
            methodBuilder.addCode(incrementBuilder.build());
        }

        if ((!tableDefinition.hasRowID() && !tableDefinition.hasAutoIncrement()) || !tableDefinition.getAutoIncrementColumn().getIsQuickCheckPrimaryKeyAutoIncrement()) {
            if (tableDefinition.hasAutoIncrement() || tableDefinition.hasRowID()) {
                methodBuilder.addCode(" && ");
            } else {
                methodBuilder.addCode("return ");
            }
            methodBuilder.addCode("$T.selectCountOf()\n.from($T.class)\n.where(getPrimaryConditionClause($L))\n.hasData(wrapper)",
                    ClassNames.SQLITE, tableDefinition.elementClassName, ModelUtils.getVariable());
        }
        methodBuilder.addCode(";\n");

        return methodBuilder.build();
    }
}
