package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ContainerKey;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.model.builder.AdapterQueryBuilder;
import com.raizlabs.android.dbflow.processor.model.builder.MockConditionQueryBuilder;
import com.raizlabs.android.dbflow.processor.model.writer.ColumnAccessModel;
import com.raizlabs.android.dbflow.processor.model.writer.ContentValueModel;
import com.raizlabs.android.dbflow.processor.model.writer.ForeignKeyContainerModel;
import com.raizlabs.android.dbflow.processor.model.writer.LoadFromCursorModel;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.tools.Diagnostic;

/**
 * Author: andrewgrosner
 * Description:
 */
public class ColumnDefinition extends BaseDefinition implements FlowWriter {

    public String columnName;

    public String columnFieldName;

    public String columnFieldType;

    public String columnFieldActualType;

    private String modelContainerType;

    public String foreignKeyTableClassName;

    public TypeElement modelType;

    public boolean hasTypeConverter = false;

    public boolean isPrimaryKey = false;

    public boolean isPrimaryKeyAutoIncrement = false;

    public boolean isForeignKey = false;

    public Column column;

    public ForeignKeyReference[] foreignKeyReferences;

    public boolean isModel;

    public int length = -1;

    /**
     * Whether this field is itself a model container
     */
    public boolean fieldIsModelContainer;

    public String containerKeyName;

    boolean saveModelForeignKey = true;

    public boolean notNull = false;

    public ConflictAction onNullConflict;

    public boolean unique = false;

    public List<Integer> uniqueGroups = new ArrayList<>();

    public ConflictAction onUniqueConflict;

    public ForeignKeyAction onDeleteConflict;

    public ForeignKeyAction onUpdateConflict;

    public String collate;

    public String defaultValue;

    public boolean isBoolean = false;

    public boolean isBlob = false;

    public boolean columnFieldIsPrimitive = false;

    public boolean isEnum = false;

    public boolean isPrivate = false;
    public String setterName;
    public String getterName;

    public ColumnDefinition(ProcessorManager processorManager, VariableElement element) {
        super(element, processorManager);

        column = element.getAnnotation(Column.class);
        isPrivate = element.getModifiers()
                .contains(Modifier.PRIVATE);
        if (isPrivate) {
            setterName = column.setterName();
            getterName = column.getterName();
        }

        PrimaryKey primaryKey = element.getAnnotation(PrimaryKey.class);
        if (primaryKey != null) {
            if (primaryKey.autoincrement()) {
                isPrimaryKeyAutoIncrement = true;
            } else {
                isPrimaryKey = true;
            }
        }

        Unique uniqueColumn = element.getAnnotation(Unique.class);
        if (uniqueColumn != null) {
            unique = uniqueColumn.unique();
            onUniqueConflict = uniqueColumn.onUniqueConflict();
            int[] groups = uniqueColumn.uniqueGroups();
            for (int group : groups) {
                uniqueGroups.add(group);
            }
        }

        NotNull notNullAnno = element.getAnnotation(NotNull.class);
        if (notNullAnno != null) {
            notNull = true;
            onNullConflict = notNullAnno.onNullConflict();
        }

        if (column != null) {
            this.columnName = column.name()
                    .equals("") ? element.getSimpleName()
                    .toString() : column.name();
            length = column.length();
            collate = column.collate()
                    .equals(Collate.NONE)
                    ? ""
                    : column.collate()
                            .name();
            defaultValue = column.defaultValue();
        } else {
            this.columnName = element.getSimpleName()
                    .toString();
        }
        this.columnFieldName = element.getSimpleName()
                .toString();
        this.columnFieldType = element.asType()
                .toString();
        this.columnFieldActualType = columnFieldType;

        ContainerKey containerKey = element.getAnnotation(ContainerKey.class);
        if (containerKey != null) {
            containerKeyName = containerKey.value();
        } else {
            containerKeyName = columnName;
        }

        this.columnFieldIsPrimitive = element.asType()
                .getKind()
                .isPrimitive();
        if (columnFieldIsPrimitive) {
            this.modelType = processorManager.getTypeUtils()
                    .boxedClass((PrimitiveType) element.asType());
        } else {
            boolean isAModelContainer = false;
            DeclaredType declaredType = null;
            if (element.asType() instanceof DeclaredType) {
                declaredType = (DeclaredType) element.asType();
                isAModelContainer = !declaredType.getTypeArguments()
                        .isEmpty();
            } else if (element.asType() instanceof ArrayType) {
                processorManager.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR, "Columns cannot be of array type.");
            }

            // TODO: not currently correctly supporting model containers as fields. Certainly is possible
            if (isAModelContainer) {
                fieldIsModelContainer = true;
                // TODO: hack for now
                modelContainerType = columnFieldType;
                this.modelType = (TypeElement) processorManager.getTypeUtils()
                        .asElement(
                                declaredType.getTypeArguments()
                                        .get(0));
                columnFieldType = modelType.asType()
                        .toString();
            } else {
                this.modelType = processorManager.getElements()
                        .getTypeElement(element.asType()
                                                .toString());
            }
        }

