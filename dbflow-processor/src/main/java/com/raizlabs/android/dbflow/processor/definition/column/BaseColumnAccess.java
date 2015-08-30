package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.SQLiteType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

/**
 * Description: Simply defines how to access a column's field.
 */
public abstract class BaseColumnAccess {

    abstract String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter);

    abstract String getShortAccessString(String elementName, boolean isModelContainerAdapter);

    abstract String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, String formattedAccess);

    SQLiteType getSqliteTypeForTypeName(TypeName elementTypeName, boolean isModelContainerAdapter) {
        return SQLiteType.get(elementTypeName);
    }
}
