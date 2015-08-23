package com.raizlabs.android.dbflow.processor.definition.method;

import com.andrewgrosner.swiftdb.processor.ClassNames;
import com.andrewgrosner.swiftdb.processor.definition.MethodDefinition;
import com.andrewgrosner.swiftdb.processor.definition.TableDefinition;
import com.andrewgrosner.swiftdb.processor.definition.column.ColumnDefinition;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.lang.Override;import java.lang.String;import javax.lang.model.element.Modifier;

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
        for (ColumnDefinition columnDefinition : tableDefinition.primaryDefinitionList) {
            code.add(".and($T.$L.eq($L))", tableDefinition.propertyClassName, columnDefinition.name, columnDefinition.getColumnAccessString());
        }
        methodBuilder.addCode(code.addStatement("").build());
        return methodBuilder.build();
    }
}
