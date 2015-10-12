package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.SQLiteHelper;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description: Simplest of all fields. Simply returns "model.field".
 */
public class SimpleColumnAccess extends BaseColumnAccess {

    @Override
    public String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter) {
        if (isModelContainerAdapter) {
            String method = SQLiteHelper.getModelContainerMethod(fieldType);
            if (method == null) {
                method = "get";
            }
            return variableNameString + "." + method + "Value(\"" + elementName + "\")";
        } else {
            return variableNameString + "." + fullElementName;
        }
    }

    @Override
    public String getShortAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter) {
        return elementName;
    }

    @Override
    public String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess) {
        if (isModelContainerAdapter) {
            return variableNameString + ".put(\"" + elementName + "\", " + formattedAccess + ")";
        } else {
            return getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, false) + " = " + formattedAccess;
        }
    }
}
