package com.dbflow5.processor.definition

import com.grosner.kpoet.typeName
import com.dbflow5.annotation.Migration
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.isNullOrEmpty
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

/**
 * Description: Used in holding data about migration files.
 */
class MigrationDefinition(processorManager: ProcessorManager, typeElement: TypeElement)
    : BaseDefinition(typeElement, processorManager) {

    var databaseName: TypeName? = null

    var version: Int = 0

    var priority = -1

    var constructorName: String? = null
        private set

    init {
        setOutputClassName("")

        val migration = typeElement.annotation<Migration>()
        if (migration == null) {
            processorManager.logError("Migration was null for:" + typeElement)
        } else {
            try {
                migration.database
            } catch (mte: MirroredTypeException) {
                databaseName = mte.typeMirror.typeName
            }

            version = migration.version
            priority = migration.priority

            val elements = typeElement.enclosedElements
            elements.forEach { element ->
                if (element is ExecutableElement && element.simpleName.toString() == "<init>") {
                    if (!constructorName.isNullOrEmpty()) {
                        manager.logError(MigrationDefinition::class, "Migrations cannot have more than one constructor. " +
                                "They can only have an Empty() or single-parameter constructor Empty(Empty.class) that specifies " +
                                "the .class of this migration class.")
                    }

                    if (element.parameters.isEmpty()) {
                        constructorName = "()"
                    } else if (element.parameters.size == 1) {
                        val params = element.parameters
                        val param = params[0]

                        val type = param.asType().typeName
                        if (type is ParameterizedTypeName && type.rawType == ClassName.get(Class::class.java)) {
                            val containedType = type.typeArguments[0]
                            constructorName = CodeBlock.of("(\$T.class)", containedType).toString()
                        } else {
                            manager.logError(MigrationDefinition::class, "Wrong parameter type found for $typeElement. Found $type but required ModelClass.class")
                        }
                    }
                }
            }
        }
    }

}
