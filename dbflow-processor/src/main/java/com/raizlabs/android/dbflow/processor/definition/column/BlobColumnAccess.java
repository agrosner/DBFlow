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
    String getColumnAccessString(String variableNameString, String elementName) {
        return CodeBlock.builder()
                .add("$L.getBlob()", existingColumnAccess.getColumnAccessString(variableNameString, elementName))
                .build().toString();
    }

    @Override
    String getShortAccessString(String elementName) {
        return CodeBlock.builder()
                .add("$L.getBlob()", existingColumnAccess.getShortAccessString(elementName))
                .build().toString();
    }

    @Override
    String setColumnAccessString(String variableNameString, String elementName, String formattedAccess) {
        String newFormattedAccess = CodeBlock.builder()
                .add("$L.setBlob($L)", variableNameString, formattedAccess)
                .build().toString();
        return existingColumnAccess.setColumnAccessString(variableNameString, elementName, newFormattedAccess);
    }
}
