package com.raizlabs.android.dbflow.processor.definition.method

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import javax.lang.model.element.Modifier

/**
 * Description: Creates a method that builds a clause of ConditionGroup that represents its primary keys. Useful
 * for updates or deletes.
 */
class PrimaryConditionMethod(private val tableDefinition: BaseTableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            val methodBuilder = MethodSpec.methodBuilder("getPrimaryConditionClause")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(tableDefinition.parameterClassName,
                            ModelUtils.getVariable()).returns(ClassNames.CONDITION_GROUP)
            val code = CodeBlock.builder()
            code.add("\$T clause = \$T.clause();", ClassNames.CONDITION_GROUP, ClassNames.CONDITION_GROUP)
            tableDefinition.primaryColumnDefinitions.forEach {
                val codeBuilder = CodeBlock.builder()
                it.appendPropertyComparisonAccessStatement(codeBuilder)
                code.add(codeBuilder.build())
            }
            methodBuilder.addCode(code.build())
            methodBuilder.addStatement("return clause")
            return methodBuilder.build()
        }

    companion object {

        internal val PARAM_MODEL = "model"
    }
}
