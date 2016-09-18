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
    public CodeBlock getColumnAccessString(TypeName fieldType, String elementName,
                                           String fullElementName, String variableNameString,
                                           boolean isSqliteStatement) {
        return CodeBlock.of(dontAppendModel ? elementName : (variableNameString + ".") + fullElementName);
    }

    @Override
    public CodeBlock getShortAccessString(TypeName fieldType, String elementName,
                                          boolean isSqliteStatement) {
        return CodeBlock.of(elementName);
    }

    @Override
    public CodeBlock setColumnAccessString(TypeName fieldType, String elementName,
                                           String fullElementName,
                                           String variableNameString, CodeBlock formattedAccess) {
        return CodeBlock.of("$L = $L", getColumnAccessString(fieldType, elementName, fullElementName,
                variableNameString, false), formattedAccess);
    }
}
