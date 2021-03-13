package com.dbflow5.processor.definition

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.Database
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ModelViewValidator
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.TableValidator
import com.dbflow5.processor.utils.`override fun`
import com.dbflow5.processor.utils.isSubclass
import com.grosner.kpoet.L
import com.grosner.kpoet.`return`
import com.grosner.kpoet.constructor
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.param
import com.grosner.kpoet.public
import com.grosner.kpoet.statement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

/**
 * Description: Writes [Database] definitions,
 * which contain [Table], [ModelView], and [Migration]
 */
class DatabaseDefinition(database: Database,
                         manager: ProcessorManager, element: Element)
    : BaseDefinition(element, manager, packageName = ClassNames.FLOW_MANAGER_PACKAGE), TypeDefinition {

    private val databaseVersion: Int = database.version
    private val foreignKeysSupported = database.foreignKeyConstraintsEnforced
    private val consistencyChecksEnabled = database.consistencyCheckEnabled
    private val backupEnabled = database.backupEnabled

    val insertConflict: ConflictAction = database.insertConflict
    val updateConflict: ConflictAction = database.updateConflict

    var objectHolder: DatabaseObjectHolder? = null

    init {
        setOutputClassName("${elementName}_Database")

        if (!element.modifiers.contains(Modifier.ABSTRACT)
            || element.modifiers.contains(Modifier.PRIVATE)
            || !typeElement.isSubclass(manager.processingEnvironment, ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME)) {
            manager.logError("$elementClassName must be a visible abstract class that " +
                "extends ${ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME}")
        }
    }

    override val extendsClass: TypeName? = elementClassName

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        writeConstructor(typeBuilder)
        writeGetters(typeBuilder)
    }

    fun validateAndPrepareToWrite() {
        prepareDefinitions()
        validateDefinitions()
    }

    private fun validateDefinitions() {
        elementClassName?.let { className ->
            val map = hashMapOf<TypeName, TableDefinition>()
            val tableValidator = TableValidator()
            manager.getTableDefinitions(className)
                .filter { tableValidator.validate(ProcessorManager.manager, it) }
                .forEach { it.elementClassName?.let { className -> map.put(className, it) } }
            manager.setTableDefinitions(map, className)

            val modelViewDefinitionMap = hashMapOf<TypeName, ModelViewDefinition>()
            val modelViewValidator = ModelViewValidator()
            manager.getModelViewDefinitions(className)
                .filter { modelViewValidator.validate(ProcessorManager.manager, it) }
                .forEach { it.elementClassName?.let { className -> modelViewDefinitionMap.put(className, it) } }
            manager.setModelViewDefinitions(modelViewDefinitionMap, className)
        }
    }

    private fun prepareDefinitions() {
        elementClassName?.let { className ->
            manager.getTableDefinitions(className).forEach(TableDefinition::prepareForWrite)
            manager.getModelViewDefinitions(className).forEach(ModelViewDefinition::prepareForWrite)
            manager.getQueryModelDefinitions(className).forEach(QueryModelDefinition::prepareForWrite)
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
                        statement("addRetrievalAdapter(new \$T(holder, this), holder)", definition.outputClassName)
                    } else {
                        statement("addRetrievalAdapter(new \$T(this), holder)", definition.outputClassName)
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
        }
    }
}
