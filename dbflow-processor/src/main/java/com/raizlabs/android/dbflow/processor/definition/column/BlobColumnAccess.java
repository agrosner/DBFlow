package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.data.Blob;
import com.squareup.javapoet.CodeBlock;

/**
 * Description: Defines how to access a {@link Blob}.
 */
public class BlobColumnAccess extends WrapperColumnAccess {

    public BlobColumnAccess(ColumnDefinition columnDefinition) {
        super(columnDefinition);
    }

    @Override
    String getColumnAccessString(String variableNameString, String elementName, boolean isModelContainerAdapter) {
        return CodeBlock.builder()
                .add("$L.getBlob()", getExistingColumnAccess()
                        .getColumnAccessString(variableNameString, elementName, isModelContainerAdapter))
                .build().toString();
    }

    @Override
    String getShortAccessString(String elementName, boolean isModelContainerAdapter) {
        return CodeBlock.builder()
                .add("$L.getBlob()", getExistingColumnAccess()
                        .getShortAccessString(elementName, isModelContainerAdapter))
                .build().toString();
    }

    @Override
    String setColumnAccessString(String variableNameString, String elementName, String formattedAccess, boolean isModelContainerAdapter) {
        String newFormattedAccess = CodeBlock.builder()
                .add("$L.setBlob($L)", variableNameString, formattedAccess)
                .build().toString();
        return getExistingColumnAccess()
                .setColumnAccessString(variableNameString, elementName, newFormattedAccess, isModelContainerAdapter);
    }
}
