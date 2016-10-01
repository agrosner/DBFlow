package com.raizlabs.android.dbflow.processor.definition.method

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Modifier

/**
 * Description:
 */
class ExistenceMethod(private val tableDefinition: BaseTableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() {
            val methodBuilder = MethodSpec.methodBuilder("exists")
                    .addAnnotation(Override::class.java)
                    .addParameter(tableDefinition.parameterClassName, ModelUtils.variable)
                    .addParameter(ClassNames.DATABASE_WRAPPER, "wrapper")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL).returns(TypeName.BOOLEAN)
            // only quick check if enabled.
            val autoincrementColumn = tableDefinition.autoIncrementColumn

            if (tableDefinition.hasAutoIncrement || tableDefinition.hasRowID) {
                val incrementBuilder = CodeBlock.builder().add("return ")
                val columnAccess = autoincrementColumn!!.getColumnAccessString(false)
                val autoElementType = autoincrementColumn.elementTypeName
                autoElementType?.let {
                    if (!it.isPrimitive) {
                        incrementBuilder.add("(\$L != null && ", columnAccess)
                    }
                    incrementBuilder.add("\$L > 0", columnAccess)
                    if (!it.isPrimitive) {
                        incrementBuilder.add(" || \$L == null)", columnAccess)
                    }
                }
                methodBuilder.addCode(incrementBuilder.build())
            }

            if (!tableDefinition.hasRowID && !tableDefinition.hasAutoIncrement ||
                    autoincrementColumn != null &&
                            !autoincrementColumn.isQuickCheckPrimaryKeyAutoIncrement) {
                if (tableDefinition.hasAutoIncrement || tableDefinition.hasRowID) {
                    methodBuilder.addCode(" && ")
                } else {
                    methodBuilder.addCode("return ")
                }
                methodBuilder.addCode("\$T.selectCountOf()\n.from(\$T.class)\n.where(getPrimaryConditionClause(\$L))\n.hasData(wrapper)",
                        ClassNames.SQLITE, tableDefinition.elementClassName, ModelUtils.variable)
            }
            methodBuilder.addCode(";\n")

            return methodBuilder.build()
        }
}
