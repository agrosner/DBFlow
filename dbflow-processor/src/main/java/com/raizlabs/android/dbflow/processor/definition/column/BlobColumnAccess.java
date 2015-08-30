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
    String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter) {
        return CodeBlock.builder()
                .add("$L.getBlob()", getExistingColumnAccess()
                        .getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isModelContainerAdapter))
                .build().toString();
    }

    @Override
    String getShortAccessString(boolean isModelContainerAdapter, String elementName) {
        return CodeBlock.builder()
                .add("$L.getBlob()", getExistingColumnAccess()
                        .getShortAccessString(isModelContainerAdapter, elementName))
                .build().toString();
    }

    @Override
    String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess) {
        CodeBlock newFormattedAccess = CodeBlock.builder()
                .add("$L.setBlob($L)", variableNameString, formattedAccess)
                .build();
        return getExistingColumnAccess()
                .setColumnAccessString(fieldType, elementName, fullElementName, isModelContainerAdapter, variableNameString, newFormattedAccess);
    }
}
