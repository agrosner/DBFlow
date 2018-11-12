package com.dbflow5.processor.definition

import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.hasJavaX
import com.dbflow5.processor.utils.toClassName
import com.dbflow5.processor.utils.toTypeElement
import com.grosner.kpoet.S
import com.grosner.kpoet.`@`
import com.grosner.kpoet.`public final class`
import com.grosner.kpoet.extends
import com.grosner.kpoet.implements
import com.grosner.kpoet.javadoc
import com.grosner.kpoet.typeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Description: Holds onto a common-set of fields and provides a common-set of methods to output class files.
 */
abstract class BaseDefinition(
    /**
     * Original definition element.
     */
    val element: Element,

    /**
     * Optional [TypeElement]. if the [element] passed in is a type.
     */
    val typeElement: TypeElement?,
    /**
     * The resolved [TypeName] for this definition. It may be a return type if [ExecutableElement],
     *
     */
    val elementTypeName: TypeName?,
    val manager: ProcessorManager,
    val packageName: String) : TypeDefinition {


    /**
     * The [ClassName] referring to the type of the definition. This excludes primitives.
     */
    var elementClassName: ClassName? = null


    var outputClassName: ClassName? = null

    /**
     * Unqualified name of the [element]. Useful for names of methods, fields, or short type names.
     */
    val elementName: String = element.simpleName.toString()

    constructor(element: ExecutableElement, processorManager: ProcessorManager) : this(
        manager = processorManager,
        packageName = processorManager.elements.getPackageOf(element)?.qualifiedName?.toString()
            ?: "",
        element = element,
        typeElement = null,
        elementTypeName = try {
            element.asType().typeName
        } catch (i: IllegalArgumentException) {
            // unexpected TypeMirror (usually a List). Cannot use for TypeName.
            null
        }
    ) {
        elementClassName = if (elementTypeName != null && !elementTypeName.isPrimitive) getElementClassName(element) else null
    }

    constructor(element: Element, processorManager: ProcessorManager,
                packageName: String = processorManager.elements.getPackageOf(element)?.qualifiedName?.toString()
                    ?: "") : this(
        manager = processorManager,
        element = element,
        packageName = packageName,
        typeElement = element as? TypeElement ?: element.toTypeElement(),
        elementTypeName = try {
            when (element) {
                is ExecutableElement -> element.returnType
                else -> element.asType()
            }.typeName
        } catch (i: IllegalArgumentException) {
            processorManager.logError("Found illegal type: ${element.asType()} for ${element.simpleName}")
            processorManager.logError("Exception here: $i")
            null
        }
    ) {
        elementTypeName?.let {
            if (!it.isPrimitive) elementClassName = getElementClassName(element)
        }

    }

    constructor(element: TypeElement, processorManager: ProcessorManager) : this(
        manager = processorManager,
        element = element,
        typeElement = element,
        packageName = processorManager.elements.getPackageOf(element)?.qualifiedName?.toString()
            ?: "",
        elementTypeName = element.asType().typeName
    ) {
        elementClassName = element.toClassName()
    }

    protected open fun getElementClassName(element: Element?): ClassName? {
        return try {
            ElementUtility.getClassName(element?.asType().toString(), manager)
        } catch (e: Exception) {
            null
        }
    }

    protected fun setOutputClassName(postfix: String) {
        val outputName: String
        if (elementClassName == null) {
            when (elementTypeName) {
                is ClassName -> outputName = elementTypeName.simpleName()
                is ParameterizedTypeName -> {
                    outputName = elementTypeName.rawType.simpleName()
                    elementClassName = elementTypeName.rawType
                }
                else -> outputName = elementTypeName.toString()
            }
        } else {
            outputName = elementClassName!!.simpleName()
        }
        outputClassName = ClassName.get(packageName, outputName + postfix)
    }

    protected fun setOutputClassNameFull(fullName: String) {
        outputClassName = ClassName.get(packageName, fullName)
    }

    override val typeSpec: TypeSpec
        get() {
            if (outputClassName == null) {
                manager.logError("$elementTypeName's outputClass name was null. Database was " +
                    "${(this as? BaseTableDefinition)?.associationalBehavior?.databaseTypeName}")
            }
            return `public final class`(outputClassName?.simpleName() ?: "") {
                if (hasJavaX()) {
                    addAnnotation(`@`(com.dbflow5.processor.ClassNames.GENERATED) {
                        this["value"] = com.dbflow5.processor.DBFlowProcessor::class.java.canonicalName.toString().S
                    }.build())
                }
                extendsClass?.let { extends(it) }
                implementsClasses.forEach { implements(it) }
                javadoc("This is generated code. Please do not modify")
                onWriteDefinition(this)
                this
            }
        }

    protected open val extendsClass: TypeName?
        get() = null

    protected val implementsClasses: Array<TypeName>
        get() = arrayOf()

    open fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {

    }
}
