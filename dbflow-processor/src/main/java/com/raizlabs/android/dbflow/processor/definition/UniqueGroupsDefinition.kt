package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.UniqueGroup
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.CodeBlock
import java.util.*

/**
 * Description:
 */
class UniqueGroupsDefinition(uniqueGroup: UniqueGroup) {

    var columnDefinitionList: MutableList<ColumnDefinition> = ArrayList()

    var number: Int = uniqueGroup.groupNumber

    private val uniqueConflict: ConflictAction = uniqueGroup.uniqueConflict

    fun addColumnDefinition(columnDefinition: ColumnDefinition) {
        columnDefinitionList.add(columnDefinition)
    }

    val creationName: CodeBlock
        get() {
            val codeBuilder = CodeBlock.builder().add(", UNIQUE(")
            var count = 0
            for (columnDefinition in columnDefinitionList) {
                if (count > 0) {
                    codeBuilder.add(",")
                }
                if (columnDefinition is ForeignKeyColumnDefinition) {
                    for (reference in columnDefinition._foreignKeyReferenceDefinitionList) {
                        codeBuilder.add(QueryBuilder.quote(reference.columnName))
                    }
                } else {
                    codeBuilder.add(QueryBuilder.quote(columnDefinition.columnName))
                }
                count++
            }
            codeBuilder.add(") ON CONFLICT \$L", uniqueConflict)
            return codeBuilder.build()
        }
}