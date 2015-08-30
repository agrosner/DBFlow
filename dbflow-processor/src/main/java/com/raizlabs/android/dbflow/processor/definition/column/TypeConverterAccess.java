package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.SQLiteType;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description: Supports type converters here.
 */
public class TypeConverterAccess extends WrapperColumnAccess {

    private static final String METHOD_TYPE_CONVERTER = "getTypeConverterForClass";

    public final TypeConverterDefinition typeConverterDefinition;

    private final ProcessorManager manager;

    public TypeConverterAccess(ProcessorManager manager, ColumnDefinition columnDefinition) {
        super(columnDefinition);
        typeConverterDefinition = manager.getTypeConverterDefinition(columnDefinition.elementTypeName.box());
        this.manager = manager;
    }

    @Override
    String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter) {
        return CodeBlock.builder()
                .add("($T) $T.$L($T.class).getDBValue($L)",
                        typeConverterDefinition.getDbTypeName(),
                        ClassNames.FLOW_MANAGER,
                        METHOD_TYPE_CONVERTER,
                        columnDefinition.elementTypeName.box(),
                        getExistingColumnAccess()
                                .getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isModelContainerAdapter))
                .build()
                .toString();
    }

    @Override
    String getShortAccessString(String elementName, boolean isModelContainerAdapter) {
        return CodeBlock.builder()
                .add("($T) $T.$L($T.class).getDBValue($L)",
                        typeConverterDefinition.getDbTypeName(),
                        ClassNames.FLOW_MANAGER,
                        METHOD_TYPE_CONVERTER,
                        columnDefinition.elementTypeName.box(),
                        getExistingColumnAccess()
                                .getShortAccessString(elementName, isModelContainerAdapter))
                .build()
                .toString();
    }

    @Override
    String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, String formattedAccess) {
        String newFormattedAccess = CodeBlock.builder()
                .add("($T) $T.$L($T.class).getModelValue(($T) $L)",
                        typeConverterDefinition.getModelTypeName(),
                        ClassNames.FLOW_MANAGER,
                        METHOD_TYPE_CONVERTER,
                        columnDefinition.elementTypeName.box(),
                        typeConverterDefinition.getDbTypeName(),
                        formattedAccess).build().toString();
        return getExistingColumnAccess()
                .setColumnAccessString(fieldType, elementName, fullElementName, isModelContainerAdapter, variableNameString, newFormattedAccess);
    }

    @Override
    SQLiteType getSqliteTypeForTypeName(TypeName elementTypeName, boolean isModelContainerAdapter) {
        if (typeConverterDefinition == null) {
            manager.logError(TypeConverterAccess.class, "No type converter definition found for %1s. Please register it via annotations.", elementTypeName);
            throw new RuntimeException("");
        }

        return super.getSqliteTypeForTypeName(typeConverterDefinition.getDbTypeName(), isModelContainerAdapter);
    }
}
