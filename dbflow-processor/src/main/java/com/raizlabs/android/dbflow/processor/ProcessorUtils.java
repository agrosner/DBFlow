package com.raizlabs.android.dbflow.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Description: Provides handy methods for processing
 */
public class ProcessorUtils {

    /**
     * Whether the specified element is assignable to the fqTn parameter
     *
     * @param processingEnvironment The environment this runs in
     * @param fqTn                  THe fully qualified type name of the element we want to check
     * @param element               The element to check that implements
     * @return true if element implements the fqTn
     */
    public static boolean implementsClass(ProcessingEnvironment processingEnvironment, String fqTn, TypeElement element) {
        TypeElement typeElement = processingEnvironment.getElementUtils().getTypeElement(fqTn);
        if (typeElement == null) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Type Element was null for: " + fqTn + "" +
                    "ensure that the visibility of the class is not private.");
            return false;
        } else {
            TypeMirror classMirror = typeElement.asType();
            if (classMirror != null) {
                classMirror = processingEnvironment.getTypeUtils().erasure(classMirror);
            }
            return classMirror != null && element != null && element.asType() != null &&
                    processingEnvironment.getTypeUtils().isAssignable(element.asType(), classMirror);
        }
    }

    /**
     * Whether the specified element is assignable to the fqTn parameter
     *
     * @param processingEnvironment The environment this runs in
     * @param fqTn                  THe fully qualified type name of the element we want to check
     * @param element               The element to check that implements
     * @return true if element implements the fqTn
     */
    public static boolean isSubclass(ProcessingEnvironment processingEnvironment, String fqTn, TypeElement element) {
        TypeElement typeElement = processingEnvironment.getElementUtils().getTypeElement(fqTn);
        if (typeElement == null) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Type Element was null for: " + fqTn + "" +
                    "ensure that the visibility of the class is not private.");
            return false;
        } else {
            TypeMirror classMirror = typeElement.asType();
            return classMirror != null && element != null && element.asType() != null && processingEnvironment.getTypeUtils().isSubtype(element.asType(), classMirror);
        }
    }

    public static boolean isSubclassOf(String columnFieldType, Class<?> enumClass) {
        boolean isSubClass = false;
        try {
            Class type = Class.forName(columnFieldType);
            isSubClass = type.getSuperclass() != null && (type.getSuperclass().equals(enumClass) ||
                    isSubclassOf(type.getSuperclass().getName(), enumClass));
        } catch (ClassNotFoundException e) {
        }
        return isSubClass;
    }

}
