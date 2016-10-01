package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

/**
 * Description: Defines how to access a [Blob].
 */
class BlobColumnAccess(columnDefinition: ColumnDefinition) : WrapperColumnAccess(columnDefinition) {

    override fun getColumnAccessString(fieldType: TypeName?, elementName: String, fullElementName: String, variableNameString: String, isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.builder().add("\$L.getBlob()", existingColumnAccess.getColumnAccessString(fieldType, elementName, fullElementName,
                variableNameString, isSqliteStatement)).build()
    }

    override fun getShortAccessString(fieldType: TypeName?, elementName: String, isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.builder().add("\$L.getBlob()", existingColumnAccess.getShortAccessString(fieldType, elementName, isSqliteStatement)).build()
    }

    override fun setColumnAccessString(fieldType: TypeName?, elementName: String,
                                       fullElementName: String, variableNameString: String,
                                       formattedAccess: CodeBlock): CodeBlock {
        val newFormattedAccess = CodeBlock.builder().add("new \$T(\$L)", ClassName.get(Blob::class.java), formattedAccess).build()
        return existingColumnAccess.setColumnAccessString(ArrayTypeName.of(TypeName.BYTE), elementName,
                fullElementName, variableNameString, newFormattedAccess)
    }

    override fun getSqliteTypeForTypeName(elementTypeName: TypeName?): SQLiteHelper {
        return SQLiteHelper.BLOB
    }
}
