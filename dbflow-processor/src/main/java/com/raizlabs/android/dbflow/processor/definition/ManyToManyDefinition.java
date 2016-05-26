package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.processor.ClassNames;
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

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description: Generates the Model class that is used in a many to many.
 */
public class ManyToManyDefinition extends BaseDefinition {

    TypeName referencedTable;
    public TypeName databaseTypeName;
    boolean generateAutoIncrement;
    boolean sameTableReferenced;
    String generatedTableClassName;
    boolean saveForeignKeyModels;
    String thisColumnName;
    String referencedColumnName;

    public ManyToManyDefinition(TypeElement element, ProcessorManager processorManager) {
        this(element, processorManager, element.getAnnotation(ManyToMany.class));
    }

    public ManyToManyDefinition(TypeElement element, ProcessorManager processorManager, ManyToMany manyToMany) {
        super(element, processorManager);

        referencedTable = TypeName.get(ModelUtils.getReferencedClassFromAnnotation(manyToMany));
        generateAutoIncrement = manyToMany.generateAutoIncrement();
        generatedTableClassName = manyToMany.generatedTableClassName();
        saveForeignKeyModels = manyToMany.saveForeignKeyModels();

        sameTableReferenced = (referencedTable.equals(elementTypeName));

        Table table = element.getAnnotation(Table.class);
        try {
            table.database();
        } catch (MirroredTypeException mte) {
            databaseTypeName = TypeName.get(mte.getTypeMirror());
        }

        thisColumnName = manyToMany.thisTableColumnName();
        referencedColumnName = manyToMany.referencedTableColumnName();

        if (!StringUtils.isNullOrEmpty(thisColumnName) && !StringUtils.isNullOrEmpty(referencedColumnName)
                && thisColumnName.equals(referencedColumnName)) {
            manager.logError(ManyToManyDefinition.class, "The thisTableColumnName and referenceTableColumnName" +
                    "cannot be the same");
        }
    }

    public void prepareForWrite() {
        DatabaseDefinition databaseDefinition = manager.getDatabaseHolderDefinition(databaseTypeName).getDatabaseDefinition();
        if (databaseDefinition == null) {
            manager.logError("DatabaseDefinition was null for : " + elementName);
        } else {
            if (StringUtils.isNullOrEmpty(generatedTableClassName)) {
                ClassName referencedOutput = getElementClassName(manager.getElements().getTypeElement(referencedTable.toString()));
                setOutputClassName(databaseDefinition.classSeparator + referencedOutput.simpleName());
            } else {
                setOutputClassNameFull(generatedTableClassName);
            }
        }
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {
        typeBuilder.addAnnotation(AnnotationSpec.builder(Table.class).addMember("database", "$T.class", databaseTypeName).build());

        TableDefinition referencedDefinition = manager.getTableDefinition(databaseTypeName, referencedTable);
        TableDefinition selfDefinition = manager.getTableDefinition(databaseTypeName, elementTypeName);

        if (generateAutoIncrement) {
            typeBuilder.addField(FieldSpec.builder(TypeName.LONG, "_id")
                    .addAnnotation(AnnotationSpec.builder(PrimaryKey.class).addMember("autoincrement", "true").build())
                    .build());
            typeBuilder.addMethod(MethodSpec.methodBuilder("getId")
                    .returns(TypeName.LONG)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return $L", "_id")
                    .build());
        }

        appendColumnDefinitions(typeBuilder, referencedDefinition, 0, referencedColumnName);
        appendColumnDefinitions(typeBuilder, selfDefinition, 1, thisColumnName);
    }

    @Override
    protected TypeName getExtendsClass() {
        return ClassNames.BASE_MODEL;
    }

    private void appendColumnDefinitions(TypeSpec.Builder typeBuilder,
                                         TableDefinition referencedDefinition, int index, String optionalName) {
        String fieldName = StringUtils.lower(referencedDefinition.elementName);
        if (sameTableReferenced) {
            fieldName += index;
        }
        // override with the name (if specified)
        if (!StringUtils.isNullOrEmpty(optionalName)) {
            fieldName = optionalName;
        }

        FieldSpec.Builder fieldBuilder = FieldSpec.builder(referencedDefinition.elementClassName, fieldName)
                .addAnnotation(AnnotationSpec.builder(ForeignKey.class)
                        .addMember("saveForeignKeyModel", saveForeignKeyModels + "").build());
        if (!generateAutoIncrement) {
            fieldBuilder.addAnnotation(AnnotationSpec.builder(PrimaryKey.class).build());
        }
        typeBuilder.addField(fieldBuilder.build()).build();
        typeBuilder.addMethod(MethodSpec.methodBuilder("get" + StringUtils.capitalize(fieldName))
                .returns(referencedDefinition.elementClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return $L", fieldName)
                .build());
        typeBuilder.addMethod(MethodSpec.methodBuilder("set" + StringUtils.capitalize(fieldName))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(referencedDefinition.elementClassName, "param")
                .addStatement("$L = param", fieldName)
                .build());
    }
}
