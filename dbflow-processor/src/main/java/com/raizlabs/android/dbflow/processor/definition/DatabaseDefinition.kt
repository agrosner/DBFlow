package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.DatabaseHandler
import com.raizlabs.android.dbflow.processor.ModelViewValidator
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.TableValidator
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
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

    var databaseFileName = ""

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

        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC).addParameter(ClassNames.DATABASE_HOLDER, "holder")

        elementClassName?.let {

            for (tableDefinition in manager.getTableDefinitions(it)) {
                constructor.addStatement("holder.putDatabaseForTable(\$T.class, this)", tableDefinition.elementClassName)
            }

            for (modelViewDefinition in manager.getModelViewDefinitions(it)) {
                constructor.addStatement("holder.putDatabaseForTable(\$T.class, this)", modelViewDefinition.elementClassName)
            }

            for (queryModelDefinition in manager.getQueryModelDefinitions(it)) {
                constructor.addStatement("holder.putDatabaseForTable(\$T.class, this)", queryModelDefinition.elementClassName)
            }

            val migrationDefinitionMap = manager.getMigrationsForDatabase(it)
            if (!migrationDefinitionMap.isEmpty()) {
                val versionSet = ArrayList<Int>(migrationDefinitionMap.keys)
                Collections.sort<Int>(versionSet)
                for (version in versionSet) {
                    val migrationDefinitions = migrationDefinitionMap[version]
                    migrationDefinitions?.let {
                        Collections.sort(migrationDefinitions, { o1, o2 -> Integer.valueOf(o2.priority)!!.compareTo(o1.priority) })
                        constructor.addStatement("\$T migrations\$L = new \$T()", ParameterizedTypeName.get(ClassName.get(List::class.java), ClassNames.MIGRATION),
                                version, ParameterizedTypeName.get(ClassName.get(ArrayList::class.java), ClassNames.MIGRATION))
                        constructor.addStatement("\$L.put(\$L, migrations\$L)", DatabaseHandler.MIGRATION_FIELD_NAME,
                                version, version)
                        for (migrationDefinition in migrationDefinitions) {
                            constructor.addStatement("migrations\$L.add(new \$T\$L)", version, migrationDefinition.elementClassName,
                                    migrationDefinition.constructorName)
                        }
                    }
                }
            }

            for (tableDefinition in manager.getTableDefinitions(it)) {
                constructor.addStatement("\$L.add(\$T.class)", DatabaseHandler.MODEL_FIELD_NAME, tableDefinition.elementClassName)
                constructor.addStatement("\$L.put(\$S, \$T.class)", DatabaseHandler.MODEL_NAME_MAP, tableDefinition.tableName, tableDefinition.elementClassName)
                constructor.addStatement("\$L.put(\$T.class, new \$T(holder, this))", DatabaseHandler.MODEL_ADAPTER_MAP_FIELD_NAME,
                        tableDefinition.elementClassName, tableDefinition.outputClassName)
            }

            for (modelViewDefinition in manager.getModelViewDefinitions(it)) {
                constructor.addStatement("\$L.add(\$T.class)", DatabaseHandler.MODEL_VIEW_FIELD_NAME, modelViewDefinition.elementClassName)
                constructor.addStatement("\$L.put(\$T.class, new \$T(holder, this))", DatabaseHandler.MODEL_VIEW_ADAPTER_MAP_FIELD_NAME,
                        modelViewDefinition.elementClassName, modelViewDefinition.outputClassName)
            }

            for (queryModelDefinition in manager.getQueryModelDefinitions(it)) {
                constructor.addStatement("\$L.put(\$T.class, new \$T(holder, this))", DatabaseHandler.QUERY_MODEL_ADAPTER_MAP_FIELD_NAME,
                        queryModelDefinition.elementClassName, queryModelDefinition.outputClassName)
            }
        }

        builder.addMethod(constructor.build())
    }

    private fun writeGetters(typeBuilder: TypeSpec.Builder) {

        typeBuilder.addMethod(MethodSpec.methodBuilder("getAssociatedDatabaseClassFile")
                .addAnnotation(Override::class.java).addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return \$T.class", elementTypeName)
                .returns(ParameterizedTypeName.get(ClassName.get(Class::class.java), WildcardTypeName.subtypeOf(Any::class.java))).build())

        typeBuilder.addMethod(MethodSpec.methodBuilder("isForeignKeysSupported")
                .addAnnotation(Override::class.java).addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return \$L", foreignKeysSupported).returns(TypeName.BOOLEAN).build())

        typeBuilder.addMethod(MethodSpec.methodBuilder("isInMemory").addAnnotation(Override::class.java)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS).addStatement("return \$L", isInMemory)
                .returns(TypeName.BOOLEAN).build())

        typeBuilder.addMethod(MethodSpec.methodBuilder("backupEnabled").addAnnotation(Override::class.java)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS).addStatement("return \$L", backupEnabled)
                .returns(TypeName.BOOLEAN).build())

        typeBuilder.addMethod(MethodSpec.methodBuilder("areConsistencyChecksEnabled")
                .addAnnotation(Override::class.java).addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return \$L", consistencyChecksEnabled).returns(TypeName.BOOLEAN).build())

        typeBuilder.addMethod(MethodSpec.methodBuilder("getDatabaseVersion")
                .addAnnotation(Override::class.java).addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return \$L", databaseVersion).returns(TypeName.INT).build())

        typeBuilder.addMethod(MethodSpec.methodBuilder("getDatabaseName")
                .addAnnotation(Override::class.java).addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return \$S", databaseName!!).returns(ClassName.get(String::class.java)).build())

        if (databaseExtensionName.isNullOrBlank().not()) {
            typeBuilder.addMethod(MethodSpec.methodBuilder("getDatabaseExtensionName")
                    .addAnnotation(Override::class.java).addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                    .addStatement("return \$S", databaseExtensionName)
                    .returns(String::class.java)
                    .build())
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
