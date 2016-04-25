package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.BindToContentValuesMethod;
import com.raizlabs.android.dbflow.processor.definition.method.BindToStatementMethod;
import com.raizlabs.android.dbflow.processor.definition.method.CustomTypeConverterPropertyMethod;
import com.raizlabs.android.dbflow.processor.definition.method.ExistenceMethod;
import com.raizlabs.android.dbflow.processor.definition.method.ForeignKeyContainerMethod;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.PrimaryConditionMethod;
import com.raizlabs.android.dbflow.processor.definition.method.ToModelMethod;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Description: Used in writing model container adapters
 */
public class ModelContainerDefinition extends BaseDefinition {

    public static final String DBFLOW_MODEL_CONTAINER_TAG = "Container";

    private MethodDefinition[] methods;
    public TableDefinition tableDefinition;

    public ModelContainerDefinition(TypeElement classElement, ProcessorManager manager) {
        super(classElement, manager);
    }

    public void prepareForWrite() {
        ModelContainer containerKey = typeElement.getAnnotation(ModelContainer.class);
        if (containerKey != null) {
            tableDefinition = manager.getTableDefinition(manager.getDatabase(elementTypeName), elementTypeName);

            if (tableDefinition == null) {
                manager.logError("Could not find a table definition for " + elementClassName + " ensure" +
                    "that you have added a @Table definition for it.");
                return;
            }
            setOutputClassName(tableDefinition.databaseDefinition.classSeparator + DBFLOW_MODEL_CONTAINER_TAG);

            boolean putDefaultValue = containerKey.putDefault();

            methods = new MethodDefinition[]{
                new BindToContentValuesMethod(tableDefinition, true, true, tableDefinition.implementsContentValuesListener),
                new BindToContentValuesMethod(tableDefinition, false, true, tableDefinition.implementsContentValuesListener),
                new BindToStatementMethod(tableDefinition, true, true),
                new BindToStatementMethod(tableDefinition, false, true),
                new ExistenceMethod(tableDefinition, true),
                new PrimaryConditionMethod(tableDefinition, true),
                new ToModelMethod(tableDefinition),
                new LoadFromCursorMethod(tableDefinition, true, tableDefinition.implementsLoadFromCursorListener, putDefaultValue),
                new ForeignKeyContainerMethod(tableDefinition)
            };
        }
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

        CustomTypeConverterPropertyMethod customTypeConverterPropertyMethod = new CustomTypeConverterPropertyMethod(tableDefinition);
        customTypeConverterPropertyMethod.addToType(typeBuilder);

        CodeBlock.Builder constructorCode = CodeBlock.builder();
        constructorCode.addStatement("super(databaseDefinition)");

        for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitions) {
            constructorCode.addStatement("$L.put($S, $T.class)", "columnMap", columnDefinition.columnName,
                columnDefinition.erasedTypeName);
        }
        customTypeConverterPropertyMethod.addCode(constructorCode);

        typeBuilder.addMethod(MethodSpec.constructorBuilder()
            .addParameter(ClassNames.DATABASE_HOLDER, "holder")
            .addParameter(ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME, "databaseDefinition")
            .addCode(constructorCode.build())
            .addModifiers(Modifier.PUBLIC).build());

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
