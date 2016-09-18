package com.raizlabs.android.dbflow.processor.definition.column;

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
    public CodeBlock getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isSqliteStatement) {
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.add(getExistingColumnAccess().getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isSqliteStatement));
        if (isSqliteStatement) {
            codeBuilder.add(" ? 1 : 0");
        }
        return codeBuilder.build();
    }

    @Override
    public CodeBlock getShortAccessString(TypeName fieldType, String elementName, boolean isSqliteStatement) {
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.add(getExistingColumnAccess().getShortAccessString(fieldType, elementName, isSqliteStatement));
        if (isSqliteStatement) {
            codeBuilder.add(" ? 1 : 0");
        }
        return codeBuilder.build();
    }

    @Override
    public CodeBlock setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, CodeBlock formattedAccess) {
        CodeBlock finalAccess;
        finalAccess = CodeBlock.builder().add("$L == 1 ? true : false", formattedAccess).build();
        return CodeBlock.builder().add(getExistingColumnAccess().setColumnAccessString(fieldType,
                elementName, fullElementName, variableNameString, finalAccess))
                .build();
    }
}
