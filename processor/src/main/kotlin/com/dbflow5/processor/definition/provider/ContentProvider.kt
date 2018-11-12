package com.dbflow5.processor.definition.provider

import com.dbflow5.contentprovider.annotation.ContentUri
import com.dbflow5.contentprovider.annotation.NotifyMethod
import com.dbflow5.contentprovider.annotation.PathSegment
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.BaseDefinition
import com.dbflow5.processor.definition.CodeAdder
import com.dbflow5.processor.definition.MethodDefinition
import com.dbflow5.processor.utils.annotation
import com.grosner.kpoet.L
import com.grosner.kpoet.`return`
import com.grosner.kpoet.case
import com.grosner.kpoet.code
import com.grosner.kpoet.parameterized
import com.grosner.kpoet.statement
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

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
 * Get any code needed to use path segments. This should be called before creating the statement that uses
 * [.getSelectionAndSelectionArgs].
 */
internal fun ContentUriDefinition.getSegmentsPreparation() = code {
    if (segments.isNotEmpty()) {
        statement("\$T segments = uri.getPathSegments()",
            parameterized<String>(List::class))
    }
    this
}

/**
 * Get code which creates the `selection` and `selectionArgs` parameters separated by a comma.
 */
internal fun ContentUriDefinition.getSelectionAndSelectionArgs(): CodeBlock {
    if (segments.isEmpty()) {
        return CodeBlock.builder().add("selection, selectionArgs").build()
    } else {
        val selectionBuilder = CodeBlock.builder().add("\$T.concatenateWhere(selection, \"", com.dbflow5.processor.ClassNames.DATABASE_UTILS)
        val selectionArgsBuilder = CodeBlock.builder().add("\$T.appendSelectionArgs(selectionArgs, new \$T[] {",
            com.dbflow5.processor.ClassNames.DATABASE_UTILS, String::class.java)
        var isFirst = true
        for (segment in segments) {
            if (!isFirst) {
                selectionBuilder.add(" AND ")
                selectionArgsBuilder.add(", ")
            }
            selectionBuilder.add("\$L = ?", segment.column)
            selectionArgsBuilder.add("segments.get(\$L)", segment.segment)
            isFirst = false
        }
        selectionBuilder.add("\")")
        selectionArgsBuilder.add("})")
        return CodeBlock.builder().add(selectionBuilder.build()).add(", ").add(selectionArgsBuilder.build()).build()
    }
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
                        code.apply {
                            case(uriDefinition.name.L) {
                                add(uriDefinition.getSegmentsPreparation())
                                add("long count = \$T.getDatabase(\$T.class).delete(\$S, ",
                                    com.dbflow5.processor.ClassNames.FLOW_MANAGER, contentProviderDefinition.databaseTypeName,
                                    it.tableName)
                                add(uriDefinition.getSelectionAndSelectionArgs())
                                add(");\n")

                                NotifyMethod(it, uriDefinition, NotifyMethod.DELETE).addCode(this)

                                `return`("(int) count")
                            }
                        }
                    }
                }
            }

            appendDefault(code)
            code.endControlFlow()

            return MethodSpec.methodBuilder("delete")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(com.dbflow5.processor.ClassNames.URI, PARAM_URI)
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
                        code.apply {
                            beginControlFlow("case \$L:", uriDefinition.name)
                            addStatement("\$T adapter = \$T.getModelAdapter(\$T.getTableClassForName(\$T.class, \$S))",
                                com.dbflow5.processor.ClassNames.MODEL_ADAPTER, com.dbflow5.processor.ClassNames.FLOW_MANAGER, com.dbflow5.processor.ClassNames.FLOW_MANAGER,
                                contentProviderDefinition.databaseTypeName, it.tableName)

                            add("final long id = FlowManager.getDatabase(\$T.class)",
                                contentProviderDefinition.databaseTypeName).add(
                                ".insertWithOnConflict(\$S, null, values, " +
                                    "\$T.getSQLiteDatabaseAlgorithmInt(adapter.getInsertOnConflictAction()));\n", it.tableName,
                                com.dbflow5.processor.ClassNames.CONFLICT_ACTION)

                            NotifyMethod(it, uriDefinition, NotifyMethod.INSERT).addCode(this)

                            if (!isBulk) {
                                addStatement("return \$T.withAppendedId(\$L, id)", com.dbflow5.processor.ClassNames.CONTENT_URIS, Constants.PARAM_URI)
                            } else {
                                addStatement("return id > 0 ? 1 : 0")
                            }
                            endControlFlow()
                        }
                    }
                }
            }

            appendDefault(code)
            code.endControlFlow()
            return MethodSpec.methodBuilder(if (isBulk) "bulkInsert" else "insert")
                .addAnnotation(Override::class.java).addParameter(com.dbflow5.processor.ClassNames.URI, Constants.PARAM_URI)
                .addParameter(com.dbflow5.processor.ClassNames.CONTENT_VALUES, Constants.PARAM_CONTENT_VALUES)
                .addModifiers(if (isBulk) Modifier.PROTECTED else Modifier.PUBLIC, Modifier.FINAL)
                .addCode(code.build()).returns(if (isBulk) TypeName.INT else com.dbflow5.processor.ClassNames.URI).build()
        }

}

