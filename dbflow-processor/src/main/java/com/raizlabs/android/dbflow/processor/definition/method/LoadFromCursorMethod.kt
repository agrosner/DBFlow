package com.raizlabs.android.dbflow.processor.definition.method

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import java.util.concurrent.atomic.AtomicInteger
import javax.lang.model.element.Modifier

/**
 * Description:
 */
class LoadFromCursorMethod(private val baseTableDefinition: BaseTableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() {
            val methodBuilder = MethodSpec.methodBuilder("loadFromCursor").addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ClassNames.CURSOR, PARAM_CURSOR)
                    .addParameter(baseTableDefinition.parameterClassName,
                            ModelUtils.variable).returns(TypeName.VOID)

            val index = AtomicInteger(0)
            baseTableDefinition.columnDefinitions.forEach {
                methodBuilder.addCode(it.getLoadFromCursorMethod(true, index))
                index.incrementAndGet()
            }

            if (baseTableDefinition is TableDefinition) {

                val codeBuilder = CodeBlock.builder()
                for (oneToMany in baseTableDefinition.oneToManyDefinitions) {
                    if (oneToMany.isLoad) oneToMany.writeLoad(codeBuilder)
                }
                methodBuilder.addCode(codeBuilder.build())
            }

            if (baseTableDefinition is TableDefinition && baseTableDefinition.implementsLoadFromCursorListener) {
                methodBuilder.addStatement("\$L.onLoadFromCursor(\$L)", ModelUtils.variable, LoadFromCursorMethod.PARAM_CURSOR)
            }

            return methodBuilder.build()
        }

    companion object {

        val PARAM_CURSOR = "cursor"
    }
}
