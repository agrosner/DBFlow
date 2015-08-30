package com.raizlabs.android.dbflow.processor.definition.column;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description:
 */
public class EnumColumnAccess extends WrapperColumnAccess {

    public EnumColumnAccess(ColumnDefinition columnDefinition) {
        super(columnDefinition);
    }

    @Override
    String getColumnAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter, String variableNameString) {
        return CodeBlock.builder()
                .add("$L.name()", getExistingColumnAccess()
                        .getColumnAccessString(fieldType, elementName, isModelContainerAdapter, variableNameString))
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
    String setColumnAccessString(TypeName fieldType, String elementName, String formattedAccess, boolean isModelContainerAdapter, String variableNameString) {
        String newFormattedAccess = CodeBlock.builder()
                .add("$T.valueOf($L)", columnDefinition.elementClassName, formattedAccess)
                .build().toString();
        return getExistingColumnAccess()
                .setColumnAccessString(fieldType, elementName, newFormattedAccess, isModelContainerAdapter, variableNameString);
    }
}
