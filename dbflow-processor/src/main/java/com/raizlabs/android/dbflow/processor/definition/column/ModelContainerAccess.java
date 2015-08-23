package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.CodeBlock;

/**
 * Description: Provides an easy way to wrap in model container accesses.
 */
public class ModelContainerAccess extends BaseColumnAccess {

    private final ColumnDefinition columnDefinition;

    private final BaseColumnAccess existingColumnAccess;

    private final ProcessorManager manager;

    public ModelContainerAccess(ProcessorManager manager, ColumnDefinition columnDefinition) {

        this.columnDefinition = columnDefinition;
        this.existingColumnAccess = columnDefinition.columnAccess;
        this.manager = manager;
    }

    @Override
    String getColumnAccessString(String variableNameString, String elementName) {
        return CodeBlock.builder()
                .add("$L.get($S)", variableNameString,
                        existingColumnAccess.getColumnAccessString(variableNameString, elementName))
                .build().toString();
    }

    @Override
    String getShortAccessString(String elementName) {
        return CodeBlock.builder()
                .add("get($S)", existingColumnAccess.getShortAccessString(elementName))
                .build().toString();
    }

    @Override
    String setColumnAccessString(String variableNameString, String elementName, String formattedAccess) {
        String newFormattedAccess = CodeBlock.builder()
                .add("$L.put($S, $L)", variableNameString, elementName, formattedAccess)
                .build().toString();
        return existingColumnAccess.setColumnAccessString(variableNameString, elementName, newFormattedAccess);
    }
}
