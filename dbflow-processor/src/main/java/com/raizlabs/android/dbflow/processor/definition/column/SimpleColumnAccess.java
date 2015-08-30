package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.SQLiteType;
import com.squareup.javapoet.TypeName;

/**
 * Description: Simplest of all fields. Simply returns "model.field".
 */
public class SimpleColumnAccess extends BaseColumnAccess {

    @Override
    String getColumnAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter, String variableNameString) {
        if (isModelContainerAdapter) {
            String method = SQLiteType.getMethod(fieldType);
            if (method == null) {
                method = "get";
            }
            return variableNameString + "." + method + "Value(\"" + elementName + "\")";
        } else {
            return variableNameString + "." + elementName;
        }
    }

    @Override
    String getShortAccessString(String elementName, boolean isModelContainerAdapter) {
        return elementName;
    }

    @Override
    String setColumnAccessString(TypeName fieldType, String elementName, String formattedAccess, boolean isModelContainerAdapter, String variableNameString) {
        if (isModelContainerAdapter) {
            return variableNameString + ".put(\"" + elementName + "\", " + formattedAccess + ")";
        } else {
            return getColumnAccessString(fieldType, elementName, false, variableNameString) + " = " + formattedAccess;
        }
    }
}
