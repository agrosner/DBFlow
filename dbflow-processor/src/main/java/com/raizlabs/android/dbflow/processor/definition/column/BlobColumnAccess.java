package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.processor.SQLiteHelper;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
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
    public String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter) {
        if (isModelContainerAdapter) {
            return getExistingColumnAccess().getColumnAccessString(ArrayTypeName.of(TypeName.BYTE),
                    elementName, fullElementName, variableNameString, isModelContainerAdapter);
        } else {
            return CodeBlock.builder()
                    .add("$L.getBlob()", getExistingColumnAccess()
                            .getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isModelContainerAdapter))
                    .build().toString();
        }
    }

    @Override
    public String getShortAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter) {
        if (isModelContainerAdapter) {
            return getExistingColumnAccess().getShortAccessString(ArrayTypeName.of(TypeName.BYTE),
                    elementName, isModelContainerAdapter);
        } else {
            return CodeBlock.builder()
                    .add("$L.getBlob()", getExistingColumnAccess()
                            .getShortAccessString(fieldType, elementName, isModelContainerAdapter))
                    .build().toString();
        }
    }

    @Override
    public String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess) {
        CodeBlock newFormattedAccess = CodeBlock.builder()
                .add("new $T($L)", ClassName.get(Blob.class), formattedAccess)
                .build();
        return getExistingColumnAccess()
                .setColumnAccessString(ArrayTypeName.of(TypeName.BYTE), elementName, fullElementName, isModelContainerAdapter, variableNameString, newFormattedAccess);
    }

    @Override
    SQLiteHelper getSqliteTypeForTypeName(TypeName elementTypeName, boolean isModelContainerAdapter) {
        return SQLiteHelper.BLOB;
    }
}
