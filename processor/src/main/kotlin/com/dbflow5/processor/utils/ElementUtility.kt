package com.dbflow5.processor.utils

import com.dbflow5.annotation.ColumnIgnore
import com.dbflow5.processor.ProcessorManager
import com.squareup.javapoet.ClassName
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Description:
 */
object ElementUtility {

    /**
     * @return real full-set of elements, including ones from super-class.
     */
    fun getAllElements(element: TypeElement, manager: ProcessorManager): List<Element> {
        val elements = manager.elements.getAllMembers(element).toMutableList()
        var superMirror: TypeMirror? = null
        var typeElement: TypeElement? = element
        while (typeElement?.superclass.let { superMirror = it; it != null }) {
            typeElement = manager.typeUtils.asElement(superMirror) as TypeElement?
            typeElement?.let {
                val superElements = manager.elements.getAllMembers(typeElement)
                superElements.forEach { if (!elements.contains(it)) elements += it }
            }
        }
        return elements
    }

    fun isInSamePackage(manager: ProcessorManager, elementToCheck: Element, original: Element): Boolean {
        return manager.elements.getPackageOf(elementToCheck).toString() == manager.elements.getPackageOf(original).toString()
    }

    fun isPackagePrivate(element: Element): Boolean {
        return !element.modifiers.contains(Modifier.PUBLIC) && !element.modifiers.contains(Modifier.PRIVATE)
                && !element.modifiers.contains(Modifier.STATIC)
    }

    fun isValidAllFields(allFields: Boolean, element: Element): Boolean {
        return allFields && element.kind.isField &&
                !element.modifiers.contains(Modifier.STATIC) &&
                !element.modifiers.contains(Modifier.FINAL) &&
                element.annotation<ColumnIgnore>() == null
    }

    fun getClassName(elementClassname: String, manager: ProcessorManager): ClassName? {
        val typeElement: TypeElement? = manager.elements.getTypeElement(elementClassname)
        return if (typeElement != null) {
            ClassName.get(typeElement)
        } else {
            val names = elementClassname.split(".")
            if (names.isNotEmpty()) {
                // attempt to take last part as class name
                val className = names[names.size - 1]
                ClassName.get(elementClassname.replace("." + className, ""), className)
            } else {
                null
            }
        }
    }
}
