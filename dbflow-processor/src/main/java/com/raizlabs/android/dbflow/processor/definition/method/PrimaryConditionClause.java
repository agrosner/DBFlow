package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

/**
 * Description: Creates a method that builds a clause of ConditionGroup that represents its primary keys. Useful
 * for updates or deletes.
 */
public class PrimaryConditionClause implements MethodDefinition {

    static final String PARAM_MODEL = "model";

    private final TableDefinition tableDefinition;

    public PrimaryConditionClause(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    @Override
    public MethodSpec getMethodSpec() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getPrimaryConditionClause")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(tableDefinition.elementClassName, PARAM_MODEL)
                .returns(ClassNames.CONDITION_GROUP);
        CodeBlock.Builder code = CodeBlock.builder()
                .add("return $T.clause()", ClassNames.CONDITION_GROUP);
        for (ColumnDefinition columnDefinition : tableDefinition.getPrimaryColumnDefinitions()) {
            code.add(".and($T.$L.eq($L))", tableDefinition.outputClassName, columnDefinition.columnName,
                    columnDefinition.getColumnAccessString());
        }
        methodBuilder.addCode(code.addStatement("").build());
        return methodBuilder.build();
    }
}
