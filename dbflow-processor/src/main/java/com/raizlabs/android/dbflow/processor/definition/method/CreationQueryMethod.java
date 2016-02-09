package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.UniqueGroupsDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyReferenceDefinition;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class CreationQueryMethod implements MethodDefinition {

    private TableDefinition tableDefinition;

    public CreationQueryMethod(TableDefinition tableDefinition) {

        this.tableDefinition = tableDefinition;
    }

    @Override
    public MethodSpec getMethodSpec() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getCreationQuery")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .returns(ClassName.get(String.class));

        CodeBlock.Builder creationBuilder = CodeBlock.builder()
            .add("CREATE TABLE IF NOT EXISTS ")
            .add(QueryBuilder.quote(tableDefinition.tableName))
            .add("(");

        for (int i = 0; i < tableDefinition.getColumnDefinitions().size(); i++) {
            if (i > 0) {
                creationBuilder.add(",");
            }

            ColumnDefinition columnDefinition = tableDefinition.getColumnDefinitions().get(i);
            creationBuilder.add(columnDefinition.getCreationName());
        }

        for (UniqueGroupsDefinition definition : tableDefinition.uniqueGroupsDefinitions) {
            if (!definition.columnDefinitionList.isEmpty()) {
                creationBuilder.add(definition.getCreationName());
            }
        }

        int primarySize = tableDefinition.getPrimaryColumnDefinitions().size();
        for (int i = 0; i < primarySize; i++) {
            if (i == 0) {
                creationBuilder.add(", PRIMARY KEY(");
            }

            if (i > 0) {
                creationBuilder.add(",");
            }

            ColumnDefinition primaryDefinition = tableDefinition.getPrimaryColumnDefinitions().get(i);
            creationBuilder.add(primaryDefinition.getCreationName());

            if (i == primarySize - 1) {
                creationBuilder.add(")");
            }
        }

        int foreignSize = tableDefinition.foreignKeyDefinitions.size();

        List<CodeBlock> foreignKeyBlocks = new ArrayList<>();
        List<CodeBlock> tableNameBlocks = new ArrayList<>();
        List<CodeBlock> referenceKeyBlocks = new ArrayList<>();

        for (int i = 0; i < foreignSize; i++) {
            CodeBlock.Builder foreignKeyBuilder = CodeBlock.builder();
            CodeBlock.Builder referenceBuilder = CodeBlock.builder();
            ForeignKeyColumnDefinition foreignKeyColumnDefinition = tableDefinition.foreignKeyDefinitions.get(i);

            foreignKeyBuilder.add(", FOREIGN KEY(");

            for (int j = 0; j < foreignKeyColumnDefinition.foreignKeyReferenceDefinitionList.size(); j++) {
                if (j > 0) {
                    foreignKeyBuilder.add(",");
                }
                ForeignKeyReferenceDefinition referenceDefinition = foreignKeyColumnDefinition.foreignKeyReferenceDefinitionList.get(j);
                foreignKeyBuilder.add("$L", QueryBuilder.quote(referenceDefinition.columnName));
            }


            foreignKeyBuilder.add(") REFERENCES ");

            foreignKeyBlocks.add(foreignKeyBuilder.build());

            tableNameBlocks.add(CodeBlock.builder().add("$T.getTableName($T.class)",
                ClassNames.FLOW_MANAGER, foreignKeyColumnDefinition.referencedTableClassName).build());

            referenceBuilder.add("(");
            for (int j = 0; j < foreignKeyColumnDefinition.foreignKeyReferenceDefinitionList.size(); j++) {
                if (j > 0) {
                    referenceBuilder.add(", ");
                }
                ForeignKeyReferenceDefinition referenceDefinition = foreignKeyColumnDefinition.foreignKeyReferenceDefinitionList.get(j);
                referenceBuilder.add("$L", QueryBuilder.quote(referenceDefinition.foreignColumnName));
            }
            referenceBuilder.add(") ON UPDATE $L ON DELETE $L", foreignKeyColumnDefinition.onUpdate.name().replace("_", " "),
                foreignKeyColumnDefinition.onDelete.name().replace("_", " "));
            referenceKeyBlocks.add(referenceBuilder.build());
        }

        CodeBlock.Builder codeBuilder = CodeBlock.builder()
            .add("return $S", creationBuilder.build().toString());

        if (foreignSize > 0) {
            for (int i = 0; i < foreignSize; i++) {
                codeBuilder.add("+ $S + $L + $S", foreignKeyBlocks.get(i), tableNameBlocks.get(i), referenceKeyBlocks.get(i));
            }
        }
        codeBuilder.add(" + $S", ");").add(";\n");


        methodBuilder.addCode(codeBuilder.build());

        return methodBuilder.build();
    }
}
