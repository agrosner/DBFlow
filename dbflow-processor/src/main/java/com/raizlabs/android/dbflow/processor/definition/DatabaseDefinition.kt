package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.processor.*
import com.raizlabs.android.dbflow.processor.utils.*
import com.squareup.javapoet.*
import java.util.*
import java.util.regex.Pattern
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

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

        val database = element.getAnnotation(Database::class.java)
        if (database != null) {
            databaseName = database.name
            databaseExtensionName = database.databaseExtension;
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

        builder.constructor {
            modifiers(Modifier.PUBLIC)
            addParameter(ClassNames.DATABASE_HOLDER, "holder")
            val elementClassName = this@DatabaseDefinition.elementClassName
            if (elementClassName != null) {
                for (tableDefinition in manager.getTableDefinitions(elementClassName)) {
                    addStatement("holder.putDatabaseForTable(\$T.class, this)", tableDefinition.elementClassName)
                }

                for (modelViewDefinition in manager.getModelViewDefinitions(elementClassName)) {
                    addStatement("holder.putDatabaseForTable(\$T.class, this)", modelViewDefinition.elementClassName)
                }

                for (queryModelDefinition in manager.getQueryModelDefinitions(elementClassName)) {
                    addStatement("holder.putDatabaseForTable(\$T.class, this)", queryModelDefinition.elementClassName)
                }

                val migrationDefinitionMap = manager.getMigrationsForDatabase(elementClassName)
                if (!migrationDefinitionMap.isEmpty()) {
                    val versionSet = ArrayList<Int>(migrationDefinitionMap.keys)
                    Collections.sort<Int>(versionSet)
                    for (version in versionSet) {
                        val migrationDefinitions = migrationDefinitionMap[version]
                        migrationDefinitions?.let {
                            Collections.sort(migrationDefinitions, { o1, o2 -> Integer.valueOf(o2.priority)!!.compareTo(o1.priority) })
                            addStatement("\$T migrations\$L = new \$T()", ParameterizedTypeName.get(ClassName.get(List::class.java), ClassNames.MIGRATION),
                                version, ParameterizedTypeName.get(ClassName.get(ArrayList::class.java), ClassNames.MIGRATION))
                            addStatement("\$L.put(\$L, migrations\$L)", DatabaseHandler.MIGRATION_FIELD_NAME,
                                version, version)
                            for (migrationDefinition in migrationDefinitions) {
                                addStatement("migrations\$L.add(new \$T\$L)", version, migrationDefinition.elementClassName,
                                    migrationDefinition.constructorName)
                            }
                        }
                    }
                }

                for (tableDefinition in manager.getTableDefinitions(elementClassName)) {
                    addStatement("\$L.add(\$T.class)", DatabaseHandler.MODEL_FIELD_NAME, tableDefinition.elementClassName)
                    addStatement("\$L.put(\$S, \$T.class)", DatabaseHandler.MODEL_NAME_MAP, tableDefinition.tableName, tableDefinition.elementClassName)
                    addStatement("\$L.put(\$T.class, new \$T(holder, this))", DatabaseHandler.MODEL_ADAPTER_MAP_FIELD_NAME,
                        tableDefinition.elementClassName, tableDefinition.outputClassName)
                }

                for (modelViewDefinition in manager.getModelViewDefinitions(elementClassName)) {
                    addStatement("\$L.add(\$T.class)", DatabaseHandler.MODEL_VIEW_FIELD_NAME, modelViewDefinition.elementClassName)
                    addStatement("\$L.put(\$T.class, new \$T(holder, this))", DatabaseHandler.MODEL_VIEW_ADAPTER_MAP_FIELD_NAME,
                        modelViewDefinition.elementClassName, modelViewDefinition.outputClassName)
                }

                for (queryModelDefinition in manager.getQueryModelDefinitions(elementClassName)) {
                    addStatement("\$L.put(\$T.class, new \$T(holder, this))", DatabaseHandler.QUERY_MODEL_ADAPTER_MAP_FIELD_NAME,
                        queryModelDefinition.elementClassName, queryModelDefinition.outputClassName)
                }
            }
            this
        }
    }

    private fun writeGetters(typeBuilder: TypeSpec.Builder) {
        typeBuilder.apply {
            overrideMethod("getAssociatedDatabaseClassFile" returns ParameterizedTypeName.get(ClassName.get(Class::class.java),
                WildcardTypeName.subtypeOf(Any::class.java)) modifiers publicFinal) {
                addStatement("return \$T.class", elementTypeName)
            }
            overrideMethod("isForeignKeysSupported" returns TypeName.BOOLEAN modifiers publicFinal) {
                addStatement("return \$L", foreignKeysSupported)
            }
            overrideMethod("isInMemory" returns TypeName.BOOLEAN modifiers publicFinal) {
                addStatement("return \$L", isInMemory)
            }
            overrideMethod("backupEnabled" returns TypeName.BOOLEAN modifiers publicFinal) {
                addStatement("return \$L", backupEnabled)
            }
            overrideMethod("areConsistencyChecksEnabled" returns TypeName.BOOLEAN modifiers publicFinal) {
                addStatement("return \$L", consistencyChecksEnabled)
            }
            overrideMethod("getDatabaseVersion" returns TypeName.INT modifiers publicFinal) {
                addStatement("return \$L", databaseVersion)
            }
            overrideMethod("getDatabaseName" returns String::class modifiers publicFinal) {
                addStatement("return \$S", databaseName)
            }
            if (!databaseExtensionName.isNullOrBlank()) {
                overrideMethod("getDatabaseExtensionName" returns String::class modifiers publicFinal) {
                    addStatement("return \$S", databaseExtensionName)
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
