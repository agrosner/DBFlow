package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.L
import com.grosner.kpoet.`=`
import com.grosner.kpoet.`break`
import com.grosner.kpoet.`private final field`
import com.grosner.kpoet.`private static final field`
import com.grosner.kpoet.`return`
import com.grosner.kpoet.case
import com.grosner.kpoet.code
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.param
import com.grosner.kpoet.parameterized
import com.grosner.kpoet.public
import com.grosner.kpoet.statement
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider
import com.raizlabs.android.dbflow.annotation.provider.ContentUri
import com.raizlabs.android.dbflow.annotation.provider.NotifyMethod
import com.raizlabs.android.dbflow.annotation.provider.PathSegment
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.TableEndpointValidator
import com.raizlabs.android.dbflow.processor.utils.`override fun`
import com.raizlabs.android.dbflow.processor.utils.annotation
import com.raizlabs.android.dbflow.processor.utils.controlFlow
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.MirroredTypeException

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
        val selectionBuilder = CodeBlock.builder().add("\$T.concatenateWhere(selection, \"", ClassNames.DATABASE_UTILS)
        val selectionArgsBuilder = CodeBlock.builder().add("\$T.appendSelectionArgs(selectionArgs, new \$T[] {",
                ClassNames.DATABASE_UTILS, String::class.java)
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
                                add("long count = \$T.getDatabase(\$T.class).getWritableDatabase().delete(\$S, ",
                                        ClassNames.FLOW_MANAGER, contentProviderDefinition.databaseTypeName,
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
                        code.apply {
                            beginControlFlow("case \$L:", uriDefinition.name)
                            addStatement("\$T adapter = \$T.getModelAdapter(\$T.getTableClassForName(\$T.class, \$S))",
                                    ClassNames.MODEL_ADAPTER, ClassNames.FLOW_MANAGER, ClassNames.FLOW_MANAGER,
                                    contentProviderDefinition.databaseTypeName, it.tableName)

                            add("final long id = FlowManager.getDatabase(\$T.class).getWritableDatabase()",
                                    contentProviderDefinition.databaseTypeName).add(
                                    ".insertWithOnConflict(\$S, null, values, " +
                                            "\$T.getSQLiteDatabaseAlgorithmInt(adapter.getInsertOnConflictAction()));\n", it.tableName,
                                    ClassNames.CONFLICT_ACTION)

                            NotifyMethod(it, uriDefinition, NotifyMethod.INSERT).addCode(this)

                            if (!isBulk) {
                                addStatement("return \$T.withAppendedId(\$L, id)", ClassNames.CONTENT_URIS, Constants.PARAM_URI)
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
                        method.apply {
                            beginControlFlow("case \$L:", uriDefinition.name)
                            addCode(uriDefinition.getSegmentsPreparation())
                            addCode("cursor = \$T.getDatabase(\$T.class).getWritableDatabase().query(\$S, projection, ",
                                    ClassNames.FLOW_MANAGER, contentProviderDefinition.databaseTypeName,
                                    tableEndpointDefinition.tableName)
                            addCode(uriDefinition.getSelectionAndSelectionArgs())
                            addCode(", null, null, sortOrder);\n")
                            addStatement("break")
                            endControlFlow()
                        }
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
                        method.apply {
                            beginControlFlow("case \$L:", uriDefinition.name)
                            addStatement("\$T adapter = \$T.getModelAdapter(\$T.getTableClassForName(\$T.class, \$S))",
                                    ClassNames.MODEL_ADAPTER, ClassNames.FLOW_MANAGER, ClassNames.FLOW_MANAGER,
                                    contentProviderDefinition.databaseTypeName,
                                    tableEndpointDefinition.tableName)
                            addCode(uriDefinition.getSegmentsPreparation())
                            addCode(
                                    "long count = \$T.getDatabase(\$T.class).getWritableDatabase().updateWithOnConflict(\$S, \$L, ",
                                    ClassNames.FLOW_MANAGER, contentProviderDefinition.databaseTypeName,
                                    tableEndpointDefinition.tableName,
                                    Constants.PARAM_CONTENT_VALUES)
                            addCode(uriDefinition.getSelectionAndSelectionArgs())
                            addCode(
                                    ", \$T.getSQLiteDatabaseAlgorithmInt(adapter.getUpdateOnConflictAction()));\n",
                                    ClassNames.CONFLICT_ACTION)

                            val code = CodeBlock.builder()
                            NotifyMethod(tableEndpointDefinition, uriDefinition,
                                    NotifyMethod.UPDATE).addCode(code)
                            addCode(code.build())

                            addStatement("return (int) count")
                            endControlFlow()
                        }
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
class ContentProviderDefinition(typeElement: Element, processorManager: ProcessorManager)
    : BaseDefinition(typeElement, processorManager) {

    var databaseTypeName: TypeName? = null

    var authority: String = ""

    var endpointDefinitions = arrayListOf<TableEndpointDefinition>()

    private val methods: Array<MethodDefinition> = arrayOf(QueryMethod(this, manager),
            InsertMethod(this, false),
            InsertMethod(this, true),
            DeleteMethod(this, manager),
            UpdateMethod(this, manager))

    init {

        val provider = element.annotation<ContentProvider>()
        if (provider != null) {
            try {
                provider.database
            } catch (mte: MirroredTypeException) {
                databaseTypeName = TypeName.get(mte.typeMirror)
            }

            authority = provider.authority

            val validator = TableEndpointValidator()
            val elements = manager.elements.getAllMembers(typeElement as TypeElement)
            elements.forEach {
                if (it.getAnnotation(TableEndpoint::class.java) != null) {
                    val endpointDefinition = TableEndpointDefinition(it, manager)
                    if (validator.validate(processorManager, endpointDefinition)) {
                        endpointDefinitions.add(endpointDefinition)
                    }
                }
            }

        }

    }

    override val extendsClass: TypeName?
        get() = ClassNames.BASE_CONTENT_PROVIDER

    fun prepareForWrite() {
        val databaseDefinition = manager.getDatabaseHolderDefinition(databaseTypeName)!!.databaseDefinition
        setOutputClassName(databaseDefinition?.classSeparator + DEFINITION_NAME)
    }

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {

        typeBuilder.apply {
            var code = 0
            for (endpointDefinition in endpointDefinitions) {
                endpointDefinition.contentUriDefinitions.forEach {
                    `private static final field`(TypeName.INT, it.name) { `=`(code.toString()) }
                    code++
                }
            }

            `private final field`(ClassNames.URI_MATCHER, URI_MATCHER) { `=`("new \$T(\$T.NO_MATCH)", ClassNames.URI_MATCHER, ClassNames.URI_MATCHER) }

            `override fun`(TypeName.BOOLEAN, "onCreate") {
                modifiers(public, final)
                addStatement("final \$T $AUTHORITY = \$L", String::class.java,
                        if (authority.contains("R.string."))
                            "getContext().getString($authority)"
                        else
                            "\"$authority\"")

                for (endpointDefinition in endpointDefinitions) {
                    endpointDefinition.contentUriDefinitions.forEach {
                        val path: String
                        if (!it.path.isNullOrEmpty()) {
                            path = "\"" + it.path + "\""
                        } else {
                            path = CodeBlock.builder().add("\$L.\$L.getPath()", it.elementClassName,
                                    it.name).build().toString()
                        }
                        addStatement("\$L.addURI(\$L, \$L, \$L)", URI_MATCHER, AUTHORITY, path, it.name)
                    }
                }

                addStatement("return super.onCreate()")
            }

            `override fun`(String::class, "getDatabaseName") {
                modifiers(public, final)
                `return`("\$T.getDatabaseName(\$T.class)", ClassNames.FLOW_MANAGER, databaseTypeName)
            }

            `override fun`(String::class, "getType", param(ClassNames.URI, "uri")) {
                modifiers(public, final)
                code {
                    statement("\$T type = null", ClassName.get(String::class.java))
                    controlFlow("switch(\$L.match(uri))", URI_MATCHER) {
                        endpointDefinitions.flatMap { it.contentUriDefinitions }
                                .forEach { uri ->
                                    controlFlow("case \$L:", uri.name) {
                                        statement("type = \$S", uri.type)
                                        `break`()
                                    }
                                }
                        appendDefault(this)
                    }
                    `return`("type")
                }
            }
        }

        methods.mapNotNull { it.methodSpec }
                .forEach { typeBuilder.addMethod(it) }
    }

    companion object {

        internal val DEFINITION_NAME = "Provider"
        val URI_MATCHER = "MATCHER"
        private val AUTHORITY = "AUTHORITY"
    }
}

/**
 * Description:
 */
class ContentUriDefinition(typeElement: Element, processorManager: ProcessorManager) : BaseDefinition(typeElement, processorManager) {

    var name = typeElement.enclosingElement.simpleName.toString() + "_" + typeElement.simpleName.toString()

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

    override fun getElementClassName(element: Element?): ClassName? {
        return null
    }
}