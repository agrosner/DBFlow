package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.processor.SQLiteHelper;
import com.raizlabs.android.dbflow.processor.definition.method.BindToContentValuesMethod;
import com.raizlabs.android.dbflow.processor.definition.method.BindToStatementMethod;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 */
public class DefinitionUtils {

    public static CodeBlock.Builder getContentValuesStatement(String elementName, String fullElementName,
                                                              String columnName, TypeName elementTypeName,
                                                              boolean isModelContainerAdapter, BaseColumnAccess columnAccess,
                                                              String variableNameString) {
        String statement = columnAccess.getColumnAccessString(elementTypeName, elementName, fullElementName, variableNameString, isModelContainerAdapter);

        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        String finalAccessStatement = statement;
        boolean isBlobRaw = false;

        TypeName finalTypeName = elementTypeName;
        if (columnAccess instanceof WrapperColumnAccess
                || isModelContainerAdapter) {
            finalAccessStatement = (isModelContainerAdapter ? (variableNameString + elementName) : ("ref" + fullElementName));

            if (columnAccess instanceof TypeConverterAccess) {
                finalTypeName = ((TypeConverterAccess) columnAccess).typeConverterDefinition.getDbTypeName();
                isBlobRaw = (finalTypeName.equals(ClassName.get(Blob.class)));
            } else {
                if (columnAccess instanceof EnumColumnAccess) {
                    finalTypeName = ClassName.get(String.class);
                } else if (columnAccess instanceof BlobColumnAccess) {
                    finalTypeName = ArrayTypeName.of(TypeName.BYTE);
                } else {
                    finalTypeName = elementTypeName;
                }
            }

            if (!isModelContainerAdapter && !elementTypeName.isPrimitive()) {
                String shortAccess = ((WrapperColumnAccess) columnAccess).existingColumnAccess.getShortAccessString(elementTypeName, elementName, isModelContainerAdapter);
                codeBuilder.addStatement("$T $L = model.$L != null ? $L : null", finalTypeName,
                        finalAccessStatement, shortAccess, statement);
            } else {
                codeBuilder.addStatement("$T $L = $L", finalTypeName,
                        finalAccessStatement, statement);
            }
        }

        String putAccess = finalAccessStatement;
        if (isBlobRaw) {
            putAccess += ".getBlob()";
        } else if (elementTypeName.box().equals(TypeName.CHAR.box())) {
            // wrap char in string.
            putAccess = "new String(new char[]{" + putAccess + "})";
        }
        if (!finalTypeName.isPrimitive()) {
            if (!isModelContainerAdapter && (columnAccess instanceof EnumColumnAccess || columnAccess instanceof BlobColumnAccess)) {
                codeBuilder.beginControlFlow("if (($L != null) && ($L != null))", variableNameString + "." + elementName, finalAccessStatement);
            } else {
                codeBuilder.beginControlFlow("if ($L != null)", putAccess);
            }
        }

        codeBuilder.addStatement("$L.put($S, $L)",
                BindToContentValuesMethod.PARAM_CONTENT_VALUES,
                QueryBuilder.quote(columnName), putAccess);

        if (!finalTypeName.isPrimitive()) {
            codeBuilder.nextControlFlow("else")
                    .addStatement("$L.putNull($S)", BindToContentValuesMethod.PARAM_CONTENT_VALUES, QueryBuilder.quote(columnName))
                    .endControlFlow();
        }
        return codeBuilder;
    }

