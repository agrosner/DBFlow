package com.raizlabs.android.dbflow.processor.definition.column

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

/**
 * Description:
 */
class BooleanTypeColumnAccess(columnDefinition: ColumnDefinition) : WrapperColumnAccess(columnDefinition) {

    override fun getColumnAccessString(fieldType: TypeName?, elementName: String, fullElementName: String, variableNameString: String, isSqliteStatement: Boolean): CodeBlock {
        val codeBuilder = CodeBlock.builder()
        codeBuilder.add(existingColumnAccess.getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isSqliteStatement))
        if (isSqliteStatement) {
            codeBuilder.add(" ? 1 : 0")
        }
        return codeBuilder.build()
    }

    override fun getShortAccessString(fieldType: TypeName?, elementName: String, isSqliteStatement: Boolean): CodeBlock {
        val codeBuilder = CodeBlock.builder()
        codeBuilder.add(existingColumnAccess.getShortAccessString(fieldType, elementName, isSqliteStatement))
        if (isSqliteStatement) {
            codeBuilder.add(" ? 1 : 0")
        }
        return codeBuilder.build()
    }

    override fun setColumnAccessString(fieldType: TypeName?, elementName: String, fullElementName: String, variableNameString: String, formattedAccess: CodeBlock): CodeBlock {
        val finalAccess: CodeBlock
        finalAccess = CodeBlock.builder().add("\$L == 1 ? true : false", formattedAccess).build()
        return CodeBlock.builder().add(existingColumnAccess.setColumnAccessString(fieldType,
                elementName, fullElementName, variableNameString, finalAccess)).build()
    }
}
