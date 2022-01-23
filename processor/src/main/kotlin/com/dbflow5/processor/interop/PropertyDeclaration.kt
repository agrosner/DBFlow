package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.interop.PropertyDeclaration
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.Element
import kotlin.reflect.KClass

sealed interface KaptPropertyDeclaration : PropertyDeclaration {
    val element: Element
    fun <A : Annotation> annotation(aClass: KClass<A>): A?
}

inline fun <reified A : Annotation> KaptPropertyDeclaration.annotation() = annotation(A::class)

/**
 * Description:
 */
data class KaptJavaPropertyDeclaration(
    override val element: Element,
    private val property: JavaPropertyDeclaration,
) : KaptPropertyDeclaration {
    override val typeName: TypeName = property.typeName
    override val isAbstract: Boolean = property.isAbstract
    override fun <A : Annotation> annotation(aClass: KClass<A>): A? {
        return property.getAnnotation(aClass)
    }

    override val simpleName: NameModel = property.simpleName
}

data class KaptKotlinPropertyDeclaration(
    val packageName: String,
    val propertySpec: PropertySpec,
    override val element: Element,
) : KaptPropertyDeclaration {
    override val isAbstract: Boolean = propertySpec.modifiers.contains(KModifier.ABSTRACT)
    override fun <A : Annotation> annotation(aClass: KClass<A>): A? {
        return element.getAnnotation(aClass.java)
    }

    override val simpleName: NameModel = NameModel(
        packageName = packageName,
        shortName = propertySpec.name,
        nullable = propertySpec.type.isNullable,
    )
    override val typeName: TypeName = propertySpec.type
}
