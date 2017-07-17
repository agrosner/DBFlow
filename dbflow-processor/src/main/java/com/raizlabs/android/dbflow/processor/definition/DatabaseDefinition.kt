package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.L
import com.grosner.kpoet.S
import com.grosner.kpoet.`return`
import com.grosner.kpoet.constructor
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.param
import com.grosner.kpoet.public
import com.grosner.kpoet.statement
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ModelViewValidator
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.TableValidator
import com.raizlabs.android.dbflow.processor.utils.`override fun`
import com.raizlabs.android.dbflow.processor.utils.annotation
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import java.util.*
import java.util.regex.Pattern
import javax.lang.model.element.Element

/**
 * Description: Writes [Database] definitions,
 * which contain [Table], [ModelView], and [Migration]
 */
class DatabaseDefinition(manager: ProcessorManager, element: Element) : BaseDefinition(element, manager), TypeDefinition {

    var databaseClassName: String? = null

    var databaseVersion: Int = 0

    internal var foreignKeysSupported: Boolean = false

    internal var consistencyChecksEnabled: Boolean = false

    internal var backupEnabled: Boolean = false

    var insertConflict: ConflictAction? = null

    var updateConflict: ConflictAction? = null

    var classSeparator: String = ""
    var fieldRefSeparator: String = "" // safe field for javapoet

    var objectHolder: DatabaseObjectHolder? = null

    var databaseExtensionName = ""

    init {
        packageName = ClassNames.FLOW_MANAGER_PACKAGE

        element.annotation<Database>()?.let { database ->
            databaseExtensionName = database.databaseExtension
            databaseClassName = element.simpleName.toString()
            consistencyChecksEnabled = database.consistencyCheckEnabled
            backupEnabled = database.backupEnabled

            classSeparator = database.generatedClassSeparator
            fieldRefSeparator = classSeparator

            setOutputClassName(databaseClassName + classSeparator + "Database")

            databaseVersion = database.version
            foreignKeysSupported = database.foreignKeyConstraintsEnforced

            insertConflict = database.insertConflict
            updateConflict = database.updateConflict
        }
    }

    override val extendsClass: TypeName? = ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        writeConstructor(typeBuilder)
        writeGetters(typeBuilder)
    }

    fun validateAndPrepareToWrite() {
        prepareDefinitions()
        validateDefinitions()
    }

    private fun validateDefinitions() {
        elementClassName?.let {
            val map = HashMap<TypeName, TableDefinition>()
            val tableValidator = TableValidator()
            manager.getTableDefinitions(it)
                .filter { tableValidator.validate(ProcessorManager.manager, it) }
                .forEach { it.elementClassName?.let { className -> map.put(className, it) } }
            manager.setTableDefinitions(map, it)

            val modelViewDefinitionMap = HashMap<TypeName, ModelViewDefinition>()
            val modelViewValidator = ModelViewValidator()
            manager.getModelViewDefinitions(it)
                .filter { modelViewValidator.validate(ProcessorManager.manager, it) }
                .forEach { it.elementClassName?.let { className -> modelViewDefinitionMap.put(className, it) } }
            manager.setModelViewDefinitions(modelViewDefinitionMap, it)
        }
    }

    private fun prepareDefinitions() {
        elementClassName?.let {
            manager.getTableDefinitions(it).forEach(TableDefinition::prepareForWrite)
            manager.getModelViewDefinitions(it).forEach(ModelViewDefinition::prepareForWrite)
            manager.getQueryModelDefinitions(it).forEach(QueryModelDefinition::prepareForWrite)
        }
    }

    private fun writeConstructor(builder: TypeSpec.Builder) {

        builder.constructor(param(ClassNames.DATABASE_HOLDER, "holder")) {
            modifiers(public)
            this@DatabaseDefinition.elementClassName?.let { elementClassName ->
                for (definition in manager.getTableDefinitions(elementClassName)) {
                    if (definition.hasGlobalTypeConverters) {
                        statement("addModelAdapter(new \$T(holder, this), holder)", definition.outputClassName)
                    } else {
                        statement("addModelAdapter(new \$T(this), holder)", definition.outputClassName)
                    }
                }

                for (definition in manager.getModelViewDefinitions(elementClassName)) {
                    if (definition.hasGlobalTypeConverters) {
                        statement("addModelViewAdapter(new \$T(holder, this), holder)", definition.outputClassName)
                    } else {
                        statement("addModelViewAdapter(new \$T(this), holder)", definition.outputClassName)
                    }
                }

                for (definition in manager.getQueryModelDefinitions(elementClassName)) {
                    if (definition.hasGlobalTypeConverters) {
                        statement("addQueryModelAdapter(new \$T(holder, this), holder)", definition.outputClassName)
                    } else {
                        statement("addQueryModelAdapter(new \$T(this), holder)", definition.outputClassName)
                    }
                }

                val migrationDefinitionMap = manager.getMigrationsForDatabase(elementClassName)
                migrationDefinitionMap.keys
                    .sortedByDescending { it }
                    .forEach { version ->
                        migrationDefinitionMap[version]
                            ?.sortedBy { it.priority }
                            ?.forEach { migrationDefinition ->
                                statement("addMigration($version, new \$T${migrationDefinition.constructorName})", migrationDefinition.elementClassName)
                            }
                    }
            }
            this
        }

    }

    private fun writeGetters(typeBuilder: TypeSpec.Builder) {
        typeBuilder.apply {
            `override fun`(ParameterizedTypeName.get(ClassName.get(Class::class.java), WildcardTypeName.subtypeOf(Any::class.java)),
                "getAssociatedDatabaseClassFile") {
                modifiers(public, final)
                `return`("\$T.class", elementTypeName)
            }
            `override fun`(TypeName.BOOLEAN, "isForeignKeysSupported") {
                modifiers(public, final)
                `return`(foreignKeysSupported.L)
            }
            `override fun`(TypeName.BOOLEAN, "backupEnabled") {
                modifiers(public, final)
                `return`(backupEnabled.L)
            }
            `override fun`(TypeName.BOOLEAN, "areConsistencyChecksEnabled") {
                modifiers(public, final)
                `return`(consistencyChecksEnabled.L)
            }
            `override fun`(TypeName.INT, "getDatabaseVersion") {
                modifiers(public, final)
                `return`(databaseVersion.L)
            }
            if (!databaseExtensionName.isNullOrBlank()) {
                `override fun`(String::class, "getDatabaseExtensionName") {
                    modifiers(public, final)
                    `return`(databaseExtensionName.S)
                }
            }
        }
    }

    /**
     *
     * Checks if databaseName is valid. It will check if databaseName matches regex pattern
     * [A-Za-z_$]+[a-zA-Z0-9_$]* Examples:   * database - valid  * DbFlow1 - valid  * database.db -
     * invalid (contains a dot)  * 1database - invalid (starts with a number)

     * @param databaseName database name to validate.
     * *
     * @return `true` if parameter is a valid database name, `false` otherwise.
     */
    private fun isValidDatabaseName(databaseName: String?): Boolean {
        val javaClassNamePattern = Pattern.compile("[A-Za-z_$]+[a-zA-Z0-9_$]*")
        return javaClassNamePattern.matcher(databaseName).matches()
    }
}
