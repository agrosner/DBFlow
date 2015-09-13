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
        if (isModelContainerAdapter) {
            return getExistingColumnAccess()
                    .getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isModelContainerAdapter);
        } else {
            return CodeBlock.builder()
                    .add("$L.name()", getExistingColumnAccess()
                            .getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isModelContainerAdapter))
                    .build().toString();
        }
    }

    @Override
    String getShortAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter) {
        if (isModelContainerAdapter) {
            return getExistingColumnAccess()
                    .getShortAccessString(fieldType, elementName, isModelContainerAdapter);
        } else {
            return CodeBlock.builder()
                    .add("$L.name()", getExistingColumnAccess()
                            .getShortAccessString(fieldType, elementName, isModelContainerAdapter))
                    .build().toString();
        }
    }

    @Override
    String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess) {
        CodeBlock newFormattedAccess = CodeBlock.builder()
                .add("$T.valueOf($L)", columnDefinition.elementTypeName, formattedAccess)
                .build();
        return getExistingColumnAccess()
                .setColumnAccessString(fieldType, elementName, fullElementName, isModelContainerAdapter, variableNameString, newFormattedAccess);
    }
}
