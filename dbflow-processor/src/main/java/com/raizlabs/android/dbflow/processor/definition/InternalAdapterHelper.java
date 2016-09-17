package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.DefinitionUtils;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * Description: Assists in writing methods for adapters
 */
public class InternalAdapterHelper {

    public static void writeGetModelClass(TypeSpec.Builder typeBuilder, final ClassName modelClassName) {
        typeBuilder.addMethod(MethodSpec.methodBuilder("getModelClass")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return $T.class", modelClassName)
                .returns(ParameterizedTypeName.get(ClassName.get(Class.class), modelClassName))
                .build());
    }

    public static void writeGetTableName(TypeSpec.Builder typeBuilder, final String tableName) {
        typeBuilder.addMethod(MethodSpec.methodBuilder("getTableName")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return $S", QueryBuilder.quote(tableName))
                .returns(ClassName.get(String.class))
                .build());
    }

    public static void writeUpdateAutoIncrement(TypeSpec.Builder typeBuilder, final TypeName modelClassName,
                                                ColumnDefinition autoIncrementDefinition) {
        typeBuilder.addMethod(MethodSpec.methodBuilder("updateAutoIncrement")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(modelClassName, ModelUtils.getVariable())
                .addParameter(ClassName.get(Number.class), "id")
                .addCode(autoIncrementDefinition.getUpdateAutoIncrementMethod()).build());
    }

    public static void writeGetCachingId(TypeSpec.Builder typeBuilder, final TypeName modelClassName,
                                         List<ColumnDefinition> primaryColumns) {
        if (primaryColumns.size() > 1) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getCachingColumnValuesFromModel")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ArrayTypeName.of(Object.class), "inValues")
                    .addParameter(modelClassName, ModelUtils.getVariable());
            for (int i = 0; i < primaryColumns.size(); i++) {
                ColumnDefinition column = primaryColumns.get(i);
                methodBuilder.addStatement("inValues[$L] = $L", i, column.getColumnAccessString(false));
            }
            methodBuilder.addStatement("return $L", "inValues")
                    .returns(ArrayTypeName.of(Object.class));
            typeBuilder.addMethod(methodBuilder.build());

            methodBuilder = MethodSpec.methodBuilder("getCachingColumnValuesFromCursor")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ArrayTypeName.of(Object.class), "inValues")
                    .addParameter(ClassNames.CURSOR, "cursor");
            for (int i = 0; i < primaryColumns.size(); i++) {
                ColumnDefinition column = primaryColumns.get(i);
                String method = DefinitionUtils.getLoadFromCursorMethodString(column.elementTypeName, column.columnAccess);
                methodBuilder.addStatement("inValues[$L] = $L.$L($L.getColumnIndex($S))", i, LoadFromCursorMethod.PARAM_CURSOR,
                        method, LoadFromCursorMethod.PARAM_CURSOR, column.columnName);
            }
            methodBuilder.addStatement("return $L", "inValues")
                    .returns(ArrayTypeName.of(Object.class));
            typeBuilder.addMethod(methodBuilder.build());
        } else {
            // single primary key
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getCachingColumnValueFromModel")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(modelClassName, ModelUtils.getVariable());
            methodBuilder.addStatement("return $L",
                    primaryColumns.get(0).getColumnAccessString(false))
                    .returns(Object.class);
            typeBuilder.addMethod(methodBuilder.build());

            methodBuilder = MethodSpec.methodBuilder("getCachingColumnValueFromCursor")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ClassNames.CURSOR, "cursor");
            ColumnDefinition column = primaryColumns.get(0);
            String method = DefinitionUtils.getLoadFromCursorMethodString(column.elementTypeName, column.columnAccess);
            methodBuilder.addStatement("return $L.$L($L.getColumnIndex($S))", LoadFromCursorMethod.PARAM_CURSOR,
                    method, LoadFromCursorMethod.PARAM_CURSOR, column.columnName)
                    .returns(Object.class);
            typeBuilder.addMethod(methodBuilder.build());

            methodBuilder = MethodSpec.methodBuilder("getCachingId")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(modelClassName, ModelUtils.getVariable())
                    .addStatement("return getCachingColumnValueFromModel($L)", ModelUtils.getVariable())
                    .returns(TypeName.OBJECT);
            typeBuilder.addMethod(methodBuilder.build());
        }
    }

}
