package com.raizlabs.android.dbflow.processor.definition.column

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

/**
 * Description:
 */
class EnumColumnAccess(columnDefinition: ColumnDefinition) : WrapperColumnAccess(columnDefinition) {

    override fun getColumnAccessString(fieldType: TypeName?, elementName: String, fullElementName: String, variableNameString: String, isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.builder().add("\$L.name()", existingColumnAccess.getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isSqliteStatement)).build()
    }

    override fun getShortAccessString(fieldType: TypeName?, elementName: String, isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.builder().add("\$L.name()", existingColumnAccess.getShortAccessString(fieldType, elementName, isSqliteStatement)).build()
    }

    override fun setColumnAccessString(fieldType: TypeName?, elementName: String, fullElementName: String, variableNameString: String, formattedAccess: CodeBlock): CodeBlock {
        val newFormattedAccess = CodeBlock.builder().add("\$T.valueOf(\$L)", columnDefinition.elementTypeName, formattedAccess).build()
        return existingColumnAccess.setColumnAccessString(ClassName.get(String::class.java), elementName, fullElementName, variableNameString, newFormattedAccess)
    }
}
