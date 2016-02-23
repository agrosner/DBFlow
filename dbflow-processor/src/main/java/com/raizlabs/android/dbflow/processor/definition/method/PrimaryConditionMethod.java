package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

/**
 * Description: Creates a method that builds a clause of ConditionGroup that represents its primary keys. Useful
 * for updates or deletes.
 */
public class PrimaryConditionMethod implements MethodDefinition {

    static final String PARAM_MODEL = "model";

    private final BaseTableDefinition tableDefinition;
    private final boolean isModelContainerAdapter;

    public PrimaryConditionMethod(BaseTableDefinition tableDefinition, boolean isModelContainerAdapter) {
        this.tableDefinition = tableDefinition;
        this.isModelContainerAdapter = isModelContainerAdapter;
    }

    @Override
    public MethodSpec getMethodSpec() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getPrimaryConditionClause")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(tableDefinition.getParameterClassName(isModelContainerAdapter),
                ModelUtils.getVariable(isModelContainerAdapter))
            .returns(ClassNames.CONDITION_GROUP);
        CodeBlock.Builder code = CodeBlock.builder();
        code.add("$T clause = $T.clause();", ClassNames.CONDITION_GROUP, ClassNames.CONDITION_GROUP);
        for (ColumnDefinition columnDefinition : tableDefinition.getPrimaryColumnDefinitions()) {
            CodeBlock.Builder codeBuilder = CodeBlock.builder();
            columnDefinition.appendPropertyComparisonAccessStatement(isModelContainerAdapter, codeBuilder);
            code.add(codeBuilder.build());
        }
        methodBuilder.addCode(code.build());
        methodBuilder.addStatement("return clause");
        return methodBuilder.build();
    }
}
