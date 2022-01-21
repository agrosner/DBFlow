package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.interop.PropertyDeclaration
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.getPackage
import com.grosner.kpoet.typeName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.javapoet.toKTypeName
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import kotlin.reflect.KClass

sealed interface KaptPropertyDeclaration : PropertyDeclaration {
    val element: VariableElement
    fun <A : Annotation> annotation(aClass: KClass<A>): A?
}

inline fun <reified A : Annotation> KaptPropertyDeclaration.annotation() = annotation(A::class)

/**
 * Description:
 */
data class KaptJavaPropertyDeclaration(
    override val element: VariableElement,
) : KaptPropertyDeclaration {
    override val typeName: TypeName
        get() = element.asType().typeName.toKTypeName()
    override val isAbstract: Boolean = element.modifiers.contains(Modifier.ABSTRACT)
    override fun <A : Annotation> annotation(aClass: KClass<A>): A? {
        return element.getAnnotation(aClass.java)
    }

    override val simpleName: NameModel =
        NameModel(
            packageName = element.getPackage(ProcessorManager.manager).simpleName.toString(),
            shortName = element.simpleName.toString(),
        )
}

data class KaptKotlinPropertyDeclaration(
    val packageName: String,
    val propertySpec: PropertySpec,
    override val element: VariableElement,
) : KaptPropertyDeclaration {
    override val isAbstract: Boolean = propertySpec.modifiers.contains(KModifier.ABSTRACT)
    override fun <A : Annotation> annotation(aClass: KClass<A>): A? {
        return element.getAnnotation(aClass.java)
    }

    override val simpleName: NameModel = NameModel(
        packageName = packageName,
        shortName = propertySpec.name,
    )
    override val typeName: TypeName = propertySpec.type
}
