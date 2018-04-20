package com.raizlabs.dbflow5.processor.definition

import com.grosner.kpoet.*
import com.raizlabs.dbflow5.processor.ClassNames
import com.raizlabs.dbflow5.processor.ProcessorManager
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
                statement("putTypeConverterForClass(\$T.class, new \$T())",
                    tc.modelTypeName, tc.className)

                tc.allowedSubTypes?.forEach { subType ->
                    statement("putTypeConverterForClass(\$T.class, new \$T())",
                        subType, tc.className)
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
        .none { it.databaseDefinition?.outputClassName != null }

    companion object {

        @JvmField
        val OPTION_TARGET_MODULE_NAME = "targetModuleName"
    }
}