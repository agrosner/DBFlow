package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.CodeBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 */
public class UniqueGroupsDefinition {

    private final ProcessorManager manager;

    private List<ColumnDefinition> columnDefinitionList = new ArrayList<>();

    public int number;

    private ConflictAction uniqueConflict;

    public UniqueGroupsDefinition(ProcessorManager manager, UniqueGroup uniqueGroup) {
        this.manager = manager;
        number = uniqueGroup.groupNumber();
        uniqueConflict = uniqueGroup.uniqueConflict();
    }

    public void addColumnDefinition(ColumnDefinition columnDefinition) {
        columnDefinitionList.add(columnDefinition);
    }

    public CodeBlock getCreationName() {
        CodeBlock.Builder codeBuilder = CodeBlock.builder()
                .add(", UNIQUE(");
        int count = 0;
        for (ColumnDefinition columnDefinition : columnDefinitionList) {
            if (count > 0) {
                codeBuilder.add(",");
            }
            codeBuilder.add(QueryBuilder.quote(columnDefinition.columnName));
            count++;
        }
        codeBuilder.add(") ON CONFLICT $L", uniqueConflict);
        return codeBuilder.build();
    }
}