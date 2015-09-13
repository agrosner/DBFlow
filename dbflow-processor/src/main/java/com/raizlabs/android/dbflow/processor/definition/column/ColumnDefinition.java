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
    public List<Integer> indexGroups = new ArrayList<>();

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
                    //columnAccess = new ModelContainerAccess(this);
                }
            } else if (elementTypeName instanceof ArrayTypeName) {
                processorManager.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR, "Columns cannot be of array type.");
            } else {
                if (elementTypeName.box().equals(TypeName.BOOLEAN.box())) {
                    isBoolean = true;
                    columnAccess = new BooleanColumnAccess(manager, this);
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
        }


        // TODO: add Index annotation

        // TODO: consider dropping model container fields


    }

    @Override
    protected ClassName getElementClassName(Element element) {
        return null;
    }

    public void addPropertyDefinition(TypeSpec.Builder typeBuilder, TypeName tableClass) {
        ParameterizedTypeName propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, elementTypeName.isPrimitive() ? elementTypeName.box() : elementTypeName);
        typeBuilder.addField(FieldSpec.builder(propParam,
                columnName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T<>($T.class, $S)", ClassNames.PROPERTY, tableClass, columnName).build());
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

    /**
     * @param isModelContainerAdapter
     * @return A string without any type conversion for this field.
     */
    public String getPropertyComparisonAccessStatement(boolean isModelContainerAdapter) {
        if (columnAccess instanceof TypeConverterAccess) {
            TypeConverterAccess converterAccess = ((TypeConverterAccess) columnAccess);
            TypeConverterDefinition converterDefinition = converterAccess.typeConverterDefinition;
            if (!isModelContainerAdapter) {
                return converterAccess.existingColumnAccess.getColumnAccessString(converterDefinition.getDbTypeName(), containerKeyName, elementName,
                        ModelUtils.getVariable(isModelContainerAdapter), isModelContainerAdapter);
            } else {
                return CodeBlock.builder()
                        .add("$L.getTypeConvertedPropertyValue($T.class, $S)",
                                ModelUtils.getVariable(isModelContainerAdapter),
                                converterAccess.typeConverterDefinition.getModelTypeName(),
                                containerKeyName)
                        .build().toString();
            }
        } else {
            return columnAccess.getColumnAccessString(elementTypeName, containerKeyName, elementName,
                    ModelUtils.getVariable(isModelContainerAdapter), isModelContainerAdapter);
        }
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
}
