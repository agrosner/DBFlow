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
    String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter) {
        return CodeBlock.builder()
                .add("$L.name()", getExistingColumnAccess()
                        .getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isModelContainerAdapter))
                .build().toString();
    }

    @Override
    String getShortAccessString(boolean isModelContainerAdapter, String elementName) {
        return CodeBlock.builder()
                .add("$L.name()", getExistingColumnAccess()
                        .getShortAccessString(isModelContainerAdapter, elementName))
                .build().toString();
    }

    @Override
    String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess) {
        CodeBlock newFormattedAccess = CodeBlock.builder()
                .add("$T.valueOf($L)", columnDefinition.elementClassName, formattedAccess)
                .build();
        return getExistingColumnAccess()
                .setColumnAccessString(fieldType, elementName, fullElementName, isModelContainerAdapter, variableNameString, newFormattedAccess);
    }
}
