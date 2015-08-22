package com.raizlabs.android.dbflow.processor.definition.column;

/**
 * Description: Simplest of all fields. Simply returns "model.field".
 */
public class SimpleColumnAccess extends BaseColumnAccess {

    @Override
    String getColumnAccessString(String variableNameString, String elementName) {
        return variableNameString + "." + elementName;
    }

    @Override
    String getShortAccessString(String elementName) {
        return elementName;
    }

    @Override
    String setColumnAccessString(String variableNameString, String elementName, String formattedAccess) {
        return getColumnAccessString(variableNameString, elementName) + " = " + formattedAccess;
    }
}
