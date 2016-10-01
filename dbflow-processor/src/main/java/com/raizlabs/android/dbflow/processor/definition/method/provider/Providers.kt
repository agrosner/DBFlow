package com.raizlabs.android.dbflow.processor.definition.method.provider

import com.raizlabs.android.dbflow.annotation.provider.Notify
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.CodeAdder
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier

internal fun appendDefault(code: CodeBlock.Builder) {
    code.beginControlFlow("default:")
            .addStatement("throw new \$T(\$S + \$L)",
                    ClassName.get(IllegalArgumentException::class.java), "Unknown URI", Constants.PARAM_URI)
            .endControlFlow()
}

object Constants {

    internal val PARAM_CONTENT_VALUES = "values"
    internal val PARAM_URI = "uri"
}

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */
class DeleteMethod(private val contentProviderDefinition: ContentProviderDefinition,
                   private val manager: ProcessorManager) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            val code = CodeBlock.builder()

            code.beginControlFlow("switch(MATCHER.match(\$L))", PARAM_URI)
            contentProviderDefinition.endpointDefinitions.forEach {
                it.contentUriDefinitions.forEach { uriDefinition ->
                    if (uriDefinition.deleteEnabled) {

                        code.beginControlFlow("case \$L:", uriDefinition.name)

                        code.add(uriDefinition.getSegmentsPreparation())
                        code.add("long count = \$T.getDatabase(\$S).getWritableDatabase().delete(\$S, ",
                                ClassNames.FLOW_MANAGER,
                                manager.getDatabaseName(contentProviderDefinition.databaseName),
                                it.tableName)
                        code.add(uriDefinition.getSelectionAndSelectionArgs())
                        code.add(");\n")

                        NotifyMethod(it, uriDefinition, Notify.Method.DELETE).addCode(code)

                        code.addStatement("return (int) count")
                        code.endControlFlow()
                    }
                }
            }

            appendDefault(code)
            code.endControlFlow()

            return MethodSpec.methodBuilder("delete")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ClassNames.URI, PARAM_URI)
                    .addParameter(ClassName.get(String::class.java), PARAM_SELECTION)
                    .addParameter(ArrayTypeName.of(String::class.java), PARAM_SELECTION_ARGS)
                    .addCode(code.build()).returns(TypeName.INT).build()
        }

    companion object {

        private val PARAM_URI = "uri"
        private val PARAM_SELECTION = "selection"
        private val PARAM_SELECTION_ARGS = "selectionArgs"
    }

}

/**
 * Description:
 */
class InsertMethod(private val contentProviderDefinition: ContentProviderDefinition,
                   private val isBulk: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            val code = CodeBlock.builder()
            code.beginControlFlow("switch(MATCHER.match(\$L))", Constants.PARAM_URI)

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
                            code.addStatement("return \$T.withAppendedId(\$L, id)", ClassNames.CONTENT_URIS, Constants.PARAM_URI)
                        } else {
                            code.addStatement("return id > 0 ? 1 : 0")
                        }
                        code.endControlFlow()
                    }
                }
            }

            appendDefault(code)
            code.endControlFlow()
            return MethodSpec.methodBuilder(if (isBulk) "bulkInsert" else "insert")
                    .addAnnotation(Override::class.java).addParameter(ClassNames.URI, Constants.PARAM_URI)
                    .addParameter(ClassNames.CONTENT_VALUES, Constants.PARAM_CONTENT_VALUES)
                    .addModifiers(if (isBulk) Modifier.PROTECTED else Modifier.PUBLIC, Modifier.FINAL)
                    .addCode(code.build()).returns(if (isBulk) TypeName.INT else ClassNames.URI).build()
        }

}

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
                        method.addCode(uriDefinition.getSegmentsPreparation())
                        method.addCode("cursor = \$T.getDatabase(\$S).getWritableDatabase().query(\$S, projection, ",
                                ClassNames.FLOW_MANAGER,
                                manager.getDatabaseName(contentProviderDefinition.databaseName),
                                tableEndpointDefinition.tableName)
                        method.addCode(uriDefinition.getSelectionAndSelectionArgs())
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
                    .addParameter(ClassNames.URI, Constants.PARAM_URI)
                    .addParameter(ClassNames.CONTENT_VALUES, Constants.PARAM_CONTENT_VALUES)
                    .addParameter(ClassName.get(String::class.java), "selection")
                    .addParameter(ArrayTypeName.of(String::class.java), "selectionArgs")
                    .returns(TypeName.INT)

            method.beginControlFlow("switch(MATCHER.match(\$L))", Constants.PARAM_URI)
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
                        method.addCode(uriDefinition.getSegmentsPreparation())
                        method.addCode(
                                "long count = \$T.getDatabase(\$S).getWritableDatabase().updateWithOnConflict(\$S, \$L, ",
                                ClassNames.FLOW_MANAGER,
                                manager.getDatabaseName(contentProviderDefinition.databaseName),
                                tableEndpointDefinition.tableName,
                                Constants.PARAM_CONTENT_VALUES)
                        method.addCode(uriDefinition.getSelectionAndSelectionArgs())
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

            val code = CodeBlock.builder()
            appendDefault(code)
            method.addCode(code.build())
            method.endControlFlow()

            return method.build()
        }

}