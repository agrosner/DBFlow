package com.dbflow5.processor.interop

import com.dbflow5.annotation.ModelViewQuery
import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.companion
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.ClassNameResolver
import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.dbflow5.codegen.shared.interop.PropertyDeclaration
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.getPackage
import com.dbflow5.processor.utils.toKTypeName
import com.dbflow5.processor.utils.toTypeErasedElement
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

@Suppress("FunctionName")
fun KaptClassDeclaration(typeElement: TypeElement): KaptClassDeclaration {
    return safeResolveMetaData(
        typeElement = typeElement,
        fallback = { KaptJavaClassDeclaration(typeElement) },
    ) { typeSpec, _ ->
        KaptKotlinClassDeclaration(
            typeElement, typeSpec
        )
    }
}

abstract class KaptClassDeclaration : ClassDeclaration {
    abstract val typeElement: TypeElement

    override val superTypes: Sequence<TypeName>
        get() = typeElement.asType()?.let { mirror ->
            ProcessorManager.manager.typeUtils.directSupertypes(mirror)
                .mapNotNull { it.toKTypeName() }
        }?.asSequence() ?: emptySequence()

    val allMembers by lazy { ElementUtility.getAllElements(typeElement, ProcessorManager.manager) }
    val getters by lazy {
        allMembers.filterIsInstance<ExecutableElement>()
            .filter { it.simpleName.startsWith("get") }
            .map { JavaGetterDeclaration(it) }
    }
    val setters by lazy {
        allMembers.filterIsInstance<ExecutableElement>()
            .filter { it.simpleName.startsWith("set") }
    }

    val propertyElements: List<JavaPropertyDeclaration> by lazy {
        allMembers
            .filterIsInstance<VariableElement>()
            .map { elm ->
                val propertyDeclaration = JavaPropertyDeclaration(
                    element = elm,
                    getter = getters.firstOrNull {
                        it.propertyNameString == elm.simpleName.toString()
                    }?.element,
                    setter = setters.firstOrNull {
                        val propName = it.simpleName.toString()
                            .lowercase()
                            .replaceFirst("set", "")
                        propName == elm.simpleName.toString()
                    },
                )
                propertyDeclaration
            }
    }
}

internal data class KaptJavaClassDeclaration(
    override val typeElement: TypeElement,
) : KaptClassDeclaration() {
    override val containingFile: OriginatingSource = KaptOriginatingSource(typeElement)
    override val isInternal: Boolean = false
    override val isEnum: Boolean = typeElement.kind == ElementKind.ENUM
    override val isObject: Boolean = false
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

    override val hasDefaultConstructor: Boolean = true // we don't figure out Java models.
    override val isData: Boolean = false
}

internal data class KaptKotlinClassDeclaration(
    override val typeElement: TypeElement,
    private val typeSpec: TypeSpec,
) : KaptClassDeclaration() {
    override val containingFile: OriginatingSource = KaptOriginatingSource(typeElement)
    override val isInternal: Boolean = typeSpec.modifiers.contains(KModifier.INTERNAL)
    override val isEnum: Boolean = typeSpec.isEnum
    override val isObject: Boolean = typeSpec.kind == TypeSpec.Kind.OBJECT
    override val isData: Boolean = typeSpec.modifiers.contains(KModifier.DATA)

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
            return propertyElements
                .asSequence()
                .map { it.simpleName.shortName }
                .run {
                    toMutableList().apply {
                        addAll(getters.map { it.propertyNameString })
                    }.asSequence()
                }
                // preserve same order as how they are declared
                // kotlin metadata returns types in alphabetical vs declared order.
                .mapNotNull { typeSpec.propertySpecs.firstOrNull { prop -> prop.name == it } }
                .map { spec ->
                    KaptKotlinPropertyDeclaration(
                        packageName,
                        spec,
                        propertyElements
                            .firstOrNull { prop -> prop.simpleName.shortName == spec.name }
                            ?.element
                            ?: getters
                                .firstOrNull { prop -> prop.propertyNameString == spec.name }
                                ?.element
                            ?: throw IllegalStateException(
                                "Cant find property for ${spec.name}: " +
                                    "from ${this.typeElement} -> ${propertyElements.size}"
                            )
                    )
                }
        }

    override val hasDefaultConstructor: Boolean = (typeSpec.primaryConstructor?.parameters?.all {
        it.defaultValue != null
    }) ?: (typeSpec.funSpecs.filter { it.isConstructor }
        .firstNotNullOfOrNull { constructor ->
            constructor.takeIf { declaration ->
                declaration.parameters.isEmpty() || declaration.parameters.all { it.defaultValue != null }
            }
        } != null)
}

fun KaptClassDeclaration.modelViewQueryNameOrThrow(
    name: NameModel,
    classDeclaration: KaptClassDeclaration,
    resolver: ClassNameResolver,
): Pair<NameModel, Boolean> {
    val companion = resolver.classDeclarationByClassName(
        name.companion().className,
    )

    // TODO: check methods
    val modelViewQuery = (companion?.properties
        ?.firstOrNull { (it as KaptPropertyDeclaration).annotation<ModelViewQuery>() != null }
        ?: classDeclaration.properties.firstOrNull {
            (it as KaptPropertyDeclaration).annotation<ModelViewQuery>() != null
        }) as? KaptPropertyDeclaration
    val gettersWithModelViewQuery =
        classDeclaration.getters.firstOrNull { it.element.annotation<ModelViewQuery>() != null }
    val name = modelViewQuery?.simpleName
        ?: gettersWithModelViewQuery?.simpleName
    val element = modelViewQuery?.element
        ?: gettersWithModelViewQuery?.element
    return if (name != null) {
        // TODO: this property check is pretty rough. will need a real way.
        name to (modelViewQuery != null || gettersWithModelViewQuery == null)
    } else throw IllegalStateException(
        "Missing modelview query ${name} ${
            companion ?: (classDeclaration
                as KaptJavaClassDeclaration).allMembers
        }"
    )
}