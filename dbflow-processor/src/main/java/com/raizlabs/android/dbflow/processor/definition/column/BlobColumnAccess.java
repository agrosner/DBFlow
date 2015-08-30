package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.data.Blob;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description: Defines how to access a {@link Blob}.
 */
public class BlobColumnAccess extends WrapperColumnAccess {

    public BlobColumnAccess(ColumnDefinition columnDefinition) {
        super(columnDefinition);
    }

    @Override
    String getColumnAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter, String variableNameString) {
        return CodeBlock.builder()
                .add("$L.getBlob()", getExistingColumnAccess()
                        .getColumnAccessString(fieldType, elementName, isModelContainerAdapter, variableNameString))
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
    String setColumnAccessString(TypeName fieldType, String elementName, String formattedAccess, boolean isModelContainerAdapter, String variableNameString) {
        String newFormattedAccess = CodeBlock.builder()
                .add("$L.setBlob($L)", variableNameString, formattedAccess)
                .build().toString();
        return getExistingColumnAccess()
                .setColumnAccessString(fieldType, elementName, newFormattedAccess, isModelContainerAdapter, variableNameString);
    }
}