        ForeignKey foreignKey = element.getAnnotation(ForeignKey.class);
        if (foreignKey != null) {
            isForeignKey = true;
            foreignKeyTableClassName = ModelUtils.getClassFromAnnotation(foreignKey);
            this.saveModelForeignKey = foreignKey.saveForeignKeyModel();
            if (foreignKeyTableClassName.equals(Void.class.getName())) {
                foreignKeyTableClassName = columnFieldType;
            }
            foreignKeyReferences = foreignKey.references();
            onDeleteConflict = foreignKey.onDelete();
            onUpdateConflict = foreignKey.onUpdate();
        }

        isModel = ProcessorUtils.implementsClass(processorManager.getProcessingEnvironment(), ClassNames.MODEL, modelType);

        // Any annotated members, otherwise we will use the scanner to find other ones
        final TypeConverterDefinition typeConverterDefinition = processorManager.getTypeConverterDefinition(modelType);
        if (typeConverterDefinition != null) {
            hasTypeConverter = true;
        }

        if ("java.lang.Boolean".equals(modelType.getQualifiedName().toString())) {
            isBoolean = true;
        }

        // If type cannot be represented, we will get Type converter anyways
        if (!hasTypeConverter && !isModel) {
            hasTypeConverter = !SQLiteType.containsClass(columnFieldType);
        }

        if (columnFieldType.equals(Blob.class.getName())) {
            isBlob = true;
        }

