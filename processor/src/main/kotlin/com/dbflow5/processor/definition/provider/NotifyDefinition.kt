package com.dbflow5.processor.definition.provider

import com.dbflow5.annotation.provider.Notify
import com.dbflow5.annotation.provider.NotifyMethod
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.BaseDefinition
import com.dbflow5.processor.utils.annotation
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
    var method = NotifyMethod.DELETE
    val parent = (typeElement.enclosingElement as TypeElement).qualifiedName.toString()
    val methodName = typeElement.simpleName.toString()
    var params: String
    var returnsArray: Boolean = false
    var returnsSingle: Boolean = false

    init {

        typeElement.annotation<Notify>()?.let { notify ->
            paths = notify.paths
            method = notify.notifyMethod
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
                    when (typeAsString) {
                        "android.content.Context" -> "getContext()"
                        "android.net.Uri" -> "uri"
                        "android.content.ContentValues" -> "values"
                        "long" -> "id"
                        "java.lang.String" -> "where"
                        "java.lang.String[]" -> "whereArgs"
                        else -> ""
                    })
        }

        params = paramsBuilder.toString()

        val typeMirror = executableElement.returnType
        when {
            "${com.dbflow5.processor.ClassNames.URI}[]" == typeMirror.toString() -> returnsArray = true
            com.dbflow5.processor.ClassNames.URI.toString() == typeMirror.toString() -> returnsSingle = true
            else -> processorManager.logError("Notify method returns wrong type. It must return Uri or Uri[]")
        }
    }

    override fun getElementClassName(element: Element?): ClassName? = null
}
