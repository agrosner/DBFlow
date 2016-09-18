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
                                                              BaseColumnAccess columnAccess,
                                                              String variableNameString, String defaultValue,
                                                              ClassName tableTableClassName) {
        CodeBlock statement = columnAccess.getColumnAccessString(elementTypeName, elementName, fullElementName, variableNameString, false);

        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        CodeBlock finalAccessStatement = statement;
        boolean isBlobRaw = false;

        TypeName finalTypeName = elementTypeName;
        if (columnAccess instanceof WrapperColumnAccess && !(columnAccess instanceof BooleanTypeColumnAccess)) {
            finalAccessStatement = CodeBlock.of("ref" + fullElementName);

            if (columnAccess instanceof TypeConverterAccess) {
                if (((TypeConverterAccess) columnAccess).typeConverterDefinition != null) {
                    finalTypeName = ((TypeConverterAccess) columnAccess).typeConverterDefinition.getDbTypeName();
                }
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

            if (!elementTypeName.isPrimitive()) {
                CodeBlock shortAccess = ((WrapperColumnAccess) columnAccess).existingColumnAccess.getShortAccessString(elementTypeName, elementName, false);
                codeBuilder.addStatement("$T $L = model.$L != null ? $L : null", finalTypeName,
                        finalAccessStatement, shortAccess, statement);
            } else {
                codeBuilder.addStatement("$T $L = $L", finalTypeName,
                        finalAccessStatement, statement);
            }
        }

        CodeBlock putAccess = applyAndGetPutAccess(finalAccessStatement, isBlobRaw,
                elementTypeName, finalTypeName,
                codeBuilder, variableNameString, elementName);

        codeBuilder.addStatement("$L.put($T.$L.getCursorKey(), $L)",
                BindToContentValuesMethod.PARAM_CONTENT_VALUES,
                tableTableClassName, columnName, putAccess);

        if (!finalTypeName.isPrimitive()) {
            codeBuilder.nextControlFlow("else");
            if (defaultValue != null && !defaultValue.isEmpty()) {
                codeBuilder.addStatement("$L.put($T.$L.getCursorKey(), $L)",
                        BindToContentValuesMethod.PARAM_CONTENT_VALUES,
                        tableTableClassName, columnName, defaultValue);
            } else {
                codeBuilder.addStatement("$L.putNull($T.$L.getCursorKey())", BindToContentValuesMethod.PARAM_CONTENT_VALUES,
                        tableTableClassName, columnName);
            }
            codeBuilder.endControlFlow();
        }
        return codeBuilder;
    }

    public static CodeBlock.Builder getSQLiteStatementMethod(AtomicInteger index, String elementName,
                                                             String fullElementName, TypeName elementTypeName,
                                                             BaseColumnAccess columnAccess,
                                                             String variableNameString,
                                                             boolean isAutoIncrement,
                                                             String defaultValue) {
        CodeBlock statement = columnAccess.getColumnAccessString(elementTypeName, elementName,
                fullElementName, variableNameString, true);

        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        CodeBlock finalAccessStatement = statement;
        boolean isBlobRaw = false;

        TypeName finalTypeName = elementTypeName;
        if (columnAccess instanceof WrapperColumnAccess && !(columnAccess instanceof BooleanTypeColumnAccess)) {
            finalAccessStatement = CodeBlock.of("ref" + fullElementName);

            if (columnAccess instanceof TypeConverterAccess) {
                if (((TypeConverterAccess) columnAccess).typeConverterDefinition != null) {
                    finalTypeName = ((TypeConverterAccess) columnAccess).typeConverterDefinition.getDbTypeName();
                }
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

            if (!elementTypeName.isPrimitive()) {
                CodeBlock shortAccess = ((WrapperColumnAccess) columnAccess).existingColumnAccess.getShortAccessString(elementTypeName, elementName, true);
                codeBuilder.addStatement("$T $L = model.$L != null ? $L : null", finalTypeName,
                        finalAccessStatement, shortAccess, statement);
            } else {
                codeBuilder.addStatement("$T $L = $L", finalTypeName,
                        finalAccessStatement, statement);
            }
        }

        CodeBlock putAccess = applyAndGetPutAccess(finalAccessStatement, isBlobRaw, elementTypeName, finalTypeName,
                codeBuilder, variableNameString, elementName);

        codeBuilder.addStatement("$L.bind$L($L, $L)",
                BindToStatementMethod.PARAM_STATEMENT,
                columnAccess.getSqliteTypeForTypeName(elementTypeName).getSQLiteStatementMethod(),
                index.intValue() + (!isAutoIncrement ? (" + " + BindToStatementMethod.PARAM_START) : ""), putAccess);
        if (!finalTypeName.isPrimitive()) {
            codeBuilder.nextControlFlow("else");
            if (defaultValue != null && !defaultValue.isEmpty()) {
                codeBuilder.addStatement("$L.bind$L($L, $L)", BindToStatementMethod.PARAM_STATEMENT,
                        columnAccess.getSqliteTypeForTypeName(elementTypeName).getSQLiteStatementMethod(),
                        index.intValue() + (!isAutoIncrement ? (" + " + BindToStatementMethod.PARAM_START) : ""), defaultValue);
            } else {
                codeBuilder.addStatement("$L.bindNull($L)", BindToStatementMethod.PARAM_STATEMENT, index.intValue() + (!isAutoIncrement ? (" + " + BindToStatementMethod.PARAM_START) : ""));
            }
            codeBuilder.endControlFlow();
        }

        return codeBuilder;
    }

    private static CodeBlock applyAndGetPutAccess(CodeBlock finalAccessStatement, boolean isBlobRaw,
                                                  TypeName elementTypeName, TypeName finalTypeName,
                                                  CodeBlock.Builder codeBuilder,
                                                  String variableNameString, String elementName) {
        CodeBlock putAccess = finalAccessStatement;
        if (isBlobRaw) {
            putAccess = putAccess.toBuilder().add(".getBlob()").build();
        } else if (elementTypeName.box().equals(TypeName.CHAR.box())) {
            // wrap char in string.
            putAccess = CodeBlock.of("new String(new char[]{")
                    .toBuilder().add(putAccess)
                    .add("})").build();
        }
        if (!finalTypeName.isPrimitive()) {
            if (isBlobRaw) {
                codeBuilder.beginControlFlow("if (($L != null) && ($L != null))",
                        variableNameString + "." + elementName, putAccess);
            } else {
                codeBuilder.beginControlFlow("if ($L != null)", putAccess);
            }
        }
        return putAccess;
    }

    public static CodeBlock.Builder getLoadFromCursorMethod(
            int index, String fullElementName, TypeName elementTypeName, String columnName,
            boolean putDefaultValue, BaseColumnAccess columnAccess,
            boolean orderedCursorLookUp, boolean assignDefaultValuesFromCursor, String elementName) {
        String method = getLoadFromCursorMethodString(elementTypeName, columnAccess);

        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        String indexName;
        if (!orderedCursorLookUp || index == -1) {
            indexName = "index" + columnName;
            codeBuilder.addStatement("int $L = $L.getColumnIndex($S)", indexName, LoadFromCursorMethod.PARAM_CURSOR, columnName);
            codeBuilder.beginControlFlow("if ($L != -1 && !$L.isNull($L))", indexName, LoadFromCursorMethod.PARAM_CURSOR, indexName);
        } else {
            indexName = index + "";
            codeBuilder.beginControlFlow("if (!$L.isNull($L))", LoadFromCursorMethod.PARAM_CURSOR, indexName);
        }

        CodeBlock.Builder cursorAssignment = CodeBlock.builder();
        if (elementTypeName.box().equals(TypeName.BYTE.box())) {
            cursorAssignment.add("($T)", TypeName.BYTE);
        }
        cursorAssignment.add("$L.$L($L)", LoadFromCursorMethod.PARAM_CURSOR, method, indexName);
        if (elementTypeName.box().equals(TypeName.CHAR.box())) {
            cursorAssignment.add(".charAt(0)");
        }

        codeBuilder.add(columnAccess.setColumnAccessString(elementTypeName, elementName, fullElementName,
                ModelUtils.getVariable(), cursorAssignment.build())
                .toBuilder().add(";\n").build());

        if (putDefaultValue && assignDefaultValuesFromCursor) {
            codeBuilder.nextControlFlow("else");
            BaseColumnAccess baseColumnAccess = columnAccess;
            if (columnAccess instanceof WrapperColumnAccess) {
                baseColumnAccess = ((WrapperColumnAccess) columnAccess).existingColumnAccess;
            }
            codeBuilder.add(baseColumnAccess.setColumnAccessString(elementTypeName, elementName, fullElementName,
                    ModelUtils.getVariable(),
                    CodeBlock.builder().add(getDefaultValueString(elementTypeName)).build())
                    .toBuilder().add(";\n").build());
        }

        codeBuilder.endControlFlow();

        return codeBuilder;
    }

    public static CodeBlock.Builder getUpdateAutoIncrementMethod(String elementName, String fullElementName,
                                                                 TypeName elementTypeName,
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

        codeBuilder.add(columnAccess.setColumnAccessString(elementTypeName, elementName, fullElementName,
                ModelUtils.getVariable(), accessBuilder.build()).toBuilder()
                .add(";\n").build());

        return codeBuilder;
    }

    public static CodeBlock.Builder getCreationStatement(TypeName elementTypeName,
                                                         BaseColumnAccess columnAccess,
                                                         String columnName) {
        String statement = null;

        if (SQLiteHelper.containsType(elementTypeName)) {
            statement = SQLiteHelper.get(elementTypeName).toString();
        } else if (columnAccess instanceof TypeConverterAccess &&
                ((TypeConverterAccess) columnAccess).typeConverterDefinition != null) {
            statement = SQLiteHelper.get(((TypeConverterAccess) columnAccess)
                    .typeConverterDefinition.getDbTypeName()).toString();
        }


        return CodeBlock.builder()
                .add("$L $L", QueryBuilder.quote(columnName), statement);

    }

    public static String getLoadFromCursorMethodString(TypeName elementTypeName,
                                                       BaseColumnAccess columnAccess) {
        String method = "";
        if (SQLiteHelper.containsMethod(elementTypeName)) {
            method = SQLiteHelper.getMethod(elementTypeName);
        } else if (columnAccess instanceof TypeConverterAccess &&
                ((TypeConverterAccess) columnAccess).typeConverterDefinition != null) {
            method = SQLiteHelper.getMethod(((TypeConverterAccess) columnAccess)
                    .typeConverterDefinition.getDbTypeName());
        } else if (columnAccess instanceof EnumColumnAccess) {
            method = SQLiteHelper.getMethod(ClassName.get(String.class));
        }
        return method;
    }

    public static String getDefaultValueString(TypeName elementTypeName) {
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
        return defaultValue;
    }
}
