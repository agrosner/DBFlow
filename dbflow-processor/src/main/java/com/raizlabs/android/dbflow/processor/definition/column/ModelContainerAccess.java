package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.SQLiteType;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description: Provides an easy way to wrap in model container accesses.
 */
public class ModelContainerAccess extends BaseColumnAccess {

    private final BaseColumnAccess existingColumnAccess;

    public String containerKeyName;

    public ModelContainerAccess(BaseColumnAccess existingColumnAccess, String containerKeyName) {
        this.existingColumnAccess = existingColumnAccess;
        this.containerKeyName = containerKeyName;
    }

    @Override
    public String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter) {
        String method = SQLiteType.getModelContainerMethod(fieldType);
        if (method == null) {
            method = "get";
        }
        return CodeBlock.builder()
                .add("$L.$LValue($S)",
                        existingColumnAccess.getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isModelContainerAdapter),
                        method,
                        containerKeyName)
                .build().toString();
    }

    @Override
    public String getShortAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter) {
        String method = SQLiteType.getModelContainerMethod(fieldType);
        if (method == null) {
            method = "get";
        }
        return CodeBlock.builder()
                .add("$LValue($S)", method, containerKeyName)
                .build().toString();
    }

    @Override
    public String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess) {
        CodeBlock newFormattedAccess = CodeBlock.builder()
                .add("$L.put($S, $L)", variableNameString, containerKeyName, formattedAccess)
                .build();
        return existingColumnAccess.setColumnAccessString(fieldType, elementName, fullElementName, isModelContainerAdapter, variableNameString, newFormattedAccess);
    }
}
