package com.raizlabs.android.dbflow.processor.definition.method.provider

import com.raizlabs.android.dbflow.annotation.provider.Notify
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Modifier

/**
 * Description:
 */
class InsertMethod(private val contentProviderDefinition: ContentProviderDefinition,
                   private val isBulk: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            val code = CodeBlock.builder()
            code.beginControlFlow("switch(MATCHER.match(\$L))", PARAM_URI)

            contentProviderDefinition.endpointDefinitions.forEach {
                it.contentUriDefinitions.forEach { uriDefinition ->
                    if (uriDefinition.insertEnabled) {
                        code.beginControlFlow("case \$L:", uriDefinition.name)
                        code.addStatement("\$T adapter = \$T.getModelAdapter(\$T.getTableClassForName(\$S, \$S))",
                                ClassNames.MODEL_ADAPTER, ClassNames.FLOW_MANAGER, ClassNames.FLOW_MANAGER,
                                contentProviderDefinition.databaseNameString, it.tableName)

                        code.add("final long id = FlowManager.getDatabase(\$S).getWritableDatabase()",
                                contentProviderDefinition.databaseNameString).add(".insertWithOnConflict(\$S, null, values, " + "\$T.getSQLiteDatabaseAlgorithmInt(adapter.getInsertOnConflictAction()));\n", it.tableName,
                                ClassNames.CONFLICT_ACTION)

                        NotifyMethod(it, uriDefinition,
                                Notify.Method.INSERT).addCode(code)

                        if (!isBulk) {
                            code.addStatement("return \$T.withAppendedId(\$L, id)", ClassNames.CONTENT_URIS, PARAM_URI)
                        } else {
                            code.addStatement("return id > 0 ? 1 : 0")
                        }
                        code.endControlFlow()
                    }
                }
            }

            code.beginControlFlow("default:").addStatement("throw new \$T(\$S + \$L)", ClassName.get(IllegalStateException::class.java), "Unknown Uri", PARAM_URI).endControlFlow()

            code.endControlFlow()
            return MethodSpec.methodBuilder(if (isBulk) "bulkInsert" else "insert")
                    .addAnnotation(Override::class.java).addParameter(ClassNames.URI, PARAM_URI)
                    .addParameter(ClassNames.CONTENT_VALUES, PARAM_CONTENT_VALUES)
                    .addModifiers(if (isBulk) Modifier.PROTECTED else Modifier.PUBLIC, Modifier.FINAL)
                    .addCode(code.build()).returns(if (isBulk) TypeName.INT else ClassNames.URI).build()
        }

    companion object {

        private val PARAM_URI = "uri"
        private val PARAM_CONTENT_VALUES = "values"
    }

}
