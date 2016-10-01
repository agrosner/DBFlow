package com.raizlabs.android.dbflow.processor.definition.method.provider

import com.raizlabs.android.dbflow.annotation.provider.Notify
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier

/**
 * Description:
 */
class DeleteMethod(private val contentProviderDefinition: ContentProviderDefinition,
                   private val manager: ProcessorManager) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            val code = CodeBlock.builder()

            code.beginControlFlow("switch(MATCHER.match(\$L))", PARAM_URI)
            for (tableEndpointDefinition in contentProviderDefinition.endpointDefinitions) {
                tableEndpointDefinition.contentUriDefinitions.forEach {
                    if (it.deleteEnabled) {

                        code.beginControlFlow("case \$L:", it.name)

                        code.add(ProviderMethodUtils.getSegmentsPreparation(it))
                        code.add("long count = \$T.getDatabase(\$S).getWritableDatabase().delete(\$S, ",
                                ClassNames.FLOW_MANAGER,
                                manager.getDatabaseName(contentProviderDefinition.databaseName),
                                tableEndpointDefinition.tableName)
                        code.add(ProviderMethodUtils.getSelectionAndSelectionArgs(it))
                        code.add(");\n")

                        NotifyMethod(tableEndpointDefinition, it, Notify.Method.DELETE).addCode(code)

                        code.addStatement("return (int) count")
                        code.endControlFlow()
                    }
                }
            }

            code.beginControlFlow("default:").addStatement("throw new \$T(\$S + \$L)", ClassName.get(IllegalArgumentException::class.java), "Unknown URI", PARAM_URI).endControlFlow()
            code.endControlFlow()

            return MethodSpec.methodBuilder("delete").addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL).addParameter(ClassNames.URI, PARAM_URI).addParameter(ClassName.get(String::class.java), PARAM_SELECTION).addParameter(ArrayTypeName.of(String::class.java), PARAM_SELECTION_ARGS).addCode(code.build()).returns(TypeName.INT).build()
        }

    companion object {

        private val PARAM_URI = "uri"
        private val PARAM_SELECTION = "selection"
        private val PARAM_SELECTION_ARGS = "selectionArgs"
    }

}
