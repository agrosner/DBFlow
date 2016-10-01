package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.processor.definition.method.BindToContentValuesMethod
import com.raizlabs.android.dbflow.processor.definition.method.BindToStatementMethod
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

import java.util.concurrent.atomic.AtomicInteger

/**
 * Description:
 */
object DefinitionUtils {

    fun getContentValuesStatement(elementName: String, fullElementName: String,
                                  columnName: String, elementTypeName: TypeName?,
                                  columnAccess: BaseColumnAccess,
                                  variableNameString: String, defaultValue: String?): CodeBlock.Builder {
        val statement = columnAccess.getColumnAccessString(elementTypeName, elementName, fullElementName, variableNameString, false)

        val codeBuilder = CodeBlock.builder()

        var finalAccessStatement = statement
        var isBlobRaw = false

        var finalTypeName = elementTypeName
        if (columnAccess is WrapperColumnAccess && columnAccess !is BooleanTypeColumnAccess) {
            finalAccessStatement = CodeBlock.of("ref" + fullElementName)

            if (columnAccess is TypeConverterAccess) {
                if (columnAccess.typeConverterDefinition != null) {
                    finalTypeName = columnAccess.typeConverterDefinition.dbTypeName
                }
                isBlobRaw = finalTypeName == ClassName.get(Blob::class.java)
            } else {
                if (columnAccess is EnumColumnAccess) {
                    finalTypeName = ClassName.get(String::class.java)
                } else if (columnAccess is BlobColumnAccess) {
                    finalTypeName = ArrayTypeName.of(TypeName.BYTE)
                } else {
                    finalTypeName = elementTypeName
                }
            }

            if (elementTypeName != null && !elementTypeName.isPrimitive) {
                val shortAccess = columnAccess.existingColumnAccess.getShortAccessString(elementTypeName, elementName, false)
                codeBuilder.addStatement("\$T \$L = model.\$L != null ? \$L : null", finalTypeName,
                        finalAccessStatement, shortAccess, statement)
            } else {
                codeBuilder.addStatement("\$T \$L = \$L", finalTypeName,
                        finalAccessStatement, statement)
            }
        }

        val putAccess = applyAndGetPutAccess(finalAccessStatement, isBlobRaw,
                elementTypeName, finalTypeName,
                codeBuilder, variableNameString, elementName)

        codeBuilder.addStatement("\$L.put(\$S, \$L)",
                BindToContentValuesMethod.PARAM_CONTENT_VALUES,
                QueryBuilder.quoteIfNeeded(columnName), putAccess)

        if (finalTypeName != null && !finalTypeName.isPrimitive) {
            codeBuilder.nextControlFlow("else")
            if (defaultValue != null && !defaultValue.isEmpty()) {
                codeBuilder.addStatement("\$L.put(\$S, \$L)",
                        BindToContentValuesMethod.PARAM_CONTENT_VALUES,
                        QueryBuilder.quoteIfNeeded(columnName), defaultValue)
            } else {
                codeBuilder.addStatement("\$L.putNull(\$S)",
                        BindToContentValuesMethod.PARAM_CONTENT_VALUES,
                        QueryBuilder.quoteIfNeeded(columnName))
            }
            codeBuilder.endControlFlow()
        }
        return codeBuilder
    }

