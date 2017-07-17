package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.provider.Notify
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.annotation
import com.squareup.javapoet.ClassName
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Description:
 */
class NotifyDefinition(typeElement: Element, processorManager: ProcessorManager)
    : BaseDefinition(typeElement, processorManager) {

    var paths = arrayOf<String>()
    var method = Notify.Method.DELETE
    val parent = (typeElement.enclosingElement as TypeElement).qualifiedName.toString()
    val methodName = typeElement.simpleName.toString()
    var params: String
    var returnsArray: Boolean = false
    var returnsSingle: Boolean = false

    init {

        typeElement.annotation<Notify>()?.let { notify ->
            paths = notify.paths
            method = notify.method
        }

        val executableElement = typeElement as ExecutableElement

        val parameters = executableElement.parameters
        val paramsBuilder = StringBuilder()
        var first = true
        parameters.forEach { param ->
            if (first) {
                first = false
            } else {
                paramsBuilder.append(", ")
            }
            val paramType = param.asType()
            val typeAsString = paramType.toString()
            paramsBuilder.append(
                    if ("android.content.Context" == typeAsString) {
                        "getContext()"
                    } else if ("android.net.Uri" == typeAsString) {
                        "uri"
                    } else if ("android.content.ContentValues" == typeAsString) {
                        "values"
                    } else if ("long" == typeAsString) {
                        "id"
                    } else if ("java.lang.String" == typeAsString) {
                        "where"
                    } else if ("java.lang.String[]" == typeAsString) {
                        "whereArgs"
                    } else {
                        ""
                    })
        }

        params = paramsBuilder.toString()

        val typeMirror = executableElement.returnType
        if (ClassNames.URI.toString() + "[]" == typeMirror.toString()) {
            returnsArray = true
        } else if (ClassNames.URI.toString() == typeMirror.toString()) {
            returnsSingle = true
        } else {
            processorManager.logError("Notify method returns wrong type. It must return Uri or Uri[]")
        }
    }

    override fun getElementClassName(element: Element?): ClassName? {
        return null
    }
}