/**
 * Description:
 */
class NotifyMethod(private val tableEndpointDefinition: TableEndpointDefinition,
                   private val uriDefinition: ContentUriDefinition, private val notifyMethod: NotifyMethod) : CodeAdder {

    override fun addCode(code: CodeBlock.Builder): CodeBlock.Builder {
        var hasListener = false
        val notifyDefinitionMap = tableEndpointDefinition.notifyDefinitionPathMap[uriDefinition.path]
        if (notifyDefinitionMap != null) {
            val notifyDefinitionList = notifyDefinitionMap[notifyMethod]
            if (notifyDefinitionList != null) {
                for (i in notifyDefinitionList.indices) {
                    val notifyDefinition = notifyDefinitionList[i]
                    if (notifyDefinition.returnsArray) {
                        code.addStatement("\$T[] notifyUris\$L = \$L.\$L(\$L)", com.dbflow5.processor.ClassNames.URI,
                            notifyDefinition.methodName, notifyDefinition.parent,
                            notifyDefinition.methodName, notifyDefinition.params)
                        code.beginControlFlow("for (\$T notifyUri: notifyUris\$L)", com.dbflow5.processor.ClassNames.URI, notifyDefinition.methodName)
                    } else {
                        code.addStatement("\$T notifyUri\$L = \$L.\$L(\$L)", com.dbflow5.processor.ClassNames.URI,
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

            val isUpdateDelete = notifyMethod == NotifyMethod.UPDATE || notifyMethod == NotifyMethod.DELETE
            if (isUpdateDelete) {
                code.beginControlFlow("if (count > 0)")
            }

            code.addStatement("getContext().getContentResolver().notifyChange(uri, null)")

            if (isUpdateDelete) {
                code.endControlFlow()
            }
        }
        return code
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
                .addParameter(com.dbflow5.processor.ClassNames.URI, "uri")
                .addParameter(ArrayTypeName.of(String::class.java), "projection")
                .addParameter(ClassName.get(String::class.java), "selection")
                .addParameter(ArrayTypeName.of(String::class.java), "selectionArgs")
                .addParameter(ClassName.get(String::class.java), "sortOrder")
                .returns(com.dbflow5.processor.ClassNames.CURSOR)

            method.addStatement("\$L cursor = null", com.dbflow5.processor.ClassNames.CURSOR)
            method.beginControlFlow("switch(\$L.match(uri))", ContentProviderDefinition.URI_MATCHER)
            for (tableEndpointDefinition in contentProviderDefinition.endpointDefinitions) {
                tableEndpointDefinition.contentUriDefinitions
                    .asSequence()
                    .filter { it.queryEnabled }
                    .forEach {
                        method.apply {
                            beginControlFlow("case \$L:", it.name)
                            addCode(it.getSegmentsPreparation())
                            addCode("cursor = \$T.getDatabase(\$T.class).query(\$S, projection, ",
                                com.dbflow5.processor.ClassNames.FLOW_MANAGER, contentProviderDefinition.databaseTypeName,
                                tableEndpointDefinition.tableName)
                            addCode(it.getSelectionAndSelectionArgs())
                            addCode(", null, null, sortOrder);\n")
                            addStatement("break")
                            endControlFlow()
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
                .addParameter(com.dbflow5.processor.ClassNames.URI, Constants.PARAM_URI)
                .addParameter(com.dbflow5.processor.ClassNames.CONTENT_VALUES, Constants.PARAM_CONTENT_VALUES)
                .addParameter(ClassName.get(String::class.java), "selection")
                .addParameter(ArrayTypeName.of(String::class.java), "selectionArgs")
                .returns(TypeName.INT)

            method.beginControlFlow("switch(MATCHER.match(\$L))", Constants.PARAM_URI)
            for (tableEndpointDefinition in contentProviderDefinition.endpointDefinitions) {
                tableEndpointDefinition.contentUriDefinitions
                    .asSequence()
                    .filter { it.updateEnabled }
                    .forEach {
                        method.apply {
                            beginControlFlow("case \$L:", it.name)
                            addStatement("\$T adapter = \$T.getModelAdapter(\$T.getTableClassForName(\$T.class, \$S))",
                                com.dbflow5.processor.ClassNames.MODEL_ADAPTER, com.dbflow5.processor.ClassNames.FLOW_MANAGER, com.dbflow5.processor.ClassNames.FLOW_MANAGER,
                                contentProviderDefinition.databaseTypeName,
                                tableEndpointDefinition.tableName)
                            addCode(it.getSegmentsPreparation())
                            addCode(
                                "long count = \$T.getDatabase(\$T.class).updateWithOnConflict(\$S, \$L, ",
                                com.dbflow5.processor.ClassNames.FLOW_MANAGER, contentProviderDefinition.databaseTypeName,
                                tableEndpointDefinition.tableName,
                                Constants.PARAM_CONTENT_VALUES)
                            addCode(it.getSelectionAndSelectionArgs())
                            addCode(
                                ", \$T.getSQLiteDatabaseAlgorithmInt(adapter.getUpdateOnConflictAction()));\n",
                                com.dbflow5.processor.ClassNames.CONFLICT_ACTION)

                            val code = CodeBlock.builder()
                            NotifyMethod(tableEndpointDefinition, it,
                                NotifyMethod.UPDATE).addCode(code)
                            addCode(code.build())

                            addStatement("return (int) count")
                            endControlFlow()
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

/**
 * Description:
 */
class ContentUriDefinition(typeElement: Element, processorManager: ProcessorManager) : BaseDefinition(typeElement, processorManager) {

    var name = "${typeElement.enclosingElement.simpleName}_${typeElement.simpleName}"

    var path = ""

    var type = ""

    var queryEnabled = false

    var insertEnabled = false

    var deleteEnabled = false


    var updateEnabled = false

    var segments = arrayOf<PathSegment>()

    init {
        typeElement.annotation<ContentUri>()?.let { contentUri ->
            path = contentUri.path
            type = contentUri.type
            queryEnabled = contentUri.queryEnabled
            insertEnabled = contentUri.insertEnabled
            deleteEnabled = contentUri.deleteEnabled
            updateEnabled = contentUri.updateEnabled

            segments = contentUri.segments
        }

        if (typeElement is VariableElement) {
            if (ClassNames.URI != elementTypeName) {
                processorManager.logError("Content Uri field returned wrong type. It must return a Uri")
            }
        } else if (typeElement is ExecutableElement) {
            if (ClassNames.URI != elementTypeName) {
                processorManager.logError("ContentUri method returns wrong type. It must return Uri")
            }
        }
    }

}