package com.dbflow5.processor.definition

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.UniqueGroup
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.definition.column.ReferenceColumnDefinition
import com.dbflow5.quote
import com.squareup.javapoet.CodeBlock

/**
 * Description:
 */
class UniqueGroupsDefinition(uniqueGroup: UniqueGroup) {

    val columnDefinitionList: MutableList<ColumnDefinition> = arrayListOf()
    val number: Int = uniqueGroup.groupNumber

    private val uniqueConflict: ConflictAction = uniqueGroup.uniqueConflict

    fun addColumnDefinition(columnDefinition: ColumnDefinition) {
        columnDefinitionList.add(columnDefinition)
    }

    val creationName: CodeBlock
        get() {
            val codeBuilder = CodeBlock.builder().add(", UNIQUE(")
            codeBuilder.add(columnDefinitionList.joinToString { columnDefinition ->
                if (columnDefinition is ReferenceColumnDefinition) {
                    columnDefinition.referenceDefinitionList.joinToString { it.columnName.quote() }
                } else {
                    columnDefinition.columnName.quote()
                }
            })
            codeBuilder.add(") ON CONFLICT \$L", uniqueConflict)
            return codeBuilder.build()
        }
}