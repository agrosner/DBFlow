package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.ContainerKey;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.CodeBlock;

/**
 * Description: Provides an easy way to wrap in model container accesses.
 */
public class ModelContainerAccess extends BaseColumnAccess {

    private final ColumnDefinition columnDefinition;
    private final BaseColumnAccess existingColumnAccess;
    private final ProcessorManager manager;

    public String containerKeyName;

    public ModelContainerAccess(ProcessorManager manager, ColumnDefinition columnDefinition) {

        this.columnDefinition = columnDefinition;
        this.existingColumnAccess = columnDefinition.columnAccess;
        this.manager = manager;

        ContainerKey containerKey = columnDefinition.element.getAnnotation(ContainerKey.class);
        if (containerKey != null) {
            containerKeyName = containerKey.value();
        } else {
            containerKeyName = columnDefinition.columnName;
        }
    }

    @Override
    String getColumnAccessString(String variableNameString, String elementName, boolean isModelContainerAdapter) {
        return CodeBlock.builder()
                .add("$L.get($S)",
                        existingColumnAccess.getColumnAccessString(variableNameString, elementName, isModelContainerAdapter),
                        containerKeyName)
                .build().toString();
    }

    @Override
    String getShortAccessString(String elementName, boolean isModelContainerAdapter) {
        return CodeBlock.builder()
                .add("$L.get($S)", existingColumnAccess.getShortAccessString(elementName, isModelContainerAdapter),
                        containerKeyName)
                .build().toString();
    }

    @Override
    String setColumnAccessString(String variableNameString, String elementName, String formattedAccess, boolean isModelContainerAdapter) {
        String newFormattedAccess = CodeBlock.builder()
                .add("$L.put($S, $L)", variableNameString, containerKeyName, formattedAccess)
                .build().toString();
        return existingColumnAccess.setColumnAccessString(variableNameString, elementName, newFormattedAccess, isModelContainerAdapter);
    }
}
