package com.raizlabs.android.dbflow.processor.definition.column;

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
        if (columnAccess instanceof TypeConverterAccess ||
                isModelContainerAdapter) {
            finalAccessStatement = (isModelContainerAdapter ? (variableNameString + elementName) : ("ref" + fullElementName));

            TypeName typeName;
            if (columnAccess instanceof TypeConverterAccess) {
                typeName = ((TypeConverterAccess) columnAccess).typeConverterDefinition.getDbTypeName();
            } else if (columnAccess instanceof EnumColumnAccess) {
                typeName = ClassName.get(String.class);
            } else if (columnAccess instanceof BlobColumnAccess) {
                typeName = ArrayTypeName.of(TypeName.BYTE);
            } else {
                typeName = elementTypeName;
            }

            codeBuilder.addStatement("$T $L = $L", typeName,
                    finalAccessStatement, statement);
        }

        if (!elementTypeName.isPrimitive()) {
            codeBuilder.beginControlFlow("if ($L != null)", finalAccessStatement);
        }

        codeBuilder.addStatement("$L.put($S, $L)",
                BindToContentValuesMethod.PARAM_CONTENT_VALUES,
                QueryBuilder.quote(columnName), finalAccessStatement);

        if (!elementTypeName.isPrimitive()) {
            codeBuilder.nextControlFlow("else")
                    .addStatement("$L.putNull($S)", BindToContentValuesMethod.PARAM_CONTENT_VALUES, QueryBuilder.quote(columnName))
                    .endControlFlow();
        }
        return codeBuilder;
    }

    public static CodeBlock.Builder getSQLiteStatementMethod(AtomicInteger index, String elementName,
                                                             String fullElementName, TypeName elementTypeName,
                                                             boolean isModelContainerAdapter, BaseColumnAccess columnAccess,
                                                             String variableNameString) {
        String statement = columnAccess.getColumnAccessString(elementTypeName, elementName, fullElementName, variableNameString, isModelContainerAdapter);

        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        String finalAccessStatement = statement;
        if (columnAccess instanceof TypeConverterAccess
                || isModelContainerAdapter) {
            finalAccessStatement = (isModelContainerAdapter ? (variableNameString + elementName) : ("ref" + fullElementName));

            TypeName typeName;
            if (columnAccess instanceof TypeConverterAccess) {
                typeName = ((TypeConverterAccess) columnAccess).typeConverterDefinition.getDbTypeName();
            } else if (columnAccess instanceof EnumColumnAccess) {
                typeName = ClassName.get(String.class);
            } else if (columnAccess instanceof BlobColumnAccess) {
                typeName = ArrayTypeName.of(TypeName.BYTE);
            } else {
                typeName = elementTypeName;
            }

            codeBuilder.addStatement("$T $L = $L", typeName,
                    finalAccessStatement, statement);
        }

        if (!elementTypeName.isPrimitive()) {
            codeBuilder.beginControlFlow("if ($L != null)", finalAccessStatement);
        }
        codeBuilder.addStatement("$L.bind$L($L, $L)",
                BindToStatementMethod.PARAM_STATEMENT,
                columnAccess.getSqliteTypeForTypeName(elementTypeName, isModelContainerAdapter).getSQLiteStatementMethod(),
                index.intValue(), finalAccessStatement);
        if (!elementTypeName.isPrimitive()) {
            codeBuilder.nextControlFlow("else")
                    .addStatement("$L.bindNull($L)", BindToStatementMethod.PARAM_STATEMENT, index.intValue())
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

        codeBuilder.addStatement(columnAccess.setColumnAccessString(elementTypeName, elementName, fullElementName,
                isModelContainerAdapter, ModelUtils.getVariable(isModelContainerAdapter), CodeBlock.builder().add("$L.$L($L)", LoadFromCursorMethod.PARAM_CURSOR, method, indexName).build()));

        if (putDefaultValue) {
            codeBuilder.nextControlFlow("else");
            codeBuilder.addStatement("$L.putDefault($S)", ModelUtils.getVariable(true), columnName);
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
