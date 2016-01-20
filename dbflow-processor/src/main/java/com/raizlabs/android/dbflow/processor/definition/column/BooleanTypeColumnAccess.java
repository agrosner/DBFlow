package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description:
 */
public class BooleanTypeColumnAccess extends WrapperColumnAccess {

    public BooleanTypeColumnAccess(ColumnDefinition columnDefinition) {
        super(columnDefinition);
    }

    @Override
    public String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter, boolean isSqliteStatement) {
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.add(getExistingColumnAccess().getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isModelContainerAdapter, isSqliteStatement));
        if (!isModelContainerAdapter || isSqliteStatement) {
            codeBuilder.add(" ? 1 : 0");
        }
        return codeBuilder.build().toString();
    }

    @Override
    public String getShortAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter, boolean isSqliteStatement) {
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.add(getExistingColumnAccess().getShortAccessString(fieldType, elementName, isModelContainerAdapter, isSqliteStatement));
        if (!isModelContainerAdapter || isSqliteStatement) {
            codeBuilder.add(" ? 1 : 0");
        }
        return codeBuilder.build().toString();
    }

    @Override
    public String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess, boolean toModel) {
        CodeBlock finalAccess;
        if (!isModelContainerAdapter && !toModel) {
            finalAccess = CodeBlock.builder().add("$L == 1 ? true : false", formattedAccess).build();
        } else {
            finalAccess = formattedAccess;
        }
        return CodeBlock.builder().add(getExistingColumnAccess().setColumnAccessString(fieldType, elementName, fullElementName, isModelContainerAdapter, variableNameString, finalAccess, toModel)).build().toString();
    }
}
