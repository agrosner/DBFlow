package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.SQLiteType;
import com.squareup.javapoet.TypeName;

/**
 * Description: Simply defines how to access a column's field.
 */
public abstract class BaseColumnAccess {

    abstract String getColumnAccessString(String variableNameString, String elementName);

    abstract String getShortAccessString(String elementName);

    abstract String setColumnAccessString(String variableNameString, String elementName, String formattedAccess);

    SQLiteType getSqliteTypeForTypeName(TypeName elementTypeName) {
        return SQLiteType.get(elementTypeName);
    }
}
