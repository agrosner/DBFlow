package com.raizlabs.android.dbflow.processor.definition.column;

import com.squareup.javapoet.ClassName;
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
    public String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter, boolean isSqliteStatement) {
        if (isModelContainerAdapter) {
            return getExistingColumnAccess()
                    .getColumnAccessString(ClassName.get(String.class), elementName, fullElementName, variableNameString, isModelContainerAdapter, isSqliteStatement);
        } else {
            return CodeBlock.builder()
                    .add("$L.name()", getExistingColumnAccess()
                            .getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isModelContainerAdapter, isSqliteStatement))
                    .build().toString();
        }
    }

    @Override
    public String getShortAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter, boolean isSqliteStatement) {
        if (isModelContainerAdapter) {
            return getExistingColumnAccess()
                    .getShortAccessString(ClassName.get(String.class), elementName, isModelContainerAdapter, isSqliteStatement);
        } else {
            return CodeBlock.builder()
                    .add("$L.name()", getExistingColumnAccess()
                            .getShortAccessString(fieldType, elementName, isModelContainerAdapter, isSqliteStatement))
                    .build().toString();
        }
    }

    @Override
    public String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess, boolean toModel) {
        CodeBlock newFormattedAccess = CodeBlock.builder()
                .add("$T.valueOf($L)", columnDefinition.elementTypeName, formattedAccess)
                .build();
        return getExistingColumnAccess()
                .setColumnAccessString(ClassName.get(String.class), elementName, fullElementName, isModelContainerAdapter, variableNameString, newFormattedAccess, toModel);
    }
}
