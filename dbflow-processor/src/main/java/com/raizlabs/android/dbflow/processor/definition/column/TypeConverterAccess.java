package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description: Supports type converters here.
 */
public class TypeConverterAccess extends BaseColumnAccess {

    private final ColumnDefinition columnDefinition;

    private final BaseColumnAccess existingColumnAccess;

    public final TypeConverterDefinition typeConverterDefinition;

    private final ProcessorManager manager;

    public TypeConverterAccess(ProcessorManager manager, ColumnDefinition columnDefinition) {

        this.columnDefinition = columnDefinition;
        this.existingColumnAccess = columnDefinition.columnAccess;
        typeConverterDefinition = manager.getTypeConverterDefinition(columnDefinition.elementTypeName.box());
        this.manager = manager;
    }

    @Override
    String getColumnAccessString(String variableNameString, String elementName) {
        return CodeBlock.builder()
                .add("($T) $T.getTypeConverter($T.class).getDBValue($L)",
                        typeConverterDefinition.dbClassName,
                        ClassNames.DB_MANAGER,
                        columnDefinition.elementTypeName.box(),
                        existingColumnAccess.getColumnAccessString(variableNameString, elementName))
                .build()
                .toString();
    }

    @Override
    String getShortAccessString(String elementName) {
        return CodeBlock.builder()
                .add("($T) $T.getTypeConverter($T.class).getDBValue($L)",
                        typeConverterDefinition.dbClassName,
                        ClassNames.DB_MANAGER,
                        columnDefinition.elementTypeName.box(),
                        existingColumnAccess.getShortAccessString(elementName))
                .build()
                .toString();
    }

    @Override
    String setColumnAccessString(String variableNameString, String elementName, String formattedAccess) {
        String newFormattedAccess = CodeBlock.builder()
                .add("($T) $T.getTypeConverter($T.class).getModelValue(($T) $L)",
                        typeConverterDefinition.modelClassName,
                        ClassNames.DB_MANAGER,
                        columnDefinition.elementTypeName.box(),
                        typeConverterDefinition.dbClassName,
                        formattedAccess).build().toString();
        return existingColumnAccess.setColumnAccessString(variableNameString, elementName, newFormattedAccess);
    }

    @Override
    SqliteConversions.SQLiteType getSqliteTypeForTypeName(TypeName elementTypeName) {
        if (typeConverterDefinition == null) {
            manager.logError(TypeConverterAccess.class, "No type converter definition found for %1s. Please register it via annotations.", elementTypeName);
            throw new RuntimeException("");
        }

        return super.getSqliteTypeForTypeName(typeConverterDefinition.dbClassName);
    }
}
