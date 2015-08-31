package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ContainerKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.SQLiteType;
import com.raizlabs.android.dbflow.processor.definition.BaseDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Author: andrewgrosner
 * Description:
 */
public class ColumnDefinition extends BaseDefinition {

    public String columnName;

    public String columnFieldName;

    public String containerKeyName;

    public boolean hasTypeConverter = false;
    public boolean isPrimaryKey = false;
    public boolean isPrimaryKeyAutoIncrement = false;

    public Column column;
    public int length = -1;
    public boolean notNull = false;
    public ConflictAction onNullConflict;
    public ConflictAction onUniqueConflict;
    public boolean unique = false;

    public List<Integer> uniqueGroups = new ArrayList<>();

    public Collate collate = Collate.NONE;
    public String defaultValue;

    public boolean isBoolean = false;

    public BaseColumnAccess columnAccess;

    public ColumnDefinition(ProcessorManager processorManager, Element element) {
        super(element, processorManager);

        column = element.getAnnotation(Column.class);
        if (column != null) {
            this.columnName = column.name().equals("") ? element.getSimpleName()
                    .toString() : column.name();
            length = column.length();
            collate = column.collate();
            defaultValue = column.defaultValue();
        } else {
            this.columnName = element.getSimpleName()
                    .toString();
        }

        boolean isPrivate = element.getModifiers()
                .contains(Modifier.PRIVATE);
        if (isPrivate) {
            columnAccess = new PrivateColumnAccess(column);
        } else {
            columnAccess = new SimpleColumnAccess();
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

        ContainerKey containerKey = element.getAnnotation(ContainerKey.class);
        if (containerKey != null) {
            containerKeyName = containerKey.value();
        } else {
            containerKeyName = elementName;
        }

        TypeElement typeElement = manager.getElements().getTypeElement(element.asType().toString());
        if (typeElement != null && typeElement.getKind() == ElementKind.ENUM) {
            columnAccess = new EnumColumnAccess(this);
        } else if (elementTypeName.equals(ClassName.get(Blob.class))) {
            columnAccess = new BlobColumnAccess(this);
        } else {
            if (elementTypeName instanceof ParameterizedTypeName) {
                List<TypeName> args = ((ParameterizedTypeName) elementTypeName).typeArguments;
                if (!args.isEmpty()) {
                    columnAccess = new ModelContainerAccess(manager, this);
                }
            } else if (elementTypeName instanceof ArrayTypeName) {
                processorManager.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR, "Columns cannot be of array type.");
            } else {
                // Any annotated members, otherwise we will use the scanner to find other ones
                final TypeConverterDefinition typeConverterDefinition = processorManager.getTypeConverterDefinition(elementTypeName);
                if (typeConverterDefinition != null
                        || (!hasTypeConverter && !SQLiteType.containsType(elementTypeName))) {
                    hasTypeConverter = true;
                    columnAccess = new TypeConverterAccess(manager, this);
                }
            }
        }

        if (elementTypeName.box().equals(TypeName.BOOLEAN.box())) {
            isBoolean = true;
            columnAccess = new BooleanColumnAccess(manager, this);
        }

        // TODO: add Index annotation

        // TODO: consider dropping model container fields


    }

    @Override
    protected ClassName getElementClassName(Element element) {
        return null;
    }

    public void addPropertyDefinition(TypeSpec.Builder typeBuilder) {
        ParameterizedTypeName propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, elementTypeName.isPrimitive() ? elementTypeName.box() : elementTypeName);
        typeBuilder.addField(FieldSpec.builder(propParam,
                columnName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T<>($S)", ClassNames.PROPERTY, columnName).build());
    }

    public void addPropertyCase(MethodSpec.Builder methodBuilder) {
        methodBuilder.beginControlFlow("case $S: ", columnName);
        methodBuilder.addStatement("return $L", columnName);
        methodBuilder.endControlFlow();
    }

    public CodeBlock getInsertStatementColumnName() {
        return CodeBlock.builder()
                .add("$L", QueryBuilder.quote(columnName))
                .build();
    }

    public CodeBlock getInsertStatementValuesString() {
        return CodeBlock.builder()
                .add("?")
                .build();
    }

    public CodeBlock getContentValuesStatement(boolean isModelContainerAdapter) {
        return DefinitionUtils.getContentValuesStatement(containerKeyName, elementName,
                columnName, elementTypeName, isModelContainerAdapter, columnAccess, ModelUtils.getVariable(isModelContainerAdapter)).build();
    }

    public CodeBlock getSQLiteStatementMethod(AtomicInteger index, boolean isModelContainerAdapter) {
        return DefinitionUtils.getSQLiteStatementMethod(index, containerKeyName, elementName,
                elementTypeName, isModelContainerAdapter, columnAccess, ModelUtils.getVariable(isModelContainerAdapter)).build();
    }

    public CodeBlock getLoadFromCursorMethod(boolean isModelContainerAdapter) {
        return DefinitionUtils.getLoadFromCursorMethod(containerKeyName, elementName,
                elementTypeName, columnName, isModelContainerAdapter, columnAccess).build();
    }

    public CodeBlock getToModelMethod(boolean isModelContainerAdapter) {
        // TODO: get this working.
        return CodeBlock.builder().build();
    }

    public String getColumnAccessString(boolean isModelContainerAdapter) {
        return columnAccess.getColumnAccessString(elementTypeName, containerKeyName, elementName, ModelUtils.getVariable(isModelContainerAdapter), isModelContainerAdapter);
    }

    public String getReferenceColumnName(ForeignKeyReference reference) {
        return (columnName + "_" + reference.columnName()).toUpperCase();
    }

    public CodeBlock getCreationName() {
        CodeBlock.Builder codeBlockBuilder = DefinitionUtils.getCreationStatement(elementTypeName, columnAccess, columnName);

        if (length > -1) {
            codeBlockBuilder.add("($L)", length);
        }

        if (!collate.equals(Collate.NONE)) {
            codeBlockBuilder.add(" COLLATE $L", collate);
        }

        if (unique) {
            codeBlockBuilder.add(" UNIQUE");
        }

        return codeBlockBuilder.build();
    }

    /*public void writeLoadFromCursorDefinition(BaseTableDefinition tableDefinition, JavaWriter javaWriter,
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

*/
}
