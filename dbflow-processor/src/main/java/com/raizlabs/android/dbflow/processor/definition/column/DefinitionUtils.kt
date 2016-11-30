package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.sql.QueryBuilder
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
            statement = SQLiteHelper[elementTypeName].toString()
        }

        return CodeBlock.builder().add("\$L \$L", QueryBuilder.quote(columnName), statement)

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
