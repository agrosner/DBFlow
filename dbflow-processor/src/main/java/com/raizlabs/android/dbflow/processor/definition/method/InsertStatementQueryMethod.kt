package com.raizlabs.android.dbflow.processor.definition.method

import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import javax.lang.model.element.Modifier

/**
 * Description:
 */
class InsertStatementQueryMethod(private val tableDefinition: TableDefinition, private val isInsert: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() {
            val methodBuilder = MethodSpec.methodBuilder(if (isInsert) "getInsertStatementQuery" else "getCompiledStatementQuery").addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL).returns(ClassName.get(String::class.java))

            val codeBuilder = CodeBlock.builder().add("INSERT ")
            if (!tableDefinition.insertConflictActionName.isEmpty()) {
                codeBuilder.add("OR \$L ", tableDefinition.insertConflictActionName)
            }
            codeBuilder.add("INTO ").add(QueryBuilder.quote(tableDefinition.tableName))

            val isSingleAutoincrement = tableDefinition.hasAutoIncrement() && tableDefinition.columnDefinitions.size == 1
                    && isInsert

            codeBuilder.add("(")

            val columnSize = tableDefinition.columnDefinitions.size
            var columnCount = 0
            tableDefinition.columnDefinitions.forEach {
                if (!it.isPrimaryKeyAutoIncrement && !it.isRowId || !isInsert || isSingleAutoincrement) {
                    if (columnCount > 0) codeBuilder.add(",")

                    codeBuilder.add(it.insertStatementColumnName)
                    columnCount++
                }
            }
            codeBuilder.add(")")

            codeBuilder.add(" VALUES (")

            columnCount = 0
            for (i in 0..columnSize - 1) {
                val definition = tableDefinition.columnDefinitions[i]
                if (!definition.isPrimaryKeyAutoIncrement && !definition.isRowId || !isInsert) {
                    if (columnCount > 0) {
                        codeBuilder.add(",")
                    }

                    codeBuilder.add(definition.insertStatementValuesString)
                    columnCount++
                }
            }

            if (isSingleAutoincrement) {
                codeBuilder.add("NULL")
            }

            codeBuilder.add(")")

            methodBuilder.addStatement("return \$S", codeBuilder.build().toString())

            return methodBuilder.build()
        }
}
