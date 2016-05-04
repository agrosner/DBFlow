package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.SQLiteHelper;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description:
 */
public class PrivateColumnAccess extends BaseColumnAccess {

    private String getterName;
    private String setterName;
    public final boolean useBooleanSetters;

    public PrivateColumnAccess(Column column, boolean useBooleanSetters) {
        if (column != null) {
            getterName = column.getterName();
            setterName = column.setterName();
        }
        this.useBooleanSetters = useBooleanSetters;
    }

    public PrivateColumnAccess(ForeignKeyReference reference) {
        getterName = reference.referencedGetterName();
        setterName = reference.referencedSetterName();
        this.useBooleanSetters = false;
    }

    public PrivateColumnAccess(boolean useBooleanSetters) {
        this.useBooleanSetters = useBooleanSetters;
    }

    @Override
    public String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter, boolean isSqliteStatement) {
        if (!isModelContainerAdapter) {
            return String.format("%1s.%1s()", variableNameString, getGetterNameElement(elementName));
        } else {
            String method = SQLiteHelper.getModelContainerMethod(fieldType);
            if (method == null) {
                method = "get";
            }
            return variableNameString + "." + method + "Value(\"" + elementName + "\")";
        }
    }

    @Override
    public String getShortAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter, boolean isSqliteStatement) {
        if (!isModelContainerAdapter) {
            return String.format("%1s()", getGetterNameElement(elementName));
        } else {
            return elementName;
        }
    }

    @Override
    public String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess, boolean toModel) {
        if (isModelContainerAdapter) {
            return variableNameString + ".put(\"" + elementName + "\", " + formattedAccess + ")";
        } else {
            return String.format("%1s.%1s(%1s)", variableNameString, getSetterNameElement(elementName), formattedAccess);
        }
    }

    public String getGetterNameElement(String elementName) {
        if (StringUtils.isNullOrEmpty(getterName)) {
            if (useBooleanSetters && !elementName.startsWith("is")) {
                return "is" + StringUtils.capitalize(elementName);
            } else if (!useBooleanSetters && !elementName.startsWith("get")) {
                return "get" + StringUtils.capitalize(elementName);
            } else {
                return StringUtils.lower(elementName);
            }
        } else {
            return getterName;
        }
    }

    public String getSetterNameElement(String elementName) {
        if (StringUtils.isNullOrEmpty(setterName)) {
            if (!elementName.startsWith("set")) {
                if (useBooleanSetters && elementName.startsWith("is")) {
                    elementName = elementName.replaceFirst("is", "");
                }
                return "set" + StringUtils.capitalize(elementName);
            } else {
                return StringUtils.lower(elementName);
            }
        } else {
            return setterName;
        }
    }
}
