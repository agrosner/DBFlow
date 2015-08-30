package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.SQLiteType;
import com.squareup.javapoet.TypeName;

/**
 * Description: Simply defines how to access a column's field.
 */
public abstract class BaseColumnAccess {

    abstract String getColumnAccessString(String variableNameString, String elementName, boolean isModelContainerAdapter);

    abstract String getShortAccessString(String elementName, boolean isModelContainerAdapter);

    abstract String setColumnAccessString(String variableNameString, String elementName, String formattedAccess, boolean isModelContainerAdapter);

    SQLiteType getSqliteTypeForTypeName(TypeName elementTypeName, boolean isModelContainerAdapter) {
        return SQLiteType.get(elementTypeName);
    }
}
