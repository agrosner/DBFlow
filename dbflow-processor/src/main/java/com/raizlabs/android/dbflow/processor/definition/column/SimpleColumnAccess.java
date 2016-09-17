package com.raizlabs.android.dbflow.processor.definition.column;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description: Simplest of all fields. Simply returns "model.field".
 */
public class SimpleColumnAccess extends BaseColumnAccess {

    private final boolean dontAppendModel;

    public SimpleColumnAccess() {
        this(false);
    }

    public SimpleColumnAccess(boolean dontAppendModel) {

        this.dontAppendModel = dontAppendModel;
    }

    @Override
    public String getColumnAccessString(TypeName fieldType, String elementName,
                                        String fullElementName, String variableNameString,
                                        boolean isSqliteStatement) {
        return dontAppendModel ? elementName : (variableNameString + ".") + fullElementName;
    }

    @Override
    public String getShortAccessString(TypeName fieldType, String elementName,
                                       boolean isSqliteStatement) {
        return elementName;
    }

    @Override
    public String setColumnAccessString(TypeName fieldType, String elementName,
                                        String fullElementName,
                                        String variableNameString, CodeBlock formattedAccess,
                                        boolean toModel) {
        return getColumnAccessString(fieldType, elementName, fullElementName,
                variableNameString, false) + " = " + formattedAccess;
    }
}
