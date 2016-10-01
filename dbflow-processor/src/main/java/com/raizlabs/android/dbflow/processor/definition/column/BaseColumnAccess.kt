package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

/**
 * Description: Simply defines how to access a column's field.
 */
abstract class BaseColumnAccess {

    abstract fun getColumnAccessString(fieldType: TypeName?, elementName: String,
                                       fullElementName: String, variableNameString: String,
                                       isSqliteStatement: Boolean): CodeBlock

    abstract fun getShortAccessString(fieldType: TypeName?, elementName: String,
                                      isSqliteStatement: Boolean): CodeBlock

    abstract fun setColumnAccessString(fieldType: TypeName?, elementName: String,
                                       fullElementName: String,
                                       variableNameString: String,
                                       formattedAccess: CodeBlock): CodeBlock

    internal open fun getSqliteTypeForTypeName(elementTypeName: TypeName?): SQLiteHelper {
        return SQLiteHelper.get(elementTypeName)
    }
}
