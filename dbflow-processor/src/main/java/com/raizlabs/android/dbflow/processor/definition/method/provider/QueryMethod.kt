package com.raizlabs.android.dbflow.processor.definition.method.provider

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import javax.lang.model.element.Modifier

/**
 * Description:
 */
class QueryMethod(private val contentProviderDefinition: ContentProviderDefinition, private val manager: ProcessorManager) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            val method = MethodSpec.methodBuilder("query")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ClassNames.URI, "uri")
                    .addParameter(ArrayTypeName.of(String::class.java), "projection")
                    .addParameter(ClassName.get(String::class.java), "selection")
                    .addParameter(ArrayTypeName.of(String::class.java), "selectionArgs")
                    .addParameter(ClassName.get(String::class.java), "sortOrder")
                    .returns(ClassNames.CURSOR)

            method.addStatement("\$L cursor = null", ClassNames.CURSOR)
            method.beginControlFlow("switch(\$L.match(uri))", ContentProviderDefinition.URI_MATCHER)
            for (tableEndpointDefinition in contentProviderDefinition.endpointDefinitions) {
                for (uriDefinition in tableEndpointDefinition.contentUriDefinitions) {
                    if (uriDefinition.queryEnabled) {
                        method.beginControlFlow("case \$L:", uriDefinition.name)
                        method.addCode(ProviderMethodUtils.getSegmentsPreparation(uriDefinition))
                        method.addCode("cursor = \$T.getDatabase(\$S).getWritableDatabase().query(\$S, projection, ",
                                ClassNames.FLOW_MANAGER,
                                manager.getDatabaseName(contentProviderDefinition.databaseName),
                                tableEndpointDefinition.tableName)
                        method.addCode(ProviderMethodUtils.getSelectionAndSelectionArgs(uriDefinition))
                        method.addCode(", null, null, sortOrder);\n")
                        method.addStatement("break")
                        method.endControlFlow()
                    }
                }
            }
            method.endControlFlow()

            method.beginControlFlow("if (cursor != null)")
            method.addStatement("cursor.setNotificationUri(getContext().getContentResolver(), uri)")
            method.endControlFlow()
            method.addStatement("return cursor")

            return method.build()
        }
}
