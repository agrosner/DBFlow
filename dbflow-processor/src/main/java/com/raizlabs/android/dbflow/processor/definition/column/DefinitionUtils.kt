package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.ClassName
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
                                      columnAccess: BaseColumnAccess?): String {
        var method = ""
        if (SQLiteHelper.containsMethod(elementTypeName)) {
            method = SQLiteHelper.getMethod(elementTypeName)
        } else if (columnAccess is TypeConverterAccess && columnAccess.typeConverterDefinition != null) {
            method = SQLiteHelper.getMethod(columnAccess.typeConverterDefinition.dbTypeName)
        } else if (columnAccess is EnumColumnAccess) {
            method = SQLiteHelper.getMethod(ClassName.get(String::class.java))
        }
        return method
    }

}
