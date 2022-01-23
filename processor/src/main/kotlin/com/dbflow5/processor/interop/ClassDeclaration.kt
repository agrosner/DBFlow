package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.dbflow5.codegen.shared.interop.PropertyDeclaration
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.getPackage
import com.dbflow5.processor.utils.simpleString
import com.dbflow5.processor.utils.toKTypeName
import com.dbflow5.processor.utils.toTypeErasedElement
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

@Suppress("FunctionName")
fun KaptClassDeclaration(typeElement: TypeElement): ClassDeclaration {
    return safeResolveMetaData(
        typeElement = typeElement,
        fallback = { KaptJavaClassDeclaration(typeElement) },
    ) { typeSpec, _ ->
        KaptKotlinClassDeclaration(
            typeElement, typeSpec
        )
    }
}

interface KaptClassDeclaration : ClassDeclaration {
    val typeElement: TypeElement

    override val superTypes: Sequence<TypeName>
        get() = typeElement.asType()?.let { mirror ->
            ProcessorManager.manager.typeUtils.directSupertypes(mirror)
                .mapNotNull { it.toKTypeName() }
        }?.asSequence() ?: emptySequence()

    private val allMembers
        get() = ElementUtility.getAllElements(typeElement, ProcessorManager.manager)
    val getters
        get() = allMembers.filterIsInstance<ExecutableElement>()
            .filter { it.simpleName.startsWith("get") }
    val setters
        get() = allMembers.filterIsInstance<ExecutableElement>()
            .filter { it.simpleName.startsWith("set") }

    /**
     * Collects
     */
    val propertyElements: List<JavaPropertyDeclaration>
        get() {
            return allMembers
                .filterIsInstance<VariableElement>()
                .map { elm ->
                    val propertyDeclaration = JavaPropertyDeclaration(
                        element = elm,
                        getter = getters.firstOrNull {
                            val propName = it.simpleName.toString().replaceFirst("get", "")
                            propName == it.simpleName.toString()
                        },
                        setter = setters.firstOrNull {
                            val propName = it.simpleName.toString().replaceFirst("set", "")
                            propName == it.simpleName.toString()
                        },
                    )
                    propertyDeclaration
                }
        }
}

internal data class KaptJavaClassDeclaration(
    override val typeElement: TypeElement,
) : KaptClassDeclaration {
    override val containingFile: OriginatingSource = KaptOriginatingSource(typeElement)
    override val isInternal: Boolean = false
    override val isEnum: Boolean = typeElement.kind == ElementKind.ENUM

    override val properties: Sequence<PropertyDeclaration> =
        propertyElements
            .asSequence()
            .map { prop ->
                KaptJavaPropertyDeclaration(
                    prop.element,
                    prop
                )
            }

    override fun asStarProjectedType(): ClassDeclaration {
        return KaptJavaClassDeclaration(typeElement.toTypeErasedElement())
    }
}

internal data class KaptKotlinClassDeclaration(
    override val typeElement: TypeElement,
    private val typeSpec: TypeSpec,
) : KaptClassDeclaration {
    override val containingFile: OriginatingSource = KaptOriginatingSource(typeElement)
    override val isInternal: Boolean = typeSpec.modifiers.contains(KModifier.INTERNAL)
    override val isEnum: Boolean = typeSpec.isEnum

    override fun asStarProjectedType(): ClassDeclaration {
        return KaptKotlinClassDeclaration(
            typeElement.toTypeErasedElement(),
            typeSpec = typeSpec.toBuilder()
                .build(),
        )
    }

    override val properties: Sequence<PropertyDeclaration>
        get() {
            val packageName = typeElement.getPackage().qualifiedName.toString()
            return typeSpec.propertySpecs
                .asSequence()
                .map { spec ->
                    KaptKotlinPropertyDeclaration(
                        packageName,
                        spec,
                        propertyElements
                            .firstOrNull { prop -> prop.simpleName.shortName == spec.name }
                            ?.element
                            ?: getters
                                .firstOrNull { prop ->
                                    prop.simpleString
                                        .lowercase()
                                        .replaceFirst("get", "") == spec.name
                                }
                            ?: throw IllegalStateException(
                                "Cant find property for ${spec.name}: " +
                                    "from ${this.typeElement} -> ${propertyElements.size}"
                            )
                    )
                }
        }
}
