package com.dbflow5.processor.definition.provider

import com.dbflow5.contentprovider.annotation.ContentProvider
import com.dbflow5.contentprovider.annotation.TableEndpoint
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.TableEndpointValidator
import com.dbflow5.processor.definition.BaseDefinition
import com.dbflow5.processor.definition.MethodDefinition
import com.dbflow5.processor.utils.`override fun`
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.controlFlow
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.processor.utils.isSubclass
import com.dbflow5.processor.utils.toTypeElement
import com.grosner.kpoet.`=`
import com.grosner.kpoet.`break`
import com.grosner.kpoet.`private final field`
import com.grosner.kpoet.`private static final field`
import com.grosner.kpoet.`return`
import com.grosner.kpoet.code
import com.grosner.kpoet.constructor
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.param
import com.grosner.kpoet.public
import com.grosner.kpoet.statement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Description: Writes the [ContentProvider] class.
 */
class ContentProviderDefinition(provider: ContentProvider,
                                typeElement: Element, processorManager: ProcessorManager)
    : BaseDefinition(typeElement, processorManager) {

    val databaseTypeName: TypeName = provider.extractTypeNameFromAnnotation { it.database }
    val endpointDefinitions = arrayListOf<TableEndpointDefinition>()

    private val authority: String = provider.authority
    private val holderClass: TypeName = provider.extractTypeNameFromAnnotation { it.initializeHolderClass }

    private val methods: Array<MethodDefinition> = arrayOf(QueryMethod(this, manager),
        InsertMethod(this, false),
        InsertMethod(this, true),
        DeleteMethod(this, manager),
        UpdateMethod(this, manager))

    init {
        setOutputClassName("_$DEFINITION_NAME")

        val validator = TableEndpointValidator()
        val elements = manager.elements.getAllMembers(typeElement as TypeElement)
        elements.forEach { element ->
            element.annotation<TableEndpoint>()?.let { tableEndpoint ->
                val endpointDefinition = TableEndpointDefinition(tableEndpoint, element, manager)
                if (validator.validate(processorManager, endpointDefinition)) {
                    endpointDefinitions.add(endpointDefinition)
                }
            }
        }

        if (!databaseTypeName.toTypeElement(manager).isSubclass(manager.processingEnvironment,
                ClassNames.CONTENT_PROVIDER_DATABASE)) {
            manager.logError("A Content Provider database $elementClassName " +
                "must extend ${ClassNames.CONTENT_PROVIDER_DATABASE}")
        }

        if (holderClass != TypeName.OBJECT &&
            !holderClass.toTypeElement(manager).isSubclass(manager.processingEnvironment,
                ClassNames.DATABASE_HOLDER)) {
            manager.logError("The initializeHolderClass $holderClass must point to a subclass" +
                "of ${ClassNames.DATABASE_HOLDER}")
        }
    }

    override val extendsClass: TypeName? = ClassNames.BASE_CONTENT_PROVIDER

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {

        typeBuilder.apply {
            if (holderClass != TypeName.OBJECT) {
                constructor {
                    addStatement("super(\$T.class)", holderClass)
                }
            }

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
                        val path = if (!it.path.isNullOrEmpty()) {
                            "\"${it.path}\""
                        } else {
                            CodeBlock.builder().add("\$L.\$L.getPath()", it.elementClassName,
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

        internal const val DEFINITION_NAME = "Provider"
        const val URI_MATCHER = "MATCHER"
        private const val AUTHORITY = "AUTHORITY"
    }
}