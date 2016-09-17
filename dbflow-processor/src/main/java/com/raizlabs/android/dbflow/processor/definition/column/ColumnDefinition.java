package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Index;
import com.raizlabs.android.dbflow.annotation.IndexGroup;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.SQLiteHelper;
import com.raizlabs.android.dbflow.processor.definition.BaseDefinition;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
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
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;

/**
 * Author: andrewgrosner
 * Description:
 */
public class ColumnDefinition extends BaseDefinition {

    public String columnName;

    public boolean hasTypeConverter;
    public boolean isPrimaryKey;
    private boolean isPrimaryKeyAutoIncrement;
    public boolean isQuickCheckPrimaryKeyAutoIncrement;
    public boolean isRowId;

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
    public boolean hasCustomConverter;
    public BaseTableDefinition tableDefinition;

    public ColumnDefinition(ProcessorManager processorManager, Element element,
                            BaseTableDefinition baseTableDefinition, boolean isPackagePrivate,
                            Column column, PrimaryKey primaryKey) {
        super(element, processorManager);
        this.tableDefinition = baseTableDefinition;
        this.column = column;
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


        if (isPackagePrivate) {
            columnAccess = PackagePrivateAccess.from(processorManager, element, baseTableDefinition.databaseDefinition.classSeparator);

            // register to ensure we only generate methods that are referenced by these columns.
            PackagePrivateAccess.putElement(((PackagePrivateAccess) columnAccess).helperClassName, columnName);
        } else {
            boolean isPrivate = element.getModifiers()
                    .contains(Modifier.PRIVATE);
            if (isPrivate) {
                boolean useIs = elementTypeName.box().equals(TypeName.BOOLEAN.box())
                        && (baseTableDefinition instanceof TableDefinition) && ((TableDefinition) baseTableDefinition).useIsForPrivateBooleans;
                columnAccess = new PrivateColumnAccess(column, useIs);
            } else {
                columnAccess = new SimpleColumnAccess();
            }
        }

        if (primaryKey != null) {
            if (primaryKey.rowID()) {
                isRowId = true;
            } else if (primaryKey.autoincrement()) {
                isPrimaryKeyAutoIncrement = true;
                isQuickCheckPrimaryKeyAutoIncrement = primaryKey.quickCheckAutoIncrement();
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

        Index index = element.getAnnotation(Index.class);
        if (index != null) {
            // empty index, we assume generic
            if (index.indexGroups().length == 0) {
                indexGroups.add(IndexGroup.GENERIC);
            } else {
                for (int group : index.indexGroups()) {
                    indexGroups.add(group);
                }
            }
        }

        ClassName typeConverterClassName = null;
        TypeElement typeConverterElement = null;
        if (column != null) {
            try {
                column.typeConverter();
            } catch (MirroredTypeException mte) {
                typeConverterElement = manager.getElements().getTypeElement(mte.getTypeMirror().toString());
                typeConverterClassName = ClassName.get(typeConverterElement);
            }
        }

        hasCustomConverter = false;
        if (typeConverterClassName != null && !typeConverterClassName.equals(ClassNames.TYPE_CONVERTER)) {
            TypeConverterDefinition typeConverterDefinition = new TypeConverterDefinition(typeConverterElement, manager);
            if (!typeConverterDefinition.getModelTypeName().equals(elementTypeName)) {
                manager.logError("The specified custom TypeConverter's Model Value %1s from %1s must match the type of the column %1s. ",
                        typeConverterDefinition.getModelTypeName(), typeConverterClassName, elementTypeName);
            } else {
                hasCustomConverter = true;
                String fieldName = baseTableDefinition.addColumnForCustomTypeConverter(this, typeConverterClassName);
                hasTypeConverter = true;
                columnAccess = new TypeConverterAccess(manager, this, typeConverterDefinition, fieldName);
            }
        }

        if (!hasCustomConverter) {
            TypeElement typeElement = manager.getElements().getTypeElement(element.asType().toString());
            if (typeElement != null && typeElement.getKind() == ElementKind.ENUM) {
                columnAccess = new EnumColumnAccess(this);
            } else if (elementTypeName.equals(ClassName.get(Blob.class))) {
                columnAccess = new BlobColumnAccess(this);
            } else {
                if (elementTypeName instanceof ParameterizedTypeName) {
                    // do nothing.
                } else if (elementTypeName instanceof ArrayTypeName) {
                    processorManager.getMessager()
                            .printMessage(Diagnostic.Kind.ERROR, "Columns cannot be of array type.");
                } else {
                    if (elementTypeName.equals(TypeName.BOOLEAN.box())) {
                        isBoolean = true;
                        columnAccess = new BooleanColumnAccess(manager, this);
                    } else if (elementTypeName.equals(TypeName.BOOLEAN)) {
                        // lower case boolean, we don't box up and down, we just check true or false.
                        columnAccess = new BooleanTypeColumnAccess(this);
                    } else {
                        // Any annotated members, otherwise we will use the scanner to find other ones
                        final TypeConverterDefinition typeConverterDefinition = processorManager.getTypeConverterDefinition(elementTypeName);
                        if (typeConverterDefinition != null || (!SQLiteHelper.containsType(elementTypeName))) {
                            hasTypeConverter = true;
                            if (typeConverterDefinition != null) {
                                String fieldName = baseTableDefinition.addColumnForTypeConverter(this, typeConverterDefinition.getClassName());
                                columnAccess = new TypeConverterAccess(manager, this, typeConverterDefinition, fieldName);
                            } else {
                                columnAccess = new TypeConverterAccess(manager, this);
                            }
                        }
                    }
                }
            }
        }
    }

    public ColumnDefinition(ProcessorManager processorManager, Element element,
                            BaseTableDefinition baseTableDefinition, boolean isPackagePrivate) {
        this(processorManager, element, baseTableDefinition, isPackagePrivate,
                element.getAnnotation(Column.class), element.getAnnotation(PrimaryKey.class));

    }

    @Override
    protected ClassName getElementClassName(Element element) {
        return null;
    }

    @Override
    public String toString() {
        return QueryBuilder.quoteIfNeeded(columnName);
    }

    public void addPropertyDefinition(TypeSpec.Builder typeBuilder, TypeName tableClass) {
        TypeName propParam;
        if (elementTypeName.isPrimitive() && !elementTypeName.equals(TypeName.BOOLEAN)) {
            propParam = ClassName.get(ClassNames.PROPERTY_PACKAGE, StringUtils.capitalize(elementTypeName.toString()) + "Property");
        } else {
            propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, elementTypeName.box());
        }
        typeBuilder.addField(FieldSpec.builder(propParam,
                columnName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T($T.class, $S)", propParam, tableClass, columnName).build());
    }

    public void addPropertyCase(MethodSpec.Builder methodBuilder) {
        methodBuilder.beginControlFlow("case $S: ", QueryBuilder.quote(columnName));
        methodBuilder.addStatement("return $L", columnName);
        methodBuilder.endControlFlow();
    }

    public void addColumnName(CodeBlock.Builder codeBuilder) {
        codeBuilder.add(columnName);
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
        return DefinitionUtils.getContentValuesStatement(elementName, elementName,
                columnName, elementTypeName, columnAccess,
                ModelUtils.getVariable(), defaultValue,
                tableDefinition.outputClassName).build();
    }

    public CodeBlock getSQLiteStatementMethod(AtomicInteger index) {
        return DefinitionUtils.getSQLiteStatementMethod(index, elementName, elementName,
                elementTypeName, columnAccess,
                ModelUtils.getVariable(), isPrimaryKeyAutoIncrement || isRowId, defaultValue).build();
    }

    public CodeBlock getLoadFromCursorMethod(boolean putNullForContainerAdapter,
                                             boolean endNonPrimitiveIf, AtomicInteger index) {
        return DefinitionUtils.getLoadFromCursorMethod(index.intValue(), elementName,
                elementTypeName, columnName, true, columnAccess,
                tableDefinition.orderedCursorLookUp, tableDefinition.assignDefaultValuesFromCursor,
                elementName).build();
    }

    /**
     * only used if {@link #isPrimaryKeyAutoIncrement} is true.
     *
     * @return The statement to use.
     */
    public CodeBlock getUpdateAutoIncrementMethod() {
        return DefinitionUtils.getUpdateAutoIncrementMethod(elementName, elementName, elementTypeName,
                columnAccess).build();
    }

    public String setColumnAccessString(CodeBlock formattedAccess, boolean toModelMethod) {
        return columnAccess.setColumnAccessString(elementTypeName, elementName, elementName,
                ModelUtils.getVariable(), formattedAccess);
    }

    public String getColumnAccessString(boolean isSqliteStatment) {
        return columnAccess.getColumnAccessString(elementTypeName, elementName,
                elementName, ModelUtils.getVariable(), isSqliteStatment);
    }

    public void appendPropertyComparisonAccessStatement(CodeBlock.Builder codeBuilder) {
        codeBuilder.add("\nclause.and($T.$L.eq(", tableDefinition.getPropertyClassName(), columnName);
        if (columnAccess instanceof TypeConverterAccess) {
            TypeConverterAccess converterAccess = ((TypeConverterAccess) columnAccess);
            TypeConverterDefinition converterDefinition = converterAccess.typeConverterDefinition;
            codeBuilder.add(converterAccess.existingColumnAccess
                    .getColumnAccessString(converterDefinition.getDbTypeName(),
                            elementName, elementName,
                            ModelUtils.getVariable(), false));
        } else {
            String columnAccessString = getColumnAccessString(false);
            int subAccessIndex = -1;
            if (columnAccess instanceof BlobColumnAccess) {
                subAccessIndex = columnAccessString.lastIndexOf(".getBlob()");
            } else if (columnAccess instanceof EnumColumnAccess) {
                subAccessIndex = columnAccessString.lastIndexOf(".name()");
            } else if (columnAccess instanceof BooleanTypeColumnAccess) {
                subAccessIndex = columnAccessString.lastIndexOf(" ? 1 : 0");
            }
            if (subAccessIndex > 0) {
                columnAccessString = columnAccessString.substring(0, subAccessIndex);
            }
            codeBuilder.add(columnAccessString);
        }
        codeBuilder.add("));");
    }

    public String getReferenceColumnName(ForeignKeyReference reference) {
        return (columnName + "_" + reference.columnName()).toUpperCase();
    }

    public CodeBlock getCreationName() {
        CodeBlock.Builder codeBlockBuilder = DefinitionUtils.getCreationStatement(elementTypeName, columnAccess, columnName);

        if (isPrimaryKeyAutoIncrement && !isRowId) {
            codeBlockBuilder.add(" PRIMARY KEY ");

            if (tableDefinition instanceof TableDefinition
                    && !StringUtils.isNullOrEmpty(((TableDefinition) tableDefinition).primaryKeyConflictActionName)) {
                codeBlockBuilder.add("ON CONFLICT $L ", ((TableDefinition) tableDefinition).primaryKeyConflictActionName);
            }

            codeBlockBuilder.add("AUTOINCREMENT");
        }

        if (length > -1) {
            codeBlockBuilder.add("($L)", length);
        }

        if (!collate.equals(Collate.NONE)) {
            codeBlockBuilder.add(" COLLATE $L", collate);
        }

        if (unique) {
            codeBlockBuilder.add(" UNIQUE ON CONFLICT $L", onUniqueConflict);
        }


        if (notNull) {
            codeBlockBuilder.add(" NOT NULL");
        }

        return codeBlockBuilder.build();
    }

    public String getPrimaryKeyName() {
        return QueryBuilder.quote(columnName);
    }

    public CodeBlock getForeignKeyContainerMethod(ClassName tableClassName) {

        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.addStatement("$L.put($T.$L, $L)", ModelUtils.getVariable(), tableClassName, columnName,
                columnAccess.getColumnAccessString(elementTypeName, elementName, elementName,
                        ModelUtils.getVariable(), false));
        return codeBuilder.build();
    }

    public boolean isPrimaryKeyAutoIncrement() {
        return isPrimaryKeyAutoIncrement;
    }
}