    fun getSQLiteStatementMethod(index: AtomicInteger, elementName: String,
                                 fullElementName: String, elementTypeName: TypeName?,
                                 columnAccess: BaseColumnAccess,
                                 variableNameString: String,
                                 isAutoIncrement: Boolean,
                                 defaultValue: String?): CodeBlock.Builder {
        val statement = columnAccess.getColumnAccessString(elementTypeName, elementName,
                fullElementName, variableNameString, true)

        val codeBuilder = CodeBlock.builder()

        var finalAccessStatement = statement
        var isBlobRaw = false

        var finalTypeName = elementTypeName
        if (columnAccess is WrapperColumnAccess && columnAccess !is BooleanTypeColumnAccess) {
            finalAccessStatement = CodeBlock.of("ref" + fullElementName)

            if (columnAccess is TypeConverterAccess) {
                if (columnAccess.typeConverterDefinition != null) {
                    finalTypeName = columnAccess.typeConverterDefinition.dbTypeName
                }
                isBlobRaw = finalTypeName == ClassName.get(Blob::class.java)
            } else {
                if (columnAccess is EnumColumnAccess) {
                    finalTypeName = ClassName.get(String::class.java)
                } else if (columnAccess is BlobColumnAccess) {
                    finalTypeName = ArrayTypeName.of(TypeName.BYTE)
                } else {
                    finalTypeName = elementTypeName
                }
            }

            if (elementTypeName != null && !elementTypeName.isPrimitive) {
                val shortAccess = columnAccess.existingColumnAccess.getShortAccessString(elementTypeName, elementName, true)
                codeBuilder.addStatement("\$T \$L = model.\$L != null ? \$L : null", finalTypeName,
                        finalAccessStatement, shortAccess, statement)
            } else {
                codeBuilder.addStatement("\$T \$L = \$L", finalTypeName,
                        finalAccessStatement, statement)
            }
        }

        val putAccess = applyAndGetPutAccess(finalAccessStatement, isBlobRaw, elementTypeName, finalTypeName,
                codeBuilder, variableNameString, elementName)

        codeBuilder.addStatement("\$L.bind\$L(\$L, \$L)",
                BindToStatementMethod.PARAM_STATEMENT,
                columnAccess.getSqliteTypeForTypeName(elementTypeName).sqLiteStatementMethod,
                index.toInt().toString() + if (!isAutoIncrement) " + " + BindToStatementMethod.PARAM_START else "", putAccess)
        if (finalTypeName != null && !finalTypeName.isPrimitive) {
            codeBuilder.nextControlFlow("else")
            if (defaultValue != null && !defaultValue.isEmpty()) {
                codeBuilder.addStatement("\$L.bind\$L(\$L, \$L)", BindToStatementMethod.PARAM_STATEMENT,
                        columnAccess.getSqliteTypeForTypeName(elementTypeName).sqLiteStatementMethod,
                        index.toInt().toString() + if (!isAutoIncrement) " + " + BindToStatementMethod.PARAM_START else "", defaultValue)
            } else {
                codeBuilder.addStatement("\$L.bindNull(\$L)", BindToStatementMethod.PARAM_STATEMENT,
                        index.toInt().toString() + if (!isAutoIncrement) " + " + BindToStatementMethod.PARAM_START else "")
            }
            codeBuilder.endControlFlow()
        }

        return codeBuilder
    }

    private fun applyAndGetPutAccess(finalAccessStatement: CodeBlock, isBlobRaw: Boolean,
                                     elementTypeName: TypeName?, finalTypeName: TypeName?,
                                     codeBuilder: CodeBlock.Builder,
                                     variableNameString: String, elementName: String): CodeBlock {
        var putAccess = finalAccessStatement
        if (isBlobRaw) {
            putAccess = putAccess.toBuilder().add(".getBlob()").build()
        } else if (elementTypeName?.box() == TypeName.CHAR.box()) {
            // wrap char in string.
            putAccess = CodeBlock.of("new String(new char[]{").toBuilder().add(putAccess).add("})").build()
        }
        if (finalTypeName != null && !finalTypeName.isPrimitive) {
            if (isBlobRaw) {
                codeBuilder.beginControlFlow("if ((\$L != null) && (\$L != null))",
                        variableNameString + "." + elementName, putAccess)
            } else {
                codeBuilder.beginControlFlow("if (\$L != null)", putAccess)
            }
        }
        return putAccess
    }

