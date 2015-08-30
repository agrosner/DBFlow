package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.SQLiteType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

/**
 * Description: Simply defines how to access a column's field.
 */
public abstract class BaseColumnAccess {

    abstract String getColumnAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter, String variableNameString);

    abstract String getShortAccessString(String elementName, boolean isModelContainerAdapter);

    abstract String setColumnAccessString(TypeName fieldType, String elementName, String formattedAccess, boolean isModelContainerAdapter, String variableNameString);

    SQLiteType getSqliteTypeForTypeName(TypeName elementTypeName, boolean isModelContainerAdapter) {
        return SQLiteType.get(elementTypeName);
    }
}
