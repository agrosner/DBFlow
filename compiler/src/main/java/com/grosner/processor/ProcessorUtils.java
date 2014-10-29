package com.grosner.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ProcessorUtils {

    public static boolean implementsClass(ProcessingEnvironment processingEnvironment, String fqTn, TypeElement element) {
        TypeElement typeElement = processingEnvironment.getElementUtils().getTypeElement(fqTn);
        if (typeElement == null) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Type Element was null for: " + fqTn);
            return false;
        } else {
            TypeMirror classMirror = typeElement.asType();
            return processingEnvironment.getTypeUtils().isAssignable(element.asType(), classMirror);
        }
    }
}
