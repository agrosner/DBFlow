package com.raizlabs.android.dbflow.processor.definition.column;

import com.google.common.collect.Maps;
import com.raizlabs.android.dbflow.processor.SQLiteHelper;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Description: Wraps the get call in a package-private access class so it can reference the field properly.
 */
public class PackagePrivateAccess extends BaseColumnAccess {

    public static final String classSuffix = "Helper";

    private final ClassName helperClassName;

    private static final Map<ClassName, List<String>> helperUsedMethodMap = Maps.newHashMap();

    public static boolean containsColumn(ClassName className, String columnName) {
        List<String> list = helperUsedMethodMap.get(className);
        if (list == null) {
            return false;
        } else {
            return list.contains(columnName);
        }
    }

    /**
     * Ensures we only map and use a package private field generated access method if its necessary.
     *
     * @param className
     * @param elementName
     */
    private static void putElement(ClassName className, String elementName) {
        List<String> list = helperUsedMethodMap.get(className);
        if (list == null) {
            list = new ArrayList<>();
            helperUsedMethodMap.put(className, list);
        }
        if (!list.contains(elementName)) {
            list.add(elementName);
        }
    }

    public static PackagePrivateAccess from(ProcessorManager processorManager, Element columnElement, String classSeparator) {
        return new PackagePrivateAccess(processorManager.getElements().getPackageOf(columnElement).toString(),
                classSeparator, ClassName.get((TypeElement) columnElement.getEnclosingElement()).simpleName());
    }

    public PackagePrivateAccess(String elementPackageName, String separator, String className) {
        helperClassName = ClassName.get(elementPackageName, className + separator + classSuffix);
    }

    @Override
    public String getColumnAccessString(TypeName fieldType, String elementName, String fullElementName, String variableNameString, boolean isModelContainerAdapter, boolean isSqliteStatement) {
        if (!isModelContainerAdapter) {
            putElement(helperClassName, elementName);
            return CodeBlock.builder().add("$T.get$L($L)", helperClassName, StringUtils.capitalize(elementName),
                    variableNameString).build().toString();
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
            putElement(helperClassName, elementName);
            return CodeBlock.builder().add("$T.get$L($L)", helperClassName, StringUtils.capitalize(elementName),
                    elementName).build().toString();
        } else {
            return elementName;
        }
    }

    @Override
    public String setColumnAccessString(TypeName fieldType, String elementName, String fullElementName, boolean isModelContainerAdapter, String variableNameString, CodeBlock formattedAccess, boolean toModel) {
        if (isModelContainerAdapter) {
            return variableNameString + ".put(\"" + elementName + "\", " + formattedAccess + ")";
        } else {
            putElement(helperClassName, elementName);
            return CodeBlock.builder().add("$T.set$L($L, $L)", helperClassName,
                    StringUtils.capitalize(elementName), ModelUtils.getVariable(isModelContainerAdapter),
                    formattedAccess).build().toString();
        }
    }
}
