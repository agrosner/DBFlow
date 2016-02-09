package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description: Generates the Model class that is used in a many to many.
 */
public class ManyToManyDefinition extends BaseDefinition {

    TypeName referencedTable;
    public TypeName databaseTypeName;

    public ManyToManyDefinition(TypeElement element, ProcessorManager processorManager) {
        super(element, processorManager);

        ManyToMany manyToMany = element.getAnnotation(ManyToMany.class);
        referencedTable = TypeName.get(ModelUtils.getReferencedClassFromAnnotation(manyToMany));

        Table table = element.getAnnotation(Table.class);
        try {
            table.database();
        } catch (MirroredTypeException mte) {
            databaseTypeName = TypeName.get(mte.getTypeMirror());
        }

        DatabaseDefinition databaseDefinition = manager.getDatabaseWriter(databaseTypeName);
        if (databaseDefinition == null) {
            manager.logError("DatabaseDefinition was null for : " + elementName);
        } else {
            ClassName referencedOutput = getElementClassName(manager.getElements().getTypeElement(referencedTable.toString()));
            setOutputClassName(databaseDefinition.classSeparator + referencedOutput.simpleName());
        }
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {
        typeBuilder.addAnnotation(AnnotationSpec.builder(Table.class).addMember("database", "$T.class", databaseTypeName).build());

        TableDefinition referencedDefinition = manager.getTableDefinition(databaseTypeName, referencedTable);
        TableDefinition selfDefinition = manager.getTableDefinition(databaseTypeName, elementTypeName);

        typeBuilder.addField(FieldSpec.builder(TypeName.LONG, "_id")
            .addAnnotation(AnnotationSpec.builder(PrimaryKey.class).addMember("autoincrement", "true").build())
            .build());
        typeBuilder.addMethod(MethodSpec.methodBuilder("getId")
            .returns(TypeName.LONG)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addStatement("return $L", "_id")
            .build());

        List<ColumnDefinition> primaryKeys = referencedDefinition.getPrimaryColumnDefinitions();
        appendColumnDefinitions(typeBuilder, primaryKeys, referencedDefinition);
        primaryKeys = selfDefinition.getPrimaryColumnDefinitions();
        appendColumnDefinitions(typeBuilder, primaryKeys, selfDefinition);
    }

    @Override
    protected TypeName getExtendsClass() {
        return ClassNames.BASE_MODEL;
    }

    private void appendColumnDefinitions(TypeSpec.Builder typeBuilder, List<ColumnDefinition> primaryKeys, TableDefinition referencedDefinition) {
        for (ColumnDefinition primary : primaryKeys) {
            String fieldName = primary.elementName + "_" + referencedDefinition.elementName;
            typeBuilder.addField(FieldSpec.builder(primary.elementTypeName, fieldName)
                .addAnnotation(AnnotationSpec.builder(Column.class).build()).build()).build();
            typeBuilder.addMethod(MethodSpec.methodBuilder("get" + StringUtils.capitalize(fieldName))
                .returns(primary.elementTypeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return $L", fieldName)
                .build());
            typeBuilder.addMethod(MethodSpec.methodBuilder("set" + StringUtils.capitalize(fieldName))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(primary.elementTypeName, "param")
                .addStatement("$L = param", fieldName)
                .build());
        }
    }
}
