package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.BindToContentValuesMethod;
import com.raizlabs.android.dbflow.processor.definition.method.BindToStatementMethod;
import com.raizlabs.android.dbflow.processor.definition.method.ExistenceMethod;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.PrimaryConditionMethod;
import com.raizlabs.android.dbflow.processor.definition.method.ToModelMethod;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Description: Used in writing model container adapters
 */
public class ModelContainerDefinition extends BaseDefinition {

    public static final String DBFLOW_MODEL_CONTAINER_TAG = "Container";

    private MethodDefinition[] methods;
    private TableDefinition tableDefinition;

    public ModelContainerDefinition(TypeElement classElement, ProcessorManager manager) {
        super(classElement, manager);
        tableDefinition = manager.getTableDefinition(manager.getDatabase(elementTypeName), elementTypeName);

        setOutputClassName(tableDefinition.databaseDefinition.classSeparator + DBFLOW_MODEL_CONTAINER_TAG);

        methods = new MethodDefinition[]{
                new BindToContentValuesMethod(tableDefinition, true, true, tableDefinition.implementsContentValuesListener),
                new BindToContentValuesMethod(tableDefinition, false, true, tableDefinition.implementsContentValuesListener),
                new BindToStatementMethod(tableDefinition, true, true),
                new BindToStatementMethod(tableDefinition, false, true),
                new ExistenceMethod(tableDefinition, true),
                new PrimaryConditionMethod(tableDefinition, true),
                new ToModelMethod(tableDefinition, true),
                new LoadFromCursorMethod(tableDefinition, true, tableDefinition.implementsLoadFromCursorListener)
        };

    }

    public TypeName getDatabaseName() {
        return tableDefinition.databaseTypeName;
    }

    @Override
    protected TypeName getExtendsClass() {
        return ParameterizedTypeName.get(ClassNames.MODEL_CONTAINER_ADAPTER, elementClassName);
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {

        typeBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), ClassName.get(Class.class)), "columnMap", Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(HashMap.class)).build());

        CodeBlock.Builder constructorCode = CodeBlock.builder();

        for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitions) {
            constructorCode.addStatement("$L.put($S, $T.class)", "columnMap", columnDefinition.columnName,
                    columnDefinition.erasedTypeName);
        }
        typeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addCode(constructorCode.build())
                .addModifiers(Modifier.PUBLIC).build());

        typeBuilder.addMethod(MethodSpec.methodBuilder("getClassForColumn")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ClassName.get(String.class), "columnName")
                .addStatement("return $L.get($L)", "columnMap", "columnName")
                .returns(ClassName.get(Class.class))
                .build());

        InternalAdapterHelper.writeGetModelClass(typeBuilder, elementClassName);
        InternalAdapterHelper.writeGetTableName(typeBuilder, tableDefinition.tableName);

        for (MethodDefinition method : methods) {
            MethodSpec methodSpec = method.getMethodSpec();
            if (methodSpec != null) {
                typeBuilder.addMethod(methodSpec);
            }
        }
    }
}
