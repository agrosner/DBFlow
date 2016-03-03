package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class InsertStatementQueryMethod implements MethodDefinition {

    private final TableDefinition tableDefinition;
    private final boolean isInsert;

    public InsertStatementQueryMethod(TableDefinition tableDefinition, boolean isInsert) {
        this.tableDefinition = tableDefinition;
        this.isInsert = isInsert;
    }

    @Override
    public MethodSpec getMethodSpec() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(isInsert ? "getInsertStatementQuery" : "getCompiledStatementQuery")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .returns(ClassName.get(String.class));

        CodeBlock.Builder codeBuilder = CodeBlock.builder()
            .add("INSERT ");
        if (!tableDefinition.insertConflictActionName.isEmpty()) {
            codeBuilder.add("OR $L ", tableDefinition.insertConflictActionName);
        }
        codeBuilder.add("INTO ")
            .add(QueryBuilder.quote(tableDefinition.tableName));

        boolean isSingleAutoincrement = (tableDefinition.hasAutoIncrement() && tableDefinition.getColumnDefinitions().size() == 1
            && isInsert);

        codeBuilder.add("(");

        int columnSize = tableDefinition.getColumnDefinitions().size();
        int columnCount = 0;
        for (ColumnDefinition column : tableDefinition.getColumnDefinitions()) {
            if (!column.isPrimaryKeyAutoIncrement() && !column.isRowId || !isInsert || isSingleAutoincrement) {
                if (columnCount > 0) {
                    codeBuilder.add(",");
                }

                codeBuilder.add(column.getInsertStatementColumnName());
                columnCount++;
            }
        }
        codeBuilder.add(")");

        codeBuilder.add(" VALUES (");

        columnCount = 0;
        for (int i = 0; i < columnSize; i++) {
            ColumnDefinition definition = tableDefinition.getColumnDefinitions().get(i);
            if (!definition.isPrimaryKeyAutoIncrement() && !definition.isRowId || !isInsert) {
                if (columnCount > 0) {
                    codeBuilder.add(",");
                }

                codeBuilder.add(definition.getInsertStatementValuesString());
                columnCount++;
            }
        }

        if (isSingleAutoincrement) {
            codeBuilder.add("NULL");
        }

        codeBuilder.add(")");

        methodBuilder.addStatement("return $S", codeBuilder.build().toString());

        return methodBuilder.build();
    }
}