    fun getLoadFromCursorMethod(
            index: Int, fullElementName: String, elementTypeName: TypeName?, columnName: String,
            putDefaultValue: Boolean, columnAccess: BaseColumnAccess,
            orderedCursorLookUp: Boolean, assignDefaultValuesFromCursor: Boolean, elementName: String): CodeBlock.Builder {
        val method = getLoadFromCursorMethodString(elementTypeName, columnAccess)

        val codeBuilder = CodeBlock.builder()

        val indexName: String
        if (!orderedCursorLookUp || index == -1) {
            indexName = "index" + columnName
            codeBuilder.addStatement("int \$L = \$L.getColumnIndex(\$S)", indexName, LoadFromCursorMethod.PARAM_CURSOR, columnName)
            codeBuilder.beginControlFlow("if (\$L != -1 && !\$L.isNull(\$L))", indexName, LoadFromCursorMethod.PARAM_CURSOR, indexName)
        } else {
            indexName = index.toString()
            codeBuilder.beginControlFlow("if (!\$L.isNull(\$L))", LoadFromCursorMethod.PARAM_CURSOR, indexName)
        }

        val cursorAssignment = CodeBlock.builder()
        if (elementTypeName?.box() == TypeName.BYTE.box()) {
            cursorAssignment.add("(\$T)", TypeName.BYTE)
        }
        cursorAssignment.add("\$L.\$L(\$L)", LoadFromCursorMethod.PARAM_CURSOR, method, indexName)
        if (elementTypeName?.box() == TypeName.CHAR.box()) {
            cursorAssignment.add(".charAt(0)")
        }

        codeBuilder.add(columnAccess.setColumnAccessString(elementTypeName, elementName, fullElementName,
                ModelUtils.variable, cursorAssignment.build()).toBuilder().add(";\n").build())

        if (putDefaultValue && assignDefaultValuesFromCursor) {
            codeBuilder.nextControlFlow("else")
            var baseColumnAccess = columnAccess
            if (columnAccess is WrapperColumnAccess) {
                baseColumnAccess = columnAccess.existingColumnAccess
            }
            codeBuilder.add(baseColumnAccess.setColumnAccessString(elementTypeName, elementName, fullElementName,
                    ModelUtils.variable,
                    CodeBlock.builder().add(getDefaultValueString(elementTypeName)).build()).toBuilder().add(";\n").build())
        }

        codeBuilder.endControlFlow()

        return codeBuilder
    }

    fun getUpdateAutoIncrementMethod(elementName: String, fullElementName: String,
                                     elementTypeName: TypeName?,
                                     columnAccess: BaseColumnAccess): CodeBlock.Builder {
        var method = ""
        var shouldCastUp = false
        if (SQLiteHelper.containsNumberMethod(elementTypeName?.unbox())) {
            method = elementTypeName?.unbox().toString()

            shouldCastUp = elementTypeName != null && !elementTypeName.isPrimitive
        }

        val codeBuilder = CodeBlock.builder()

        val accessBuilder = CodeBlock.builder()
        if (shouldCastUp) {
            accessBuilder.add("(\$T)", elementTypeName)
        }
        accessBuilder.add("id.\$LValue()", method)

        codeBuilder.add(columnAccess.setColumnAccessString(elementTypeName, elementName, fullElementName,
                ModelUtils.variable, accessBuilder.build()).toBuilder().add(";\n").build())

        return codeBuilder
    }

    fun getCreationStatement(elementTypeName: TypeName?,
                             columnAccess: BaseColumnAccess?,
                             columnName: String): CodeBlock.Builder {
        var statement: String? = null

        if (SQLiteHelper.containsType(elementTypeName)) {
            statement = SQLiteHelper[elementTypeName].toString()
        } else if (columnAccess is TypeConverterAccess && columnAccess.typeConverterDefinition != null) {
            statement = SQLiteHelper[columnAccess.typeConverterDefinition.dbTypeName].toString()
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

    fun getDefaultValueString(elementTypeName: TypeName?): String {
        var defaultValue = "null"
        if (elementTypeName != null && elementTypeName.isPrimitive) {
            if (elementTypeName == TypeName.BOOLEAN) {
                defaultValue = "false"
            } else if (elementTypeName == TypeName.BYTE || elementTypeName == TypeName.INT
                    || elementTypeName == TypeName.DOUBLE || elementTypeName == TypeName.FLOAT
                    || elementTypeName == TypeName.LONG || elementTypeName == TypeName.SHORT) {
                defaultValue = "0"
            } else if (elementTypeName == TypeName.CHAR) {
                defaultValue = "'\\u0000'"
            }
        }
        return defaultValue
    }
}
