package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.SQLiteHelper;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description: Simply defines how to access a column's field.
 */
public abstract class BaseColumnAccess {

    public abstract String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter, boolean isSqliteStatement);

    public abstract String getShortAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter, boolean isSqliteStatement);

    public abstract String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess, boolean toModel);

    SQLiteHelper getSqliteTypeForTypeName(TypeName elementTypeName, boolean isModelContainerAdapter) {
        return SQLiteHelper.get(elementTypeName);
    }
}
