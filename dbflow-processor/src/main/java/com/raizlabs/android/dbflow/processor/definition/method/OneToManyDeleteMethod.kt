package com.raizlabs.android.dbflow.processor.definition.method

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Modifier

/**
 * Description:
 */
class OneToManyDeleteMethod(private val tableDefinition: TableDefinition,
                            private val isModelContainerAdapter: Boolean,
                            private val useWrapper: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            var shouldWrite = false
            for (oneToManyDefinition in tableDefinition.oneToManyDefinitions) {
                if (oneToManyDefinition.isDelete) {
                    shouldWrite = true
                    break
                }
            }

            if (shouldWrite || !isModelContainerAdapter && tableDefinition.cachingEnabled) {

                val builder = CodeBlock.builder()
                for (oneToManyDefinition in tableDefinition.oneToManyDefinitions) {
                    oneToManyDefinition.writeDelete(builder, useWrapper)
                }

                if (!isModelContainerAdapter && tableDefinition.cachingEnabled) {
                    builder.addStatement("getModelCache().removeModel(getCachingId(\$L))", ModelUtils.getVariable())
                }

                builder.addStatement("super.delete(\$L\$L)", ModelUtils.getVariable(),
                        if (useWrapper) ", " + ModelUtils.getWrapper() else "")

                val delete = MethodSpec.methodBuilder("delete").addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addParameter(tableDefinition.elementClassName, ModelUtils.getVariable())
                        .addCode(builder.build()).returns(TypeName.VOID)
                if (useWrapper) {
                    delete.addParameter(ClassNames.DATABASE_WRAPPER, ModelUtils.getWrapper())
                }
                return delete.build()
            }
            return null
        }
}
