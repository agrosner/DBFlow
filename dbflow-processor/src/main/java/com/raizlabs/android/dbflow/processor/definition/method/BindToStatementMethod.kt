package com.raizlabs.android.dbflow.processor.definition.method

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import java.util.concurrent.atomic.AtomicInteger
import javax.lang.model.element.Modifier

/**
 * Description:
 */
class BindToStatementMethod(private val tableDefinition: TableDefinition, private val isInsert: Boolean, private val isModelContainerAdapter: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() {
            val methodBuilder = MethodSpec.methodBuilder(if (isInsert) "bindToInsertStatement" else "bindToStatement")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ClassNames.DATABASE_STATEMENT, PARAM_STATEMENT)
                    .addParameter(tableDefinition.parameterClassName,
                            ModelUtils.variable).returns(TypeName.VOID)

            // write the reference method
            if (isInsert) {
                methodBuilder.addParameter(TypeName.INT, PARAM_START)
                val realCount = AtomicInteger(1)
                tableDefinition.columnDefinitions.forEach {
                    if (!it.isPrimaryKeyAutoIncrement && !it.isRowId) {
                        methodBuilder.addCode(it.getSQLiteStatementMethod(realCount))
                        realCount.incrementAndGet()
                    }
                }

                if (tableDefinition.implementsSqlStatementListener) {
                    methodBuilder.addStatement("\$L.onBindTo\$LStatement(\$L)",
                            ModelUtils.variable, if (isInsert) "Insert" else "", PARAM_STATEMENT)
                }
            } else {
                var start = 0
                if (tableDefinition.hasAutoIncrement || tableDefinition.hasRowID) {
                    val autoIncrement = tableDefinition.autoIncrementColumn
                    autoIncrement?.let {
                        methodBuilder.addCode(it.getSQLiteStatementMethod(AtomicInteger(++start)))
                    }
                }

                methodBuilder.addStatement("bindToInsertStatement(\$L, \$L, \$L)", PARAM_STATEMENT, ModelUtils.variable, start)
                if (tableDefinition.implementsSqlStatementListener) {
                    methodBuilder.addStatement("\$L.onBindTo\$LStatement(\$L)",
                            ModelUtils.variable, if (isInsert) "Insert" else "", PARAM_STATEMENT)
                }
            }

            return methodBuilder.build()
        }

    companion object {

        val PARAM_STATEMENT = "statement"
        val PARAM_START = "start"
    }
}