    public static CodeBlock.Builder getSQLiteStatementMethod(AtomicInteger index, String elementName,
                                                             String fullElementName, TypeName elementTypeName,
                                                             boolean isModelContainerAdapter, BaseColumnAccess columnAccess,
                                                             String variableNameString, boolean isAutoIncrement) {
        String statement = columnAccess.getColumnAccessString(elementTypeName, elementName, fullElementName, variableNameString, isModelContainerAdapter);

        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        String finalAccessStatement = statement;
        boolean isBlobRaw = false;

        TypeName finalTypeName = elementTypeName;
        if (columnAccess instanceof WrapperColumnAccess
                || isModelContainerAdapter) {
            finalAccessStatement = (isModelContainerAdapter ? (variableNameString + elementName) : ("ref" + fullElementName));

            if (columnAccess instanceof TypeConverterAccess) {
                finalTypeName = ((TypeConverterAccess) columnAccess).typeConverterDefinition.getDbTypeName();
                isBlobRaw = (finalTypeName.equals(ClassName.get(Blob.class)));
            } else {
                if (columnAccess instanceof EnumColumnAccess) {
                    finalTypeName = ClassName.get(String.class);
                } else if (columnAccess instanceof BlobColumnAccess) {
                    finalTypeName = ArrayTypeName.of(TypeName.BYTE);
                } else {
                    finalTypeName = elementTypeName;
                }
            }

            if (!isModelContainerAdapter && !elementTypeName.isPrimitive()) {
                String shortAccess = ((WrapperColumnAccess) columnAccess).existingColumnAccess.getShortAccessString(elementTypeName, elementName, isModelContainerAdapter);
                codeBuilder.addStatement("$T $L = model.$L != null ? $L : null", finalTypeName,
                        finalAccessStatement, shortAccess, statement);
            } else {
                codeBuilder.addStatement("$T $L = $L", finalTypeName,
                        finalAccessStatement, statement);
            }
        }

        String putAccess = finalAccessStatement;
        if (isBlobRaw) {
            putAccess += ".getBlob()";
        } else if (elementTypeName.box().equals(TypeName.CHAR.box())) {
            // wrap char in string.
            putAccess = "new String(new char[]{" + putAccess + "})";
        }
        if (!finalTypeName.isPrimitive()) {
            if (!isModelContainerAdapter && (columnAccess instanceof EnumColumnAccess
                    || columnAccess instanceof BlobColumnAccess
                    || isBlobRaw)) {
                codeBuilder.beginControlFlow("if (($L != null) && ($L != null))", variableNameString + "." + elementName, putAccess);
            } else {
                codeBuilder.beginControlFlow("if ($L != null)", putAccess);
            }
        }
        codeBuilder.addStatement("$L.bind$L($L, $L)",
                BindToStatementMethod.PARAM_STATEMENT,
                columnAccess.getSqliteTypeForTypeName(elementTypeName, isModelContainerAdapter).getSQLiteStatementMethod(),
                index.intValue() + (!isAutoIncrement ? (" + " + BindToStatementMethod.PARAM_START) : ""), putAccess);
        if (!finalTypeName.isPrimitive()) {
            codeBuilder.nextControlFlow("else")
                    .addStatement("$L.bindNull($L)", BindToStatementMethod.PARAM_STATEMENT, index.intValue() + (!isAutoIncrement ? (" + " + BindToStatementMethod.PARAM_START) : ""))
                    .endControlFlow();
        }

        return codeBuilder;
    }

