package com.raizlabs.android.dbflow.processor.definition.column;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */

public class ListColumnAccess extends WrapperColumnAccess {

    public ListColumnAccess(ColumnDefinition columnDefinition) {
        super(columnDefinition);
    }

    @Override
    public CodeBlock getColumnAccessString(TypeName fieldType, String elementName,
                                           String fullElementName, String variableNameString,
                                           boolean isSqliteStatement) {
        return null;
    }

    @Override
    public CodeBlock getShortAccessString(TypeName fieldType, String elementName,
                                          boolean isSqliteStatement) {
        return null;
    }

    @Override
    public CodeBlock setColumnAccessString(TypeName fieldType, String elementName,
                                           String fullElementName, String variableNameString,
                                           CodeBlock formattedAccess) {
        return null;
    }
}
