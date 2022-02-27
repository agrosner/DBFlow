package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.interop.PropertyDeclaration
import com.dbflow5.processor.utils.kTypeName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.javapoet.toKTypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
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
    override fun <A : Annotation> annotation(aClass: KClass<A>): A? =
        element.getAnnotation(aClass.java)

    override val simpleName: NameModel = NameModel(
        packageName = packageName,
        shortName = propertySpec.name,
        nullable = propertySpec.type.isNullable,
    )
    override val typeName: TypeName = propertySpec.type
}

data class KaptKotlinMethodDeclaration(
    val packageName: String,
    val funSpec: FunSpec,
    override val element: Element,
) : KaptPropertyDeclaration {
    override fun <A : Annotation> annotation(aClass: KClass<A>): A? =
        element.getAnnotation(aClass.java)

    override val simpleName: NameModel = NameModel(
        packageName = packageName,
        shortName = funSpec.name,
        nullable = funSpec.returnType?.isNullable ?: false,
    )
    override val typeName: TypeName = funSpec.returnType ?: UNIT
    override val isAbstract: Boolean = funSpec.modifiers.contains(KModifier.ABSTRACT)
}

data class KaptJavaMethodDeclaration(
    override val element: ExecutableElement,
) : KaptPropertyDeclaration {
    override fun <A : Annotation> annotation(aClass: KClass<A>): A? =
        element.getAnnotation(aClass.java)

    override val simpleName: NameModel = element.name()
    override val typeName: TypeName = element.returnType.kTypeName
    override val isAbstract: Boolean = element.modifiers.contains(Modifier.ABSTRACT)
}
