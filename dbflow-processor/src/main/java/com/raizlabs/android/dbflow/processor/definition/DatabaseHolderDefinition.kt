package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.*
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.DatabaseHandler
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.squareup.javapoet.TypeSpec

/**
 * Description: Top-level writer that handles writing all [DatabaseDefinition]
 * and [com.raizlabs.android.dbflow.annotation.TypeConverter]
 */
class DatabaseHolderDefinition(private val processorManager: ProcessorManager) : TypeDefinition {

    var className = ""

    init {

        val options = this.processorManager.processingEnvironment.options
        if (options.containsKey(OPTION_TARGET_MODULE_NAME)) {
            className = options[OPTION_TARGET_MODULE_NAME] ?: ""
        }

        className += ClassNames.DATABASE_HOLDER_STATIC_CLASS_NAME
    }

    override val typeSpec: TypeSpec = `public final class`(this.className) {
        extends(ClassNames.DATABASE_HOLDER)

        constructor {
            modifiers(public)

            processorManager.getTypeConverters().forEach { tc ->
                statement("\$L.put(\$T.class, new \$T())",
                        DatabaseHandler.TYPE_CONVERTER_MAP_FIELD_NAME, tc.modelTypeName, tc.className)

                tc.allowedSubTypes?.forEach { subType ->
                    statement("\$L.put(\$T.class, new \$T())",
                            DatabaseHandler.TYPE_CONVERTER_MAP_FIELD_NAME, subType, tc.className)
                }
            }

            processorManager.getDatabaseHolderDefinitionList()
                    .mapNotNull { it.databaseDefinition?.outputClassName }
                    .sortedBy { it.simpleName() }
                    .forEach { statement("new \$T(this)", it) }
            this
        }
    }

    /**
     * If none of the database holder databases exist, don't generate a holder.
     */
    fun isGarbage() = processorManager.getDatabaseHolderDefinitionList()
            .filter { it.databaseDefinition?.outputClassName != null }.isEmpty()

    companion object {

        @JvmField
        val OPTION_TARGET_MODULE_NAME = "targetModuleName"
    }
}