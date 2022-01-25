package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.interop.PropertyDeclaration
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.getPackage
import com.dbflow5.processor.utils.isNullable
import com.dbflow5.processor.utils.simpleString
import com.grosner.kpoet.typeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.javapoet.toKTypeName
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
    override val typeName: TypeName = element.asType().typeName.toKTypeName(),
    override val isAbstract: Boolean = element.modifiers.contains(Modifier.ABSTRACT),
    override val simpleName: NameModel = NameModel(
        packageName = element.getPackage(ProcessorManager.manager).qualifiedName.toString(),
        shortName = element.simpleString,
        nullable = element.isNullable(),
    ),
    val isVal: Boolean = (setter == null ||
        element.modifiers.contains(Modifier.FINAL)),
) : PropertyDeclaration {
    fun <A : Annotation> getAnnotation(clazz: KClass<A>): A? {
        return element.getAnnotation(clazz.java)
            ?: getter?.getAnnotation(clazz.java)
            ?: setter?.getAnnotation(clazz.java)
    }
}