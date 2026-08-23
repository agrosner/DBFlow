package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.interop.PropertyDeclaration
import com.dbflow5.processor.utils.kTypeName
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import kotlin.reflect.KClass

/**
 * Description: This is a class holder to determine the
 * field name, and whether it has getter / setter.
 */
data class JavaPropertyDeclaration(
    val element: VariableElement,
    private val getter: ExecutableElement?,
    private val setter: ExecutableElement?,
    override val typeName: TypeName = element.asType().kTypeName,
    override val isAbstract: Boolean = element.modifiers.contains(Modifier.ABSTRACT),
    override val simpleName: NameModel = element.name(preserveNull = true),
    val isVal: Boolean = (setter == null ||
        element.modifiers.contains(Modifier.FINAL)),
) : PropertyDeclaration {
    fun <A : Annotation> getAnnotation(clazz: KClass<A>): A? {
        return element.getAnnotation(clazz.java)
            ?: getter?.getAnnotation(clazz.java)
            ?: setter?.getAnnotation(clazz.java)
    }
}