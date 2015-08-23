package com.raizlabs.android.dbflow.processor.definition.column;

import com.squareup.javapoet.CodeBlock;

/**
 * Description:
 */
public class EnumColumnAccess extends WrapperColumnAccess {

    public EnumColumnAccess(ColumnDefinition columnDefinition) {
        super(columnDefinition);
    }

    @Override
    String getColumnAccessString(String variableNameString, String elementName) {
        return CodeBlock.builder()
                .add("$L.name()", existingColumnAccess.getColumnAccessString(variableNameString, elementName))
                .build().toString();
    }

    @Override
    String getShortAccessString(String elementName) {
        return CodeBlock.builder()
                .add("$L.name()", existingColumnAccess.getShortAccessString(elementName))
                .build().toString();
    }

    @Override
    String setColumnAccessString(String variableNameString, String elementName, String formattedAccess) {
        String newFormattedAccess = CodeBlock.builder()
                .add("$T.valueOf($L)", columnDefinition.elementClassName, formattedAccess)
                .build().toString();
        return existingColumnAccess.setColumnAccessString(variableNameString, elementName, newFormattedAccess);
    }
}
