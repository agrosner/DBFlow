package com.raizlabs.android.dbflow.processor.definition

import com.google.common.collect.Lists
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition
import com.raizlabs.android.dbflow.processor.definition.method.DeleteMethod
import com.raizlabs.android.dbflow.processor.definition.method.InsertMethod
import com.raizlabs.android.dbflow.processor.definition.method.QueryMethod
import com.raizlabs.android.dbflow.processor.definition.method.UpdateMethod
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.processor.validator.TableEndpointValidator
import com.squareup.javapoet.*
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

/**
 * Description:
 */
class ContentProviderDefinition(typeElement: Element, processorManager: ProcessorManager)
: BaseDefinition(typeElement, processorManager) {

    var databaseName: TypeName? = null
    var databaseNameString: String = ""

    var authority: String = ""

    var endpointDefinitions: MutableList<TableEndpointDefinition> = Lists.newArrayList<TableEndpointDefinition>()

    private val methods: Array<MethodDefinition>

    init {

        val provider = element.getAnnotation(ContentProvider::class.java)
        if (provider != null) {
            try {
                provider.database
            } catch (mte: MirroredTypeException) {
                databaseName = TypeName.get(mte.typeMirror)
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

        methods = arrayOf(QueryMethod(this, manager), InsertMethod(this, false),
                InsertMethod(this, true), DeleteMethod(this, manager),
                UpdateMethod(this, manager))
    }

    override val extendsClass: TypeName?
        get() = ClassNames.BASE_CONTENT_PROVIDER

    fun prepareForWrite() {
        val databaseDefinition = manager.getDatabaseHolderDefinition(databaseName)!!.databaseDefinition
        databaseNameString = databaseDefinition?.databaseName ?: ""
        setOutputClassName(databaseDefinition?.classSeparator + DEFINITION_NAME)
    }

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {

        typeBuilder.addField(FieldSpec.builder(ClassName.get(String::class.java), AUTHORITY, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("\$S", authority).build())

        var code = 0
        for (endpointDefinition in endpointDefinitions) {
            for (contentUriDefinition in endpointDefinition.contentUriDefinitions) {
                typeBuilder.addField(FieldSpec.builder(TypeName.INT, contentUriDefinition.name,
                        Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer(code.toString()).build())
                code++
            }
        }

        val uriField = FieldSpec.builder(ClassNames.URI_MATCHER, URI_MATCHER,
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)

        val initializer = CodeBlock.builder().addStatement("new \$T(\$T.NO_MATCH)", ClassNames.URI_MATCHER, ClassNames.URI_MATCHER).add("static {\n")

        for (endpointDefinition in endpointDefinitions) {
            for (contentUriDefinition in endpointDefinition.contentUriDefinitions) {
                val path: String
                if (!contentUriDefinition.path.isNullOrEmpty()) {
                    path = "\"" + contentUriDefinition.path + "\""
                } else {
                    path = CodeBlock.builder().add("\$L.\$L.getPath()", contentUriDefinition.elementClassName, contentUriDefinition.name).build().toString()
                }
                initializer.addStatement("\$L.addURI(\$L, \$L, \$L)", URI_MATCHER, AUTHORITY, path, contentUriDefinition.name)
            }
        }
        initializer.add("}\n")
        typeBuilder.addField(uriField.initializer(initializer.build()).build())

        typeBuilder.addMethod(MethodSpec.methodBuilder("getDatabaseName").addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL).addStatement("return \$S", databaseNameString).returns(ClassName.get(String::class.java)).build())

        val getTypeBuilder = MethodSpec.methodBuilder("getType").addAnnotation(Override::class.java).addParameter(ClassNames.URI, "uri").returns(ClassName.get(String::class.java)).addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        val getTypeCode = CodeBlock.builder().addStatement("\$T type = null", ClassName.get(String::class.java)).beginControlFlow("switch(\$L.match(uri))", URI_MATCHER)

        for (tableEndpointDefinition in endpointDefinitions) {
            for (uriDefinition in tableEndpointDefinition.contentUriDefinitions) {
                getTypeCode.beginControlFlow("case \$L:", uriDefinition.name).addStatement("type = \$S", uriDefinition.type).addStatement("break").endControlFlow()
            }
        }
        getTypeCode.beginControlFlow("default:").addStatement("throw new \$T(\$S + \$L)", ClassName.get(IllegalArgumentException::class.java), "Unknown URI", "uri").endControlFlow()
        getTypeCode.endControlFlow()
        getTypeCode.addStatement("return type")

        getTypeBuilder.addCode(getTypeCode.build())
        typeBuilder.addMethod(getTypeBuilder.build())

        for (method in methods) {
            val methodSpec = method.methodSpec
            if (methodSpec != null) {
                typeBuilder.addMethod(methodSpec)
            }
        }


    }

    companion object {

        internal val DEFINITION_NAME = "Provider"

        internal val DATABASE_FIELD = "database"

        val URI_MATCHER = "MATCHER"

        private val AUTHORITY = "AUTHORITY"
    }
}
