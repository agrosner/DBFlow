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
    public CodeBlock getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isSqliteStatement) {
        return CodeBlock.builder()
                .add("$L.name()", getExistingColumnAccess()
                        .getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isSqliteStatement))
                .build();
    }

    @Override
    public CodeBlock getShortAccessString(TypeName fieldType, String elementName, boolean isSqliteStatement) {
        return CodeBlock.builder()
                .add("$L.name()", getExistingColumnAccess()
                        .getShortAccessString(fieldType, elementName, isSqliteStatement))
                .build();
    }

    @Override
    public CodeBlock setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, CodeBlock formattedAccess) {
        CodeBlock newFormattedAccess = CodeBlock.builder()
                .add("$T.valueOf($L)", columnDefinition.elementTypeName, formattedAccess)
                .build();
        return getExistingColumnAccess()
                .setColumnAccessString(ClassName.get(String.class), elementName, fullElementName, variableNameString, newFormattedAccess);
    }
}
