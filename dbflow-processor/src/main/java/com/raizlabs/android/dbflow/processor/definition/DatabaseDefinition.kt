package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.*
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.processor.*
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
            // TODO: validate them here before preparing them
            val map = HashMap<TypeName, TableDefinition>()
            val tableValidator = TableValidator()
            for (tableDefinition in manager.getTableDefinitions(it)) {
                if (tableValidator.validate(ProcessorManager.manager, tableDefinition)) {
                    tableDefinition.elementClassName?.let { className -> map.put(className, tableDefinition) }
                }
            }
            manager.setTableDefinitions(map, it)

            val modelViewDefinitionMap = HashMap<TypeName, ModelViewDefinition>()
            val modelViewValidator = ModelViewValidator()
            for (modelViewDefinition in manager.getModelViewDefinitions(it)) {
                if (modelViewValidator.validate(ProcessorManager.manager, modelViewDefinition)) {
                    modelViewDefinition.elementClassName?.let { className -> modelViewDefinitionMap.put(className, modelViewDefinition) }
                }
            }
            manager.setModelViewDefinitions(modelViewDefinitionMap, it)
        }

    }

    private fun prepareDefinitions() {
        elementClassName?.let {
            for (tableDefinition in manager.getTableDefinitions(it)) {
                tableDefinition.prepareForWrite()
            }

            for (modelViewDefinition in manager.getModelViewDefinitions(it)) {
                modelViewDefinition.prepareForWrite()
            }

            for (queryModelDefinition in manager.getQueryModelDefinitions(it)) {
                queryModelDefinition.prepareForWrite()
            }
        }
    }

    private fun writeConstructor(builder: TypeSpec.Builder) {

        builder.constructor(param(ClassNames.DATABASE_HOLDER, "holder")) {
            modifiers(public)
            val elementClassName = this@DatabaseDefinition.elementClassName
            if (elementClassName != null) {
                for (tableDefinition in manager.getTableDefinitions(elementClassName)) {
                    statement("holder.putDatabaseForTable(\$T.class, this)", tableDefinition.elementClassName)
                    statement("\$L.put(\$S, \$T.class)", DatabaseHandler.MODEL_NAME_MAP, tableDefinition.tableName, tableDefinition.elementClassName)
                    statement("\$L.put(\$T.class, new \$T(holder, this))", DatabaseHandler.MODEL_ADAPTER_MAP_FIELD_NAME,
                            tableDefinition.elementClassName, tableDefinition.outputClassName)
                }

                for (modelViewDefinition in manager.getModelViewDefinitions(elementClassName)) {
                    statement("holder.putDatabaseForTable(\$T.class, this)", modelViewDefinition.elementClassName)
                    statement("\$L.put(\$T.class, new \$T(holder, this))", DatabaseHandler.MODEL_VIEW_ADAPTER_MAP_FIELD_NAME,
                            modelViewDefinition.elementClassName, modelViewDefinition.outputClassName)
                }

                for (queryModelDefinition in manager.getQueryModelDefinitions(elementClassName)) {
                    statement("holder.putDatabaseForTable(\$T.class, this)", queryModelDefinition.elementClassName)
                    statement("\$L.put(\$T.class, new \$T(holder, this))", DatabaseHandler.QUERY_MODEL_ADAPTER_MAP_FIELD_NAME,
                            queryModelDefinition.elementClassName, queryModelDefinition.outputClassName)
                }

                val migrationDefinitionMap = manager.getMigrationsForDatabase(elementClassName)
                if (!migrationDefinitionMap.isEmpty()) {
                    val versionSet = ArrayList<Int>(migrationDefinitionMap.keys)
                    Collections.sort<Int>(versionSet)
                    for (version in versionSet) {
                        val migrationDefinitions = migrationDefinitionMap[version]
                        migrationDefinitions?.let {
                            Collections.sort(migrationDefinitions, { o1, o2 -> Integer.valueOf(o2.priority)!!.compareTo(o1.priority) })
                            statement("\$T migrations\$L = new \$T()", ParameterizedTypeName.get(ClassName.get(List::class.java), ClassNames.MIGRATION),
                                    version, ParameterizedTypeName.get(ClassName.get(ArrayList::class.java), ClassNames.MIGRATION))
                            statement("\$L.put(\$L, migrations\$L)", DatabaseHandler.MIGRATION_FIELD_NAME,
                                    version, version)
                            for (migrationDefinition in migrationDefinitions) {
                                statement("migrations\$L.add(new \$T\$L)", version, migrationDefinition.elementClassName,
                                        migrationDefinition.constructorName)
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
