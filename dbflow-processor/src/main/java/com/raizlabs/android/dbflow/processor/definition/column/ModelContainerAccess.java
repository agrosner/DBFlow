package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.ContainerKey;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description: Provides an easy way to wrap in model container accesses.
 */
public class ModelContainerAccess extends BaseColumnAccess {

    private final BaseColumnAccess existingColumnAccess;

    public String containerKeyName;

    public ModelContainerAccess(ColumnDefinition columnDefinition) {
        this.existingColumnAccess = columnDefinition.columnAccess;

        ContainerKey containerKey = columnDefinition.element.getAnnotation(ContainerKey.class);
        if (containerKey != null) {
            containerKeyName = containerKey.value();
        } else {
            containerKeyName = columnDefinition.columnName;
        }
    }

    public ModelContainerAccess(BaseColumnAccess existingColumnAccess, String containerKeyName) {
        this.existingColumnAccess = existingColumnAccess;
        this.containerKeyName = containerKeyName;
    }

    @Override
    String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter) {
        return CodeBlock.builder()
                .add("$L.get($S)",
                        existingColumnAccess.getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isModelContainerAdapter),
                        containerKeyName)
                .build().toString();
    }

    @Override
    String getShortAccessString(boolean isModelContainerAdapter, String elementName) {
        return CodeBlock.builder()
                .add("$L.get($S)", existingColumnAccess.getShortAccessString(isModelContainerAdapter, elementName),
                        containerKeyName)
                .build().toString();
    }

    @Override
    String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess) {
        CodeBlock newFormattedAccess = CodeBlock.builder()
                .add("$L.put($S, $L)", variableNameString, containerKeyName, formattedAccess)
                .build();
        return existingColumnAccess.setColumnAccessString(fieldType, elementName, fullElementName, isModelContainerAdapter, variableNameString, newFormattedAccess);
    }
}
