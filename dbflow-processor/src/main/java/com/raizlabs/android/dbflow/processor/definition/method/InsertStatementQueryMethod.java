package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class InsertStatementQueryMethod implements MethodDefinition {

    private TableDefinition tableDefinition;

    public InsertStatementQueryMethod(TableDefinition tableDefinition) {

        this.tableDefinition = tableDefinition;
    }

    @Override
    public MethodSpec getMethodSpec() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getInsertStatementQuery")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(ClassName.get(String.class));

        CodeBlock.Builder codeBuilder = CodeBlock.builder()
                .add("INSERT INTO ")
                .add(QueryBuilder.quote(tableDefinition.tableName))
                .add("(");

        int columnSize = tableDefinition.getColumnDefinitions().size();
        int columnCount = 0;
        for (ColumnDefinition column : tableDefinition.getColumnDefinitions()) {
            if (columnCount > 0) {
                codeBuilder.add(",");
            }

            if (!column.isPrimaryKeyAutoIncrement) {
                codeBuilder.add(column.getInsertStatementColumnName());
                columnCount++;
            }
        }

        codeBuilder.add(")");

        codeBuilder.add(" VALUES (");

        columnCount = 0;
        for (int i = 0; i < columnSize; i++) {
            if (columnCount > 0) {
                codeBuilder.add(",");
            }

            ColumnDefinition definition = tableDefinition.getColumnDefinitions().get(i);
            if (!definition.isPrimaryKeyAutoIncrement) {
                codeBuilder.add(definition.getInsertStatementValuesString());
                columnCount++;
            }
        }

        codeBuilder.add(")");

        methodBuilder.addStatement("return $S", codeBuilder.build().toString());

        return methodBuilder.build();
    }
}