    public static CodeBlock.Builder getLoadFromCursorMethod(String elementName, String fullElementName,
                                                            TypeName elementTypeName, String columnName,
                                                            boolean isModelContainerAdapter, boolean putDefaultValue,
                                                            BaseColumnAccess columnAccess) {
        String method = getLoadFromCursorMethodString(elementTypeName, columnAccess);

        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        String indexName = "index" + columnName;
        codeBuilder.addStatement("int $L = $L.getColumnIndex($S)", indexName, LoadFromCursorMethod.PARAM_CURSOR, columnName);
        codeBuilder.beginControlFlow("if ($L != -1 && !$L.isNull($L))", indexName, LoadFromCursorMethod.PARAM_CURSOR, indexName);

        CodeBlock.Builder cursorAssignment = CodeBlock.builder();
        if (elementTypeName.box().equals(TypeName.BYTE.box())) {
            cursorAssignment.add("($T)", TypeName.BYTE);
        }
        cursorAssignment.add("$L.$L($L)", LoadFromCursorMethod.PARAM_CURSOR, method, indexName);
        if (elementTypeName.box().equals(TypeName.CHAR.box())) {
            cursorAssignment.add(".charAt(0)");
        }

        codeBuilder.addStatement(columnAccess.setColumnAccessString(elementTypeName, elementName, fullElementName,
                isModelContainerAdapter, ModelUtils.getVariable(isModelContainerAdapter), cursorAssignment.build()));

        if (putDefaultValue) {
            codeBuilder.nextControlFlow("else");
            if (isModelContainerAdapter) {
                codeBuilder.addStatement("$L.putDefault($S)", ModelUtils.getVariable(true), columnName);
            } else {
                String defaultValue = "null";
                if (elementTypeName.isPrimitive()) {
                    if (elementTypeName.equals(TypeName.BOOLEAN)) {
                        defaultValue = "false";
                    } else if (elementTypeName.equals(TypeName.BYTE) || elementTypeName.equals(TypeName.INT)
                            || elementTypeName.equals(TypeName.DOUBLE) || elementTypeName.equals(TypeName.FLOAT)
                            || elementTypeName.equals(TypeName.LONG) || elementTypeName.equals(TypeName.SHORT)) {
                        defaultValue = "0";
                    } else if (elementTypeName.equals(TypeName.CHAR)) {
                        defaultValue = "'\\u0000'";
                    }
                }
                BaseColumnAccess baseColumnAccess = columnAccess;
                if (columnAccess instanceof WrapperColumnAccess) {
                    baseColumnAccess = ((WrapperColumnAccess) columnAccess).existingColumnAccess;
                }
                codeBuilder.addStatement(baseColumnAccess.setColumnAccessString(elementTypeName, elementName, fullElementName,
                        isModelContainerAdapter, ModelUtils.getVariable(isModelContainerAdapter),
                        CodeBlock.builder().add(defaultValue).build()));
            }
        }

        codeBuilder.endControlFlow();

        return codeBuilder;
    }

    public static CodeBlock.Builder getUpdateAutoIncrementMethod(String elementName, String fullElementName,
                                                                 TypeName elementTypeName,
                                                                 boolean isModelContainerAdapter,
                                                                 BaseColumnAccess columnAccess) {
        String method = "";
        boolean shouldCastUp = false;
        if (SQLiteHelper.containsNumberMethod(elementTypeName.unbox())) {
            method = elementTypeName.unbox().toString();

            shouldCastUp = !elementTypeName.isPrimitive();
        }

        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        CodeBlock.Builder accessBuilder = CodeBlock.builder();
        if (shouldCastUp) {
            accessBuilder.add("($T)", elementTypeName);
        }
        accessBuilder.add("id.$LValue()", method);

        codeBuilder.addStatement(columnAccess.setColumnAccessString(elementTypeName, elementName, fullElementName,
                isModelContainerAdapter, ModelUtils.getVariable(isModelContainerAdapter), accessBuilder.build()));

        return codeBuilder;
    }

    public static CodeBlock.Builder getCreationStatement(TypeName elementTypeName, BaseColumnAccess columnAccess, String columnName) {
        String statement = null;

        if (SQLiteHelper.containsType(elementTypeName)) {
            statement = SQLiteHelper.get(elementTypeName).toString();
        } else if (columnAccess instanceof TypeConverterAccess) {
            statement = SQLiteHelper.get(((TypeConverterAccess) columnAccess).typeConverterDefinition.getDbTypeName()).toString();
        }


        return CodeBlock.builder()
                .add("$L $L", QueryBuilder.quote(columnName), statement);

    }

    public static String getLoadFromCursorMethodString(TypeName elementTypeName, BaseColumnAccess columnAccess) {
        String method = "";
        if (SQLiteHelper.containsMethod(elementTypeName)) {
            method = SQLiteHelper.getMethod(elementTypeName);
        } else if (columnAccess instanceof TypeConverterAccess) {
            method = SQLiteHelper.getMethod(((TypeConverterAccess) columnAccess).typeConverterDefinition.getDbTypeName());
        } else if (columnAccess instanceof EnumColumnAccess) {
            method = SQLiteHelper.getMethod(ClassName.get(String.class));
        }
        return method;
    }
}
