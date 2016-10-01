package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.StringUtils
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

        val migration = typeElement.getAnnotation(Migration::class.java)
        if (migration == null) {
            processorManager.logError("Migration was null for:" + typeElement)
        } else {
            try {
                migration.database
            } catch (mte: MirroredTypeException) {
                databaseName = TypeName.get(mte.typeMirror)
            }

            version = migration.version
            priority = migration.priority

            val elements = typeElement.enclosedElements
            for (element in elements) {
                if (element is ExecutableElement && element.getSimpleName().toString() == "<init>") {
                    if (!StringUtils.isNullOrEmpty(constructorName)) {
                        manager.logError(MigrationDefinition::class, "Migrations cannot have more than one constructor. " +
                                "They can only have an Empty() or single-parameter constructor Empty(Empty.class) that specifies " +
                                "the .class of this migration class.")
                    }

                    if (element.parameters.isEmpty()) {
                        constructorName = "()"
                    } else if (element.parameters.size == 1) {
                        val params = element.parameters
                        val param = params[0]

                        val type = TypeName.get(param.asType())
                        if (type is ParameterizedTypeName && type.rawType == ClassName.get(Class::class.java)) {
                            val containedType = type.typeArguments[0]
                            constructorName = CodeBlock.builder().add("(\$T.class)", containedType).build().toString()
                        } else {
                            manager.logError(MigrationDefinition::class, "Wrong parameter type found for %1s. Found %1s" + "but required ModelClass.class", typeElement, type)
                        }
                    }
                }
            }
        }
    }

}
