package com.raizlabs.android.dbflow.processor.definition.method.provider

import com.raizlabs.android.dbflow.annotation.provider.Notify
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.CodeAdder
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition
import com.raizlabs.android.dbflow.processor.definition.NotifyDefinition
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.squareup.javapoet.CodeBlock

/**
 * Description:
 */
class NotifyMethod(private val tableEndpointDefinition: TableEndpointDefinition,
                   private val uriDefinition: ContentUriDefinition, private val method: Notify.Method) : CodeAdder {

    override fun addCode(code: CodeBlock.Builder) {
        var hasListener = false
        val notifyDefinitionMap = tableEndpointDefinition.notifyDefinitionPathMap[uriDefinition.path]
        if (notifyDefinitionMap != null) {
            val notifyDefinitionList = notifyDefinitionMap[method]
            if (notifyDefinitionList != null) {
                for (i in notifyDefinitionList.indices) {
                    val notifyDefinition = notifyDefinitionList[i]
                    if (notifyDefinition.returnsArray) {
                        code.addStatement("\$T[] notifyUris\$L = \$L.\$L(\$L)", ClassNames.URI,
                                notifyDefinition.methodName, notifyDefinition.parent,
                                notifyDefinition.methodName, notifyDefinition.params)
                        code.beginControlFlow("for (\$T notifyUri: notifyUris\$L)", ClassNames.URI, notifyDefinition.methodName)
                    } else {
                        code.addStatement("\$T notifyUri\$L = \$L.\$L(\$L)", ClassNames.URI,
                                notifyDefinition.methodName, notifyDefinition.parent,
                                notifyDefinition.methodName, notifyDefinition.params)
                    }
                    code.addStatement("getContext().getContentResolver().notifyChange(notifyUri\$L, null)",
                            if (notifyDefinition.returnsArray) "" else notifyDefinition.methodName)
                    if (notifyDefinition.returnsArray) {
                        code.endControlFlow()
                    }

                    hasListener = true
                }
            }
        }

        if (!hasListener) {

            val isUpdateDelete = method == Notify.Method.UPDATE || method == Notify.Method.DELETE
            if (isUpdateDelete) {
                code.beginControlFlow("if (count > 0)")
            }

            code.addStatement("getContext().getContentResolver().notifyChange(uri, null)")

            if (isUpdateDelete) {
                code.endControlFlow()
            }
        }
    }

}
