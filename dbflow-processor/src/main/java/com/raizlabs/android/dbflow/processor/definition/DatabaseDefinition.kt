package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.*
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ModelViewValidator
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.TableValidator
import com.raizlabs.android.dbflow.processor.utils.`override fun`
import com.raizlabs.android.dbflow.processor.utils.annotation
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.squareup.javapoet.*
import java.util.*
import java.util.regex.Pattern
import javax.lang.model.element.Element

/**
 * Description: Writes [Database] definitions,
 * which contain [Table], [ModelView], and [Migration]
 */
class DatabaseDefinition(manager: ProcessorManager, element: Element) : BaseDefinition(element, manager), TypeDefinition {

    var databaseName: String? = null

    var databaseVersion: Int = 0

    internal var foreignKeysSupported: Boolean = false

    internal var consistencyChecksEnabled: Boolean = false

    internal var backupEnabled: Boolean = false

    var insertConflict: ConflictAction? = null

    var updateConflict: ConflictAction? = null

    var classSeparator: String = ""
    var fieldRefSeparator: String = "" // safe field for javapoet

    var isInMemory: Boolean = false

    var objectHolder: DatabaseObjectHolder? = null

    var databaseExtensionName = ""

    var databaseFileName = ""

    init {
        packageName = ClassNames.FLOW_MANAGER_PACKAGE

        element.annotation<Database>()?.let { database ->
            databaseName = database.name
            databaseExtensionName = database.databaseExtension
            if (databaseName.isNullOrEmpty()) {
                databaseName = element.simpleName.toString()
            }
            if (!isValidDatabaseName(databaseName)) {
                throw Error("Database name [ " + databaseName + " ] is not valid. It must pass [A-Za-z_$]+[a-zA-Z0-9_$]* " +
                    "regex so it can't start with a number or contain any special character except '$'. Especially a dot character is not allowed!")
            }

            consistencyChecksEnabled = database.consistencyCheckEnabled
            backupEnabled = database.backupEnabled

            classSeparator = database.generatedClassSeparator
            fieldRefSeparator = classSeparator

            setOutputClassName(databaseName + classSeparator + "Database")

            databaseVersion = database.version
            foreignKeysSupported = database.foreignKeyConstraintsEnforced

            insertConflict = database.insertConflict
            updateConflict = database.updateConflict
            isInMemory = database.inMemory
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
                if (!migrationDefinitionMap.isEmpty()) {
                    val versionSet = ArrayList<Int>(migrationDefinitionMap.keys)
                    Collections.sort<Int>(versionSet)
                    for (version in versionSet) {
                        val migrationDefinitions = migrationDefinitionMap[version]
                        migrationDefinitions?.let {
                            Collections.sort(migrationDefinitions, { o1, o2 -> Integer.valueOf(o2.priority)!!.compareTo(o1.priority) })
                            for (migrationDefinition in migrationDefinitions) {
                                statement("addMigration($version, new \$T${migrationDefinition.constructorName})", migrationDefinition.elementClassName)
                            }
                        }
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
            `override fun`(TypeName.BOOLEAN, "isInMemory") {
                modifiers(public, final)
                `return`(isInMemory.L)
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
            `override fun`(String::class, "getDatabaseName") {
                modifiers(public, final)
                `return`(databaseName.S)
            }
            if (!databaseExtensionName.isNullOrBlank()) {
                `override fun`(String::class, "getDatabaseExtensionName") {
                    modifiers(public, final)
                    `return`(databaseExtensionName.S)
                }
            }
        }

        if (databaseFileName.isNullOrBlank().not()) {
            typeBuilder.addMethod(MethodSpec.methodBuilder("getDatabaseFileName")
                .addAnnotation(Override::class.java).addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return " + databaseFileName)
                .returns(String::class.java)
                .build())
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
