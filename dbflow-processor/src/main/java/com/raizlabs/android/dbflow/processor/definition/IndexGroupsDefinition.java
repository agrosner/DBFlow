package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.IndexGroup;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class IndexGroupsDefinition {

    private ProcessorManager manager;
    private TableDefinition tableDefinition;

    public final String indexName;
    public final int indexNumber;
    public final boolean isUnique;

    public final List<ColumnDefinition> columnDefinitionList = new ArrayList<>();

    public IndexGroupsDefinition(ProcessorManager manager, TableDefinition tableDefinition, IndexGroup indexGroup) {
        this.manager = manager;
        this.tableDefinition = tableDefinition;
        this.indexName = indexGroup.name();
        this.indexNumber = indexGroup.number();
        this.isUnique = indexGroup.unique();
    }

    public FieldSpec getFieldSpec() {
        FieldSpec.Builder fieldBuilder = FieldSpec
                .builder(ParameterizedTypeName.get(ClassNames.INDEX_PROPERTY, tableDefinition.elementClassName),
                        "index_" + indexName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        CodeBlock.Builder initializer = CodeBlock.builder()
                .add("new $T<>($S, $L, $T.class", ClassNames.INDEX_PROPERTY, indexName, isUnique, tableDefinition.elementTypeName);

        for (ColumnDefinition columnDefinition : columnDefinitionList) {
            initializer.add(", $L", columnDefinition.columnName);
        }
        initializer.add(")");

        fieldBuilder.initializer(initializer.build());

        return fieldBuilder.build();
    }

}
