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
    String getColumnAccessString(String variableNameString, String elementName, boolean isModelContainerAdapter) {
        return CodeBlock.builder()
                .add("$L.name()", getExistingColumnAccess()
                        .getColumnAccessString(variableNameString, elementName, isModelContainerAdapter))
                .build().toString();
    }

    @Override
    String getShortAccessString(String elementName, boolean isModelContainerAdapter) {
        return CodeBlock.builder()
                .add("$L.name()", getExistingColumnAccess()
                        .getShortAccessString(elementName, isModelContainerAdapter))
                .build().toString();
    }

    @Override
    String setColumnAccessString(String variableNameString, String elementName, String formattedAccess, boolean isModelContainerAdapter) {
        String newFormattedAccess = CodeBlock.builder()
                .add("$T.valueOf($L)", columnDefinition.elementClassName, formattedAccess)
                .build().toString();
        return getExistingColumnAccess()
                .setColumnAccessString(variableNameString, elementName, newFormattedAccess, isModelContainerAdapter);
    }
}
