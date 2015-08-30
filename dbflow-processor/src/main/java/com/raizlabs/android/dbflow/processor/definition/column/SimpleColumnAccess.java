package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.SQLiteType;
import com.squareup.javapoet.TypeName;

/**
 * Description: Simplest of all fields. Simply returns "model.field".
 */
public class SimpleColumnAccess extends BaseColumnAccess {

    @Override
    String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter) {
        if (isModelContainerAdapter) {
            String method = SQLiteType.getMethod(fieldType);
            if (method == null) {
                method = "get";
            }
            return variableNameString + "." + method + "Value(\"" + elementName + "\")";
        } else {
            return variableNameString + "." + fullElementName;
        }
    }

    @Override
    String getShortAccessString(String elementName, boolean isModelContainerAdapter) {
        return elementName;
    }

    @Override
    String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, String formattedAccess) {
        if (isModelContainerAdapter) {
            return variableNameString + ".put(\"" + elementName + "\", " + formattedAccess + ")";
        } else {
            return getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, false) + " = " + formattedAccess;
        }
    }
}