        isEnum = (modelType.getKind() == ElementKind.ENUM);
    }

    public void addPropertyDefinition(TypeSpec.Builder typeBuilder) {
        ParameterizedTypeName propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, elementTypeName.isPrimitive() ? elementTypeName.box() : elementTypeName);
        typeBuilder.addField(FieldSpec.builder(propParam,
                columnName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T<>($S)", ClassNames.PROPERTY, columnName).build());
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        if (isModel || fieldIsModelContainer) {
            for (ForeignKeyReference reference : foreignKeyReferences) {
                writeColumnDefinition(javaWriter, getReferenceColumnName(reference), reference.columnName());
            }
        } else {
            writeColumnDefinition(javaWriter, columnName);
        }
    }

    public String getReferenceColumnName(ForeignKeyReference reference) {
        return (columnName + "_" + reference.columnName()).toUpperCase();
    }

    public void writeColumnDefinition(JavaWriter javaWriter, String columnName) throws IOException {
        writeColumnDefinition(javaWriter, columnName.toUpperCase(), columnName);
    }

    /**
     * When the field name is different from the column name (foreign key names)
     *
     * @param javaWriter The writer
     * @param fieldName  The name of the filed
     * @param columnName The column name
     * @throws IOException if write fails
     */
    protected void writeColumnDefinition(JavaWriter javaWriter, String fieldName, String columnName) throws
                                                                                                     IOException {
        javaWriter.emitField("String", fieldName,
                             Sets.newHashSet(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL),
                             "\"" + columnName + "\"");
        javaWriter.emitEmptyLine();
    }

    public void writeSaveDefinition(JavaWriter javaWriter, boolean isModelContainerAdapter, boolean isContentValues,
                                    AtomicInteger columnCount) throws IOException {
        if (isForeignKey && isModel) {
            if (fieldIsModelContainer) {
                javaWriter.emitSingleLineComment("Begin Saving Model Container To DB");
            } else {
                javaWriter.emitSingleLineComment("Begin Saving Foreign Key References From Model");
            }

            if (isModelContainerAdapter) {
                javaWriter.emitSingleLineComment("Model Container Definition");
            }

            ColumnAccessModel accessModel = new ColumnAccessModel(manager, this, isModelContainerAdapter);
            String modelDefinition = isModelContainerAdapter ? (ModelUtils.getVariable(true) + columnFieldName)
                    : ModelUtils.getModelStatement(accessModel.getReferencedColumnFieldName());
            if (isModelContainerAdapter) {
                javaWriter.emitStatement("ModelContainer %1s = %1s.getInstance(%1s.getValue(\"%1s\"), %1s.class)",
                                         modelDefinition,
                                         ModelUtils.getVariable(true), ModelUtils.getVariable(true),
                                         containerKeyName,
                                         foreignKeyTableClassName);
            } else {
                javaWriter.beginControlFlow("if (%1s != null)", modelDefinition);
            }
            if (saveModelForeignKey) {
                javaWriter.emitStatement("%1s.save()", modelDefinition);
            }
            List<AdapterQueryBuilder> elseNullPuts = new ArrayList<>();
            for (ForeignKeyReference foreignKeyReference : foreignKeyReferences) {
                ColumnAccessModel columnAccessModel = new ColumnAccessModel(this, foreignKeyReference);
                ForeignKeyContainerModel foreignKeyContainerModel = new ForeignKeyContainerModel(columnAccessModel,
                                                                                                 isContentValues);
                foreignKeyContainerModel.setModelContainerName(modelDefinition);
                foreignKeyContainerModel.setIndex(columnCount.intValue());
                foreignKeyContainerModel.setIsModelContainerDefinition(isModelContainerAdapter);
                foreignKeyContainerModel.setPutValue(foreignKeyReference.columnName());
                foreignKeyContainerModel.write(javaWriter);
                if (!isModelContainerAdapter) {
                    elseNullPuts.add(foreignKeyContainerModel.getNullStatement());
                }
                columnCount.incrementAndGet();
            }

            if (!isModelContainerAdapter) {
                javaWriter.nextControlFlow("else");
                for (AdapterQueryBuilder queryBuilder : elseNullPuts) {
                    javaWriter.emitStatement(queryBuilder.getQuery());
                }
                javaWriter.endControlFlow();
            }

        } else {
            String getType = columnFieldType;
            boolean isPrimitive = element.asType()
                    .getKind()
                    .isPrimitive();
            // Type converters can never be primitive except boolean
            if (isPrimitive) {
                getType = manager.getTypeUtils()
                        .boxedClass((PrimitiveType) element.asType())
                        .asType()
                        .toString();
            }

            ColumnAccessModel columnAccessModel = new ColumnAccessModel(manager, this, isModelContainerAdapter);
            ContentValueModel contentValueModel = new ContentValueModel(columnAccessModel, isContentValues);
            contentValueModel.setPutValue(columnName);
            contentValueModel.setIndex(columnCount.intValue());
            contentValueModel.setDatabaseTypeName(getType);
            contentValueModel.write(javaWriter);
            columnCount.incrementAndGet();
        }
    }

    /**
     * A field is nullable if it is not declared to be notNull and if it is a non-primitive type.
     * Nullable primitive types will simply load null values using the standard cursor methods and
     * receive the default value for that field type.
     *
     * @return {@code true} if the field is nullable, {@code false} otherwise.
     */
    private boolean isNullable() {
        return !columnFieldIsPrimitive && !notNull || isModel || fieldIsModelContainer;
    }

    public void writeLoadFromCursorDefinition(BaseTableDefinition tableDefinition, JavaWriter javaWriter,
                                              boolean isModelContainerAdapter) throws IOException {
        if (isForeignKey) {
            //TODO: This is wrong, should be using condition query builder
            javaWriter.emitEmptyLine();
            javaWriter.emitSingleLineComment("Begin Loading %1s Model Foreign Key", columnFieldName);

            // special case for model objects within class
            if (!fieldIsModelContainer && !isModelContainerAdapter && isModel) {

                ColumnAccessModel columnAccessModel = new ColumnAccessModel(manager, this, isModelContainerAdapter);

                for (ForeignKeyReference foreignKeyReference : foreignKeyReferences) {
                    javaWriter.emitStatement(ModelUtils.getColumnIndex(foreignKeyReference.columnName()));
                }
                ModelUtils.writeColumnIndexCheckers(javaWriter, foreignKeyReferences);
                MockConditionQueryBuilder conditionQueryBuilder = new MockConditionQueryBuilder()
                        .appendForeignKeyReferences(columnFieldType + tableDefinition.databaseWriter.classSeparator +
                                                    TableDefinition.DBFLOW_TABLE_TAG, columnName, foreignKeyReferences);

                String rawConditionStatement = String.format("new Select().from(%1s).where().%1s.querySingle()",
                                                             ModelUtils.getFieldClass(columnFieldType),
                                                             conditionQueryBuilder);

                AdapterQueryBuilder adapterQueryBuilder = new AdapterQueryBuilder().appendVariable(false);
                adapterQueryBuilder.append(".")
                        .append(columnAccessModel.getSetterReferenceColumnFieldName());
                if (!columnAccessModel.isPrivate()) {
                    adapterQueryBuilder.appendSpaceSeparated("=");
                }
                adapterQueryBuilder.append(rawConditionStatement);
                if (columnAccessModel.isPrivate()) {
                    adapterQueryBuilder.append(")");
                }
                javaWriter.emitStatement(adapterQueryBuilder.getQuery());

                javaWriter.endControlFlow();

            } else {
                if (isModelContainerAdapter) {
                    javaWriter.emitSingleLineComment("Writing for container adapter load from cursor for containers");
                } else {
                    javaWriter.emitSingleLineComment("Writing normal adapter load from cursor for containers");
                }
                for (ForeignKeyReference foreignKeyReference : foreignKeyReferences) {
                    javaWriter.emitStatement(ModelUtils.getColumnIndex(foreignKeyReference.columnName()));
                }
                ModelUtils.writeColumnIndexCheckers(javaWriter, foreignKeyReferences);

                String modelContainerName = "";
                if (isModelContainerAdapter) {
                    if (isModel) {
                        modelContainerName = ModelUtils.getVariable(isModelContainerAdapter) + columnFieldName;
                        javaWriter.emitStatement(
                                "ModelContainer %1s = %1s.getInstance(%1s.newDataInstance(), %1s.class)",
                                modelContainerName, ModelUtils.getVariable(true),
                                ModelUtils.getVariable(true),
                                foreignKeyTableClassName);
                    } else {
                        modelContainerName = ModelUtils.getVariable(isModelContainerAdapter);
                    }
                } else if (fieldIsModelContainer) {
                    AdapterQueryBuilder containerBuilder =
                            new AdapterQueryBuilder().appendVariable(isModelContainerAdapter)
                                    .append(".")
                                    .append(columnFieldName)
                                    .appendSpaceSeparated("=")
                                    .append("new ")
                                    .append(columnFieldActualType)
                                    .appendParenthesisEnclosed(ModelUtils.getFieldClass(columnFieldType));
                    javaWriter.emitStatement(containerBuilder.getQuery());
                }

                for (ForeignKeyReference foreignKeyReference : foreignKeyReferences) {
                    ColumnAccessModel columnAccessModel = new ColumnAccessModel(this, foreignKeyReference);
                    LoadFromCursorModel loadFromCursorModel = new LoadFromCursorModel(columnAccessModel);
                    loadFromCursorModel.setIsNullable(isNullable());
                    loadFromCursorModel.setModelContainerName(modelContainerName);
                    loadFromCursorModel.setIsModelContainerAdapter(isModelContainerAdapter);
                    loadFromCursorModel.write(javaWriter);

                }

                if (isModelContainerAdapter && isModel) {
                    javaWriter.emitStatement("%1s.put(\"%1s\",%1s.getData())", ModelUtils.getVariable(true),
                                             containerKeyName, modelContainerName);
                    javaWriter.nextControlFlow("else");

                    javaWriter.emitStatement("%1s.put(\"%1s\", null)", ModelUtils.getVariable(true),
                                             containerKeyName);
                }

                javaWriter.endControlFlow();

            }

            javaWriter.emitSingleLineComment("End");
            javaWriter.emitEmptyLine();

        } else {
            ColumnAccessModel columnAccessModel = new ColumnAccessModel(manager, this, isModelContainerAdapter);
            LoadFromCursorModel loadFromCursorModel = new LoadFromCursorModel(columnAccessModel);
            loadFromCursorModel.setModelContainerName(columnName);
            loadFromCursorModel.setIsModelContainerAdapter(isModelContainerAdapter);
            loadFromCursorModel.setIsNullable(isNullable());
            loadFromCursorModel.writeSingleField(javaWriter);
        }
    }

    public void writeToModelDefinition(JavaWriter javaWriter, boolean isModelContainerAdapter) throws IOException {

        if (!isModel) {
            AdapterQueryBuilder adapterQueryBuilder = new AdapterQueryBuilder("Object value");
            adapterQueryBuilder.append(columnFieldName)
                    .appendSpaceSeparated("=")
                    .appendVariable(true)
                    .append(".")
                    .appendGetValue(containerKeyName);
            javaWriter.emitStatement(adapterQueryBuilder.getQuery());
            javaWriter.beginControlFlow("if (value%1s != null) ", columnFieldName);
        }


        ColumnAccessModel columnAccessModel = new ColumnAccessModel(manager, this, isModelContainerAdapter);

        AdapterQueryBuilder queryBuilder = new AdapterQueryBuilder();
        queryBuilder.appendVariable(false)
                .append(".")
                .append(columnAccessModel.getSetterReferenceColumnFieldName());

        if (!columnAccessModel.isPrivate()) {
            queryBuilder.appendSpaceSeparated("=");
        }

        String getType = columnFieldType;
        // Type converters can never be primitive except boolean
        if (element.asType()
                .getKind()
                .isPrimitive()) {
            getType = manager.getTypeUtils()
                    .boxedClass((PrimitiveType) element.asType())
                    .asType()
                    .toString();
        }

        queryBuilder.appendCast(fieldIsModelContainer ? modelContainerType : getType);

        if (isModel) {
            queryBuilder.appendVariable(true)
                    .append(".getInstance(");
            queryBuilder.appendVariable(true)
                    .append(".")
                    .appendGetValue(containerKeyName);
            queryBuilder.append(",")
                    .append(ModelUtils.getFieldClass(columnFieldType))
                    .append(")")
                    .append(
                            ".toModel())");
        } else {
            if (columnAccessModel.isRequiresTypeConverter() && !columnAccessModel.isEnum() && !columnAccessModel.isBoolean()) {
                queryBuilder.appendTypeConverter(null, getType, true);
            }
            queryBuilder.append(String.format("value%1s)", columnFieldName));
        }

        if (columnAccessModel.isRequiresTypeConverter() && !columnAccessModel.isEnum() && !columnAccessModel.isBoolean()) {
            queryBuilder.append(")");
        }

        if (columnAccessModel.isPrivate()) {
            queryBuilder.append(")");
        }

        javaWriter.emitStatement(queryBuilder.getQuery());

        if (!isModel) {
            javaWriter.endControlFlow();
        }
    }


}
