package com.dbflow5.processor.definition.behavior

import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.utils.isOneOf
import com.dbflow5.quote
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

interface FtsBehavior {

    val manager: ProcessorManager
    val elementName: String

    fun validateColumnDefinition(columnDefinition: ColumnDefinition) {
        if (columnDefinition.type != ColumnDefinition.Type.RowId
            && columnDefinition.columnName != "rowid"
            && columnDefinition.elementTypeName.isOneOf(Int::class, Long::class)) {
            manager.logError("FTS4 Table of type $elementName can only have a single primary key named \"rowid\" of type rowid that is an Int or Long type.")
        } else if (columnDefinition.elementTypeName != ClassName.get(String::class.java)) {
            manager.logError("FTS4 Table of type $elementName must only contain String columns")
        }
    }

    fun addContentTableCode(addComma: Boolean, codeBlock: CodeBlock.Builder) {

    }
}

/**
 * Description:
 */
class FTS4Behavior(
    val contentTable: TypeName,
    private val databaseTypeName: TypeName,
    override val elementName: String,
    override val manager: ProcessorManager) : FtsBehavior {

    override fun addContentTableCode(addComma: Boolean, codeBlock: CodeBlock.Builder) {
        val contentTableDefinition = manager.getTableDefinition(databaseTypeName, contentTable)
        contentTableDefinition?.let { tableDefinition ->
            if (addComma) {
                codeBlock.add(", ")
            }
            codeBlock.add("content=${tableDefinition.associationalBehavior.name.quote()}")
        }
    }
}

class FTS3Behavior(
    override val elementName: String,
    override val manager: ProcessorManager) : FtsBehavior
