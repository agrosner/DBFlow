package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.SQLiteType;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description:
 */
public class PrivateColumnAccess extends BaseColumnAccess {

    private String getterName;
    private String setterName;
    private final boolean useIsForGetter;

    public PrivateColumnAccess(Column column, boolean useIsForGetter) {
        getterName = column.getterName();
        setterName = column.setterName();
        this.useIsForGetter = useIsForGetter;
    }

    public PrivateColumnAccess(ForeignKeyReference reference) {
        getterName = reference.referencedGetterName();
        setterName = reference.referencedSetterName();
        this.useIsForGetter = false;
    }

    @Override
    public String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter) {
        if (!isModelContainerAdapter) {
            if (StringUtils.isNullOrEmpty(getterName)) {
                return String.format("%1s.%1s()", variableNameString, getGetterName(elementName));
            } else {
                return String.format("%1s.%1s()", variableNameString, getterName);
            }
        } else {
            String method = SQLiteType.getMethod(fieldType);
            if (method == null) {
                method = "get";
            }
            return variableNameString + "." + method + "Value(\"" + elementName + "\")";
        }
    }

    @Override
    public String getShortAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter) {
        if (!isModelContainerAdapter) {
            if (StringUtils.isNullOrEmpty(getterName)) {
                return String.format("%1s()", getGetterName(elementName));
            } else {
                return String.format("%1s()", getterName);
            }
        } else {
            return elementName;
        }
    }

    @Override
    public String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess) {
        if (isModelContainerAdapter) {
            return variableNameString + ".put(\"" + elementName + "\", " + formattedAccess + ")";
        } else {
            if (StringUtils.isNullOrEmpty(setterName)) {
                return String.format("%1s.set%1s(%1s)", variableNameString, StringUtils.capitalize(elementName), formattedAccess);
            } else {
                return String.format("%1s.%1s(%1s)", variableNameString, setterName, formattedAccess);
            }
        }
    }

    private String getGetterName(String elementName) {
        String finalName = elementName;
        if (useIsForGetter && !finalName.startsWith("is")) {
            finalName = "is" + StringUtils.capitalize(finalName);
        } else if (!useIsForGetter && !finalName.startsWith("get")) {
            finalName = "get" + StringUtils.capitalize(finalName);
        }
        return finalName;
    }

}
