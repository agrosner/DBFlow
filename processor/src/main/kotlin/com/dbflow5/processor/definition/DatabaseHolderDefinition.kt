package com.dbflow5.processor.definition

import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.rawTypeName
import com.grosner.kpoet.`public final class`
import com.grosner.kpoet.constructor
import com.grosner.kpoet.extends
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.public
import com.grosner.kpoet.statement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import kotlin.jvm.internal.Reflection

/**
 * Description: Top-level writer that handles writing all [DatabaseDefinition]
 * and [com.dbflow5.annotation.TypeConverter]
 */
class DatabaseHolderDefinition(private val processorManager: ProcessorManager) : TypeDefinition {

    val className: String

    init {

        var _className = ""
        val options = this.processorManager.processingEnvironment.options
        if (options.containsKey(OPTION_TARGET_MODULE_NAME)) {
            _className = options[OPTION_TARGET_MODULE_NAME] ?: ""
        }

        _className += ClassNames.DATABASE_HOLDER_STATIC_CLASS_NAME

        className = _className
    }

    override val typeSpec: TypeSpec = `public final class`(this.className) {
        extends(ClassNames.DATABASE_HOLDER)

        constructor {
            modifiers(public)

            processorManager.getTypeConverters().forEach { tc ->
                statement(
                    "putTypeConverter(\$T.getKotlinClass(\$T.class), new \$T())",
                    ClassNames.JVM_CLASS_MAPPING,
                    tc.modelTypeName?.rawTypeName(),
                    tc.className
                )

                tc.allowedSubTypes.forEach { subType ->
                    statement(
                        "putTypeConverter(\$T.getKotlinClass(\$T.class), new \$T())",
                        ClassNames.JVM_CLASS_MAPPING,
                        subType.rawTypeName(),
                        tc.className
                    )
                }
            }

            processorManager.getDatabaseHolderDefinitionList()
                .asSequence()
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

        const val OPTION_TARGET_MODULE_NAME = "targetModuleName"
    }
}