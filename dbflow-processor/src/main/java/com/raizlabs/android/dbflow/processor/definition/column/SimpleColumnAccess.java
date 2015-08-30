package com.raizlabs.android.dbflow.processor.definition.column;

/**
 * Description: Simplest of all fields. Simply returns "model.field".
 */
public class SimpleColumnAccess extends BaseColumnAccess {

    @Override
    String getColumnAccessString(String variableNameString, String elementName, boolean isModelContainerAdapter) {
        if (isModelContainerAdapter) {
            return variableNameString + ".getValue(\"" + elementName + "\")";
        } else {
            return variableNameString + "." + elementName;
        }
    }

    @Override
    String getShortAccessString(String elementName, boolean isModelContainerAdapter) {
        return elementName;
    }

    @Override
    String setColumnAccessString(String variableNameString, String elementName, String formattedAccess, boolean isModelContainerAdapter) {
        return getColumnAccessString(variableNameString, elementName, isModelContainerAdapter) + " = " + formattedAccess;
    }
}
