package com.dbflow5.processor.definition.provider

import com.dbflow5.contentprovider.annotation.Notify
import com.dbflow5.contentprovider.annotation.NotifyMethod
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.BaseDefinition
import com.dbflow5.processor.definition.CodeAdder
import com.squareup.javapoet.CodeBlock
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Description: Writes code for [Notify] annotation.
 */
class NotifyDefinition(notify: Notify,
                       typeElement: ExecutableElement,
                       processorManager: ProcessorManager)
    : BaseDefinition(typeElement, processorManager), CodeAdder {

    val paths: Array<String> = notify.paths
    val method: NotifyMethod = notify.notifyMethod
    private val parent = (typeElement.enclosingElement as TypeElement).qualifiedName.toString()
    private val methodName = typeElement.simpleName.toString()
    private var params: String = typeElement.parameters.joinToString {
        when (it.asType().toString()) {
            "android.content.Context" -> "getContext()"
            "android.net.Uri" -> "uri"
            "android.content.ContentValues" -> "values"
            "long" -> "id"
            "java.lang.String" -> "where"
            "java.lang.String[]" -> "whereArgs"
            else -> ""
        }
    }

    var returnsArray: Boolean = false
    var returnsSingle: Boolean = false

    init {
        val typeMirror = typeElement.returnType
        when {
            "${ClassNames.URI}[]" == typeMirror.toString() -> returnsArray = true
            ClassNames.URI.toString() == typeMirror.toString() -> returnsSingle = true
            else -> processorManager.logError("Notify method returns wrong type. It must return Uri or Uri[]")
        }
    }

    override fun addCode(code: CodeBlock.Builder): CodeBlock.Builder {
        if (returnsArray) {
            code.addStatement("\$T[] notifyUris\$L = \$L.\$L(\$L)", ClassNames.URI,
                methodName, parent,
                methodName, params)
            code.beginControlFlow("for (\$T notifyUri: notifyUris\$L)", ClassNames.URI, methodName)
        } else {
            code.addStatement("\$T notifyUri\$L = \$L.\$L(\$L)", ClassNames.URI,
                methodName, parent,
                methodName, params)
        }
        code.addStatement("getContext().getContentResolver().notifyChange(notifyUri\$L, null)",
            if (returnsArray) "" else methodName)
        if (returnsArray) {
            code.endControlFlow()
        }
        return code
    }
}
