package com.raizlabs.android.dbflow.processor.definition.method

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import javax.lang.model.element.Modifier

/**
 * Description: Overrides the save, update, and insert methods if the [com.raizlabs.android.dbflow.annotation.OneToMany.Method.SAVE] is used.
 */
class OneToManySaveMethod(private val tableDefinition: TableDefinition,
                          private val methodName: String,
                          private val useWrapper: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            if (!tableDefinition.oneToManyDefinitions.isEmpty() || tableDefinition.cachingEnabled) {
                val code = CodeBlock.builder()
                for (oneToManyDefinition in tableDefinition.oneToManyDefinitions) {
                    when (methodName) {
                        METHOD_SAVE -> oneToManyDefinition.writeSave(code, useWrapper)
                        METHOD_UPDATE -> oneToManyDefinition.writeUpdate(code, useWrapper)
                        METHOD_INSERT -> oneToManyDefinition.writeInsert(code, useWrapper)
                    }
                }

                code.addStatement("super.\$L(\$L\$L)", methodName,
                        ModelUtils.variable,
                        if (useWrapper) ", " + ModelUtils.wrapper else "")

                if (tableDefinition.cachingEnabled) {
                    code.addStatement("getModelCache().addModel(getCachingId(\$L), \$L)", ModelUtils.variable,
                            ModelUtils.variable)
                }

                val builder = MethodSpec.methodBuilder(methodName)
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addParameter(tableDefinition.elementClassName, ModelUtils.variable)
                        .addCode(code.build())
                if (useWrapper) {
                    builder.addParameter(ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)
                }

                return builder.build()
            } else {
                return null
            }
        }

    companion object {
        val METHOD_SAVE = "save"
        val METHOD_UPDATE = "update"
        val METHOD_INSERT = "insert"
    }
}
