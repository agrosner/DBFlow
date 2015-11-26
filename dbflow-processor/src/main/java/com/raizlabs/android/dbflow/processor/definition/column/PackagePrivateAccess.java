package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.SQLiteHelper;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description: Wraps the get call in a package-private access class so it can reference the field properly.
 */
public class PackagePrivateAccess extends BaseColumnAccess {

    public static final String classSuffix = "Helper";

    private final String elementPackageName;

    private final ClassName helperClassName;

    public PackagePrivateAccess(String elementPackageName, String separator, String className) {

        this.elementPackageName = elementPackageName;
        helperClassName = ClassName.get(elementPackageName, className + separator + classSuffix);
    }

    @Override
    public String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter, boolean isSqliteStatement) {
        if (!isModelContainerAdapter) {
            return CodeBlock.builder().add("$T.get$L($L.$L)", helperClassName, StringUtils.capitalize(elementName),
                    variableNameString, ModelUtils.getVariable(isModelContainerAdapter)).build().toString();
        } else {
            String method = SQLiteHelper.getMethod(fieldType);
            if (method == null) {
                method = "get";
            }
            return variableNameString + "." + method + "Value(\"" + elementName + "\")";
        }
    }

    @Override
    public String getShortAccessString(TypeName fieldType, String elementName, boolean isModelContainerAdapter, boolean isSqliteStatement) {
        if (!isModelContainerAdapter) {
            return CodeBlock.builder().add("$T.get$L($L)", helperClassName, StringUtils.capitalize(elementName),
                    ModelUtils.getVariable(isModelContainerAdapter)).build().toString();
        } else {
            return elementName;
        }
    }

    @Override
    public String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess, boolean toModel) {
        if (isModelContainerAdapter) {
            return variableNameString + ".put(\"" + elementName + "\", " + formattedAccess + ")";
        } else {
            return CodeBlock.builder().add("$T.set$L($L, $L)", helperClassName,
                    StringUtils.capitalize(elementName), ModelUtils.getVariable(isModelContainerAdapter),
                    formattedAccess).build().toString();
        }
    }
}
