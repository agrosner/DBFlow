package com.raizlabs.android.dbflow.processor.definition.column;

import com.google.common.collect.Maps;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
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

    public final ClassName helperClassName;
    private final ClassName internalHelperClassName; // used for safety

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
    public static void putElement(ClassName className, String elementName) {
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

        if (separator.matches("[$]+")) {
            separator += separator; // duplicate to be safe
        }
        internalHelperClassName = ClassName.get(elementPackageName, className + separator + classSuffix);
    }

    @Override
    public CodeBlock getColumnAccessString(TypeName fieldType, String elementName,
                                           String fullElementName, String variableNameString,
                                           boolean isSqliteStatement) {
        return CodeBlock.builder().add("$T.get$L($L)", internalHelperClassName,
                StringUtils.capitalize(elementName),
                variableNameString).build();
    }

    @Override
    public CodeBlock getShortAccessString(TypeName fieldType, String elementName,
                                          boolean isSqliteStatement) {
        return CodeBlock.builder().add("$T.get$L($L)", internalHelperClassName,
                StringUtils.capitalize(elementName),
                elementName).build();
    }

    @Override
    public CodeBlock setColumnAccessString(TypeName fieldType, String elementName,
                                           String fullElementName,
                                           String variableNameString, CodeBlock formattedAccess) {
        return CodeBlock.builder().add("$T.set$L($L, $L)", helperClassName,
                StringUtils.capitalize(elementName), variableNameString,
                formattedAccess).build();
    }
}
