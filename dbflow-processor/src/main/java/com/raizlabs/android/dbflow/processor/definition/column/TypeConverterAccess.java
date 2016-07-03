package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.SQLiteHelper;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description: Supports type converters here.
 */
public class TypeConverterAccess extends WrapperColumnAccess {

    private static final String METHOD_TYPE_CONVERTER = "getTypeConverterForClass";

    public final TypeConverterDefinition typeConverterDefinition;

    private final ProcessorManager manager;
    private String typeConverterFieldName;

    public TypeConverterAccess(ProcessorManager manager, ColumnDefinition columnDefinition) {
        super(columnDefinition);
        typeConverterDefinition = manager.getTypeConverterDefinition(columnDefinition.elementTypeName.box());
        this.manager = manager;
    }

    public TypeConverterAccess(ProcessorManager manager, ColumnDefinition columnDefinition, TypeConverterDefinition typeConverterDefinition, String typeConverterFieldName) {
        super(columnDefinition);
        this.manager = manager;
        this.typeConverterFieldName = typeConverterFieldName;
        this.typeConverterDefinition = typeConverterDefinition;
    }

    @Override
    public String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter, boolean isSqliteStatement) {
        checkConverter();
        if (typeConverterDefinition != null) {
            CodeBlock.Builder codeBuilder = CodeBlock.builder();
            if (typeConverterFieldName == null) {
                codeBuilder.add("($T) $T.$L($T.class)", typeConverterDefinition.getDbTypeName(),
                        ClassNames.FLOW_MANAGER,
                        METHOD_TYPE_CONVERTER,
                        columnDefinition.elementTypeName.box());
            } else {
                codeBuilder.add(typeConverterFieldName);
            }
            codeBuilder.add(".getDBValue(($T) $L)", typeConverterDefinition.getModelTypeName(), getExistingColumnAccess()
                    .getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isModelContainerAdapter, isSqliteStatement));


            return codeBuilder.build().toString();
        } else {
            return "";
        }
    }

    @Override
    public String getShortAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter, boolean isSqliteStatement) {
        checkConverter();
        if (typeConverterDefinition != null) {
            CodeBlock.Builder codeBuilder = CodeBlock.builder();
            if (typeConverterFieldName == null) {
                codeBuilder.add("($T) $T.$L($T.class)",
                        typeConverterDefinition.getDbTypeName(),
                        ClassNames.FLOW_MANAGER,
                        METHOD_TYPE_CONVERTER,
                        columnDefinition.elementTypeName.box());
            } else {
                codeBuilder.add(typeConverterFieldName);
            }
            codeBuilder.add(".getDBValue($L)", getExistingColumnAccess()
                    .getShortAccessString(fieldType, elementName, isModelContainerAdapter, isSqliteStatement));


            return codeBuilder.build().toString();
        } else {
            return "";
        }
    }

    @Override
    public String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess, boolean toModel) {
        checkConverter();
        if (typeConverterDefinition != null) {
            CodeBlock.Builder newFormattedAccess = CodeBlock.builder();
            if (typeConverterFieldName == null) {
                newFormattedAccess.add("($T) $T.$L($T.class)",
                        typeConverterDefinition.getModelTypeName(),
                        ClassNames.FLOW_MANAGER,
                        METHOD_TYPE_CONVERTER,
                        columnDefinition.elementTypeName.box()).build();
            } else {
                newFormattedAccess.add(typeConverterFieldName);
            }

            String newCursorAccess = formattedAccess.toString();
            if (typeConverterDefinition.getDbTypeName().equals(ClassName.get(Blob.class))) {
                newCursorAccess = CodeBlock.builder().add("new $T($L)", ClassName.get(Blob.class),
                        newCursorAccess).build().toString();
            }

            newFormattedAccess.add(".getModelValue($L)", newCursorAccess);

            return getExistingColumnAccess()
                    .setColumnAccessString(fieldType, elementName, fullElementName, isModelContainerAdapter, variableNameString, newFormattedAccess.build(), toModel);
        } else {
            return "";
        }
    }

    @Override
    SQLiteHelper getSqliteTypeForTypeName(TypeName elementTypeName, boolean isModelContainerAdapter) {
        checkConverter();
        if (typeConverterDefinition != null) {
            return super.getSqliteTypeForTypeName(typeConverterDefinition.getDbTypeName(), isModelContainerAdapter);
        } else {
            return SQLiteHelper.TEXT;
        }
    }

    private void checkConverter() {
        if (typeConverterDefinition == null) {
            manager.logError("No type converter for: " + columnDefinition.elementTypeName + " -> " + columnDefinition.elementName + " from class: " + columnDefinition.tableDefinition.elementClassName + ". Please" +
                    "register with a TypeConverter.");
        }
    }
}
