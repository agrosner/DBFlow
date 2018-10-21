package com.dbflow5.processor.definition.column

import com.dbflow5.processor.SQLiteHelper
import com.dbflow5.quote
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

/**
 * Description:
 */
object DefinitionUtils {

    fun getCreationStatement(elementTypeName: TypeName?,
                             wrapperTypeName: TypeName?,
                             columnName: String): CodeBlock.Builder {
        var statement: String? = null

        if (SQLiteHelper.containsType(wrapperTypeName ?: elementTypeName)) {
            statement = SQLiteHelper[wrapperTypeName ?: elementTypeName].toString()
        }

        return CodeBlock.builder().add("\$L \$L", columnName.quote(), statement)

    }

    fun getLoadFromCursorMethodString(elementTypeName: TypeName?,
                                      wrapperTypeName: TypeName?): String {
        var method = ""
        if (SQLiteHelper.containsMethod(wrapperTypeName ?: elementTypeName)) {
            method = SQLiteHelper.getMethod(elementTypeName)
        }
        return method
    }

}
