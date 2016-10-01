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
class UpdateMethod(private val contentProviderDefinition: ContentProviderDefinition,
                   private val manager: ProcessorManager) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            val method = MethodSpec.methodBuilder("update")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassNames.URI, PARAM_URI)
                    .addParameter(ClassNames.CONTENT_VALUES, PARAM_CONTENT_VALUES)
                    .addParameter(ClassName.get(String::class.java), "selection")
                    .addParameter(ArrayTypeName.of(String::class.java), "selectionArgs")
                    .returns(TypeName.INT)

            method.beginControlFlow("switch(MATCHER.match(\$L))", PARAM_URI)
            for (tableEndpointDefinition in contentProviderDefinition.endpointDefinitions) {
                for (uriDefinition in tableEndpointDefinition.contentUriDefinitions) {
                    if (uriDefinition.updateEnabled) {


                        method.beginControlFlow("case \$L:", uriDefinition.name)
                        method.addStatement(
                                "\$T adapter = \$T.getModelAdapter(\$T.getTableClassForName(\$S, \$S))",
                                ClassNames.MODEL_ADAPTER,
                                ClassNames.FLOW_MANAGER,
                                ClassNames.FLOW_MANAGER,
                                contentProviderDefinition.databaseNameString,
                                tableEndpointDefinition.tableName)
                        method.addCode(ProviderMethodUtils.getSegmentsPreparation(uriDefinition))
                        method.addCode(
                                "long count = \$T.getDatabase(\$S).getWritableDatabase().updateWithOnConflict(\$S, \$L, ",
                                ClassNames.FLOW_MANAGER,
                                manager.getDatabaseName(contentProviderDefinition.databaseName),
                                tableEndpointDefinition.tableName,
                                PARAM_CONTENT_VALUES)
                        method.addCode(ProviderMethodUtils.getSelectionAndSelectionArgs(uriDefinition))
                        method.addCode(
                                ", \$T.getSQLiteDatabaseAlgorithmInt(adapter.getUpdateOnConflictAction()));\n",
                                ClassNames.CONFLICT_ACTION)

                        val code = CodeBlock.builder()
                        NotifyMethod(tableEndpointDefinition, uriDefinition,
                                Notify.Method.UPDATE).addCode(code)
                        method.addCode(code.build())

                        method.addStatement("return (int) count")
                        method.endControlFlow()
                    }
                }

            }

            method.beginControlFlow("default:").addStatement("throw new \$T(\$S + \$L)", ClassName.get(IllegalStateException::class.java), "Unknown Uri", PARAM_URI).endControlFlow()

            method.endControlFlow()

            return method.build()
        }

    companion object {

        private val PARAM_URI = "uri"
        private val PARAM_CONTENT_VALUES = "values"
    }
}
