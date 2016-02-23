package com.raizlabs.android.dbflow.processor.utils;

import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Description:
 */
public class ElementUtility {

    /**
     * @param element
     * @param manager
     * @return real full-set of elements, including ones from super-class.
     */
    public static List<? extends Element> getAllElements(TypeElement element, ProcessorManager manager) {
        List<Element> elements = new ArrayList<>(manager.getElements().getAllMembers(element));
        TypeMirror superMirror = null;
        TypeElement typeElement = element;
        while ((superMirror = typeElement.getSuperclass()) != null) {
            typeElement = (TypeElement) manager.getTypeUtils().asElement(superMirror);
            if (typeElement == null) {
                break;
            }
            List<? extends Element> superElements = manager.getElements().getAllMembers(typeElement);
            for (Element superElement : superElements) {
                if (!elements.contains(superElement)) {
                    elements.add(superElement);
                }
            }
        }
        return elements;
    }

    public static boolean isInSamePackage(ProcessorManager manager, Element elementToCheck, Element original) {
        return
                manager.getElements().getPackageOf(elementToCheck).toString().equals(manager.getElements().getPackageOf(original).toString());
    }

    public static boolean isPackagePrivate(Element element) {
        return !element.getModifiers().contains(Modifier.PUBLIC) && !element.getModifiers().contains(Modifier.PRIVATE)
                && !element.getModifiers().contains(Modifier.STATIC);
    }

    public static boolean isValidAllFields(boolean allFields, Element element) {
        return (allFields && (element.getKind().isField() &&
                !element.getModifiers().contains(Modifier.STATIC) &&
                !element.getModifiers().contains(Modifier.FINAL))) &&
                element.getAnnotation(ColumnIgnore.class) == null &&
                !element.asType().toString().equals(ClassNames.MODEL_ADAPTER.toString());
    }
}
