package com.raizlabs.dbflow5.dbflow.processor.definition

import com.raizlabs.dbflow5.annotation.ConflictAction
import com.raizlabs.dbflow5.annotation.UniqueGroup
import com.raizlabs.dbflow5.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.dbflow5.dbflow.processor.definition.column.ReferenceColumnDefinition
import com.raizlabs.dbflow5.quote
import com.squareup.javapoet.CodeBlock

/**
 * Description:
 */
class UniqueGroupsDefinition(uniqueGroup: UniqueGroup) {

    var columnDefinitionList: MutableList<ColumnDefinition> = arrayListOf()

    var number: Int = uniqueGroup.groupNumber

    private val uniqueConflict: ConflictAction = uniqueGroup.uniqueConflict

    fun addColumnDefinition(columnDefinition: ColumnDefinition) {
        columnDefinitionList.add(columnDefinition)
    }

    val creationName: CodeBlock
        get() {
            val codeBuilder = CodeBlock.builder().add(", UNIQUE(")
            var count = 0
            columnDefinitionList.forEach {
                if (count > 0) {
                    codeBuilder.add(",")
                }
                if (it is ReferenceColumnDefinition) {
                    for (reference in it._referenceDefinitionList) {
                        codeBuilder.add(reference.columnName.quote())
                    }
                } else {
                    codeBuilder.add(it.columnName.quote())
                }
                count++
            }
            codeBuilder.add(") ON CONFLICT \$L", uniqueConflict)
            return codeBuilder.build()
        }
}