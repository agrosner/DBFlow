package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.S
import com.grosner.kpoet.`@`
import com.grosner.kpoet.`public final class`
import com.grosner.kpoet.extends
import com.grosner.kpoet.implements
import com.grosner.kpoet.javadoc
import com.grosner.kpoet.typeName
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.DBFlowProcessor
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.ElementUtility
import com.raizlabs.android.dbflow.processor.utils.hasJavaX
import com.raizlabs.android.dbflow.processor.utils.toTypeElement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Description: Holds onto a common-set of fields and provides a common-set of methods to output class files.
 */
abstract class BaseDefinition : TypeDefinition {

    val manager: ProcessorManager

    var elementClassName: ClassName? = null
    var elementTypeName: TypeName? = null
    var outputClassName: ClassName? = null
    var erasedTypeName: TypeName? = null

    var element: Element
    var typeElement: TypeElement? = null
    var elementName: String

    var packageName: String

    constructor(element: ExecutableElement, processorManager: ProcessorManager) {
        this.manager = processorManager
        this.element = element
        packageName = manager.elements.getPackageOf(element)?.qualifiedName?.toString() ?: ""
        elementName = element.simpleName.toString()

        try {
            val typeMirror = element.asType()
            elementTypeName = typeMirror.typeName
            elementTypeName?.let {
                if (!it.isPrimitive) {
                    elementClassName = getElementClassName(element)
                }
            }
            val erasedType = processorManager.typeUtils.erasure(typeMirror)
            erasedTypeName = erasedType.typeName
        } catch (e: Exception) {

        }
    }

    constructor(element: Element, processorManager: ProcessorManager) {
        this.manager = processorManager
        this.element = element
        packageName = manager.elements.getPackageOf(element)?.qualifiedName?.toString() ?: ""
        try {
            val typeMirror: TypeMirror
            if (element is ExecutableElement) {
                typeMirror = element.returnType
                elementTypeName = typeMirror.typeName
            } else {
                typeMirror = element.asType()
                elementTypeName = typeMirror.typeName
            }
            val erasedType = processorManager.typeUtils.erasure(typeMirror)
            erasedTypeName = TypeName.get(erasedType)
        } catch (i: IllegalArgumentException) {
            manager.logError("Found illegal type: ${element.asType()} for ${element.simpleName}")
            manager.logError("Exception here: $i")
        }

        elementName = element.simpleName.toString()
        elementTypeName?.let {
            if (!it.isPrimitive) elementClassName = getElementClassName(element)
        }

        if (element is TypeElement) {
            typeElement = element
        } else {
            typeElement = element.toTypeElement()
        }
    }

    constructor(element: TypeElement, processorManager: ProcessorManager) {
        this.manager = processorManager
        this.typeElement = element
        this.element = element
        elementClassName = ClassName.get(typeElement)
        elementTypeName = element.asType().typeName
        elementName = element.simpleName.toString()
        packageName = manager.elements.getPackageOf(element)?.qualifiedName?.toString() ?: ""
    }

    protected open fun getElementClassName(element: Element?): ClassName? {
        try {
            return ElementUtility.getClassName(element?.asType().toString(), manager)
        } catch (e: Exception) {
            return null
        }

    }

    protected fun setOutputClassName(postfix: String) {
        val outputName: String
        if (elementClassName == null) {
            if (elementTypeName is ClassName) {
                outputName = (elementTypeName as ClassName).simpleName()
            } else if (elementTypeName is ParameterizedTypeName) {
                outputName = (elementTypeName as ParameterizedTypeName).rawType.simpleName()
                elementClassName = (elementTypeName as ParameterizedTypeName).rawType
            } else {
                outputName = elementTypeName.toString()
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
                manager.logError("Found error for ${elementTypeName} ${outputClassName} ${(this as QueryModelDefinition).databaseTypeName}")
            }
            return `public final class`(outputClassName?.simpleName() ?: "") {
                if (hasJavaX()) {
                    addAnnotation(`@`(ClassNames.GENERATED, {
                        this["value"] = DBFlowProcessor::class.java.canonicalName.toString().S
                    }).build())
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
