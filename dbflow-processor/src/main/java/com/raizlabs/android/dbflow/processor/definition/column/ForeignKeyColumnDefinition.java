package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.BindToContentValuesMethod;
import com.raizlabs.android.dbflow.processor.definition.method.BindToStatementMethod;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description:
 */
public class ForeignKeyColumnDefinition extends ColumnDefinition {

    public final List<ForeignKeyReferenceDefinition> foreignKeyReferenceDefinitionList = new ArrayList<>();

    public final TableDefinition tableDefinition;

    public ClassName referencedTableClassName;

    public ForeignKeyAction onDelete;
    public ForeignKeyAction onUpdate;

    public boolean isStubbedRelationship;

    public boolean isModel;

    public boolean needsReferences;

    public boolean nonModelColumn;

    public boolean saveForeignKeyModel;

    public ForeignKeyColumnDefinition(ProcessorManager manager, TableDefinition tableDefinition,
                                      Element typeElement, boolean isPackagePrivate) {
        super(manager, typeElement, tableDefinition, isPackagePrivate);
        this.tableDefinition = tableDefinition;

        ForeignKey foreignKey = typeElement.getAnnotation(ForeignKey.class);
        onUpdate = foreignKey.onUpdate();
        onDelete = foreignKey.onDelete();

        isStubbedRelationship = foreignKey.stubbedRelationship();

        try {
            foreignKey.tableClass();
        } catch (MirroredTypeException mte) {
            referencedTableClassName = ProcessorUtils.fromTypeMirror(mte.getTypeMirror());
        }

        TypeElement erasedElement = manager.getElements().getTypeElement(
                manager.getTypeUtils().erasure(typeElement.asType()).toString());

        // hopefully intentionally left blank
        if (referencedTableClassName.equals(TypeName.OBJECT)) {
            if (elementTypeName instanceof ParameterizedTypeName) {
                List<TypeName> args = ((ParameterizedTypeName) elementTypeName).typeArguments;
                if (args.size() > 0) {
                    referencedTableClassName = ClassName.bestGuess(args.get(0).toString());
                }
            } else {
                if (referencedTableClassName == null || referencedTableClassName.equals(ClassName.OBJECT)) {
                    referencedTableClassName = ClassName.bestGuess(elementTypeName.toString());
                }
            }
        }

        if (referencedTableClassName == null) {
            manager.logError("Referenced was null for %1s within %1s", typeElement, elementTypeName);
        }

        isModel = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(), ClassNames.MODEL.toString(), erasedElement);
        isModel = isModel || erasedElement.getAnnotation(Table.class) != null;

        nonModelColumn = !isModel;

        saveForeignKeyModel = foreignKey.saveForeignKeyModel();

        // we need to recheck for this instance
        if (columnAccess instanceof TypeConverterAccess) {
            if (typeElement.getModifiers().contains(Modifier.PRIVATE)) {
                boolean useIs = elementTypeName.box().equals(TypeName.BOOLEAN.box())
                        && tableDefinition.useIsForPrivateBooleans;
                columnAccess = new PrivateColumnAccess(typeElement.getAnnotation(Column.class), useIs);
            } else {
                columnAccess = new SimpleColumnAccess();
            }
        }

        ForeignKeyReference[] references = foreignKey.references();
        if (references.length == 0) {
            // no references specified we will delegate references call to post-evaluation
            needsReferences = true;
        } else {
            for (ForeignKeyReference reference : references) {
                ForeignKeyReferenceDefinition referenceDefinition = new ForeignKeyReferenceDefinition(manager, elementName, reference, columnAccess, this);
                // TODO: add validation
                foreignKeyReferenceDefinitionList.add(referenceDefinition);
            }

            if (nonModelColumn && foreignKeyReferenceDefinitionList.size() == 1) {
                ForeignKeyReferenceDefinition foreignKeyReferenceDefinition = foreignKeyReferenceDefinitionList.get(0);
                columnName = foreignKeyReferenceDefinition.columnName;
            }
        }
    }

    @Override
    public void addPropertyDefinition(TypeSpec.Builder typeBuilder, TypeName tableClassName) {
        checkNeedsReferences();
        for (ForeignKeyReferenceDefinition reference : foreignKeyReferenceDefinitionList) {
            TypeName propParam;
            if (reference.columnClassName.isPrimitive() && !reference.columnClassName.equals(TypeName.BOOLEAN)) {
                propParam = ClassName.get(ClassNames.PROPERTY_PACKAGE, StringUtils.capitalize(reference.columnClassName.toString()) + "Property");
            } else {
                propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, reference.columnClassName.box());
            }
            typeBuilder.addField(FieldSpec.builder(propParam, reference.columnName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("new $T($T.class, $S)", propParam, tableClassName, reference.columnName).build());
        }
    }

    @Override
    public void addPropertyCase(MethodSpec.Builder methodBuilder) {
        checkNeedsReferences();
        for (ForeignKeyReferenceDefinition reference : foreignKeyReferenceDefinitionList) {
            methodBuilder.beginControlFlow("case $S: ", QueryBuilder.quoteIfNeeded(reference.columnName));
            methodBuilder.addStatement("return $L", reference.columnName);
            methodBuilder.endControlFlow();
        }
    }

    @Override
    public void addColumnName(CodeBlock.Builder codeBuilder) {
        checkNeedsReferences();
        for (int i = 0; i < foreignKeyReferenceDefinitionList.size(); i++) {
            ForeignKeyReferenceDefinition reference = foreignKeyReferenceDefinitionList.get(i);
            if (i > 0) {
                codeBuilder.add(",");
            }
            codeBuilder.add(reference.columnName);
        }
    }

    @Override
    public CodeBlock getInsertStatementColumnName() {
        checkNeedsReferences();
        CodeBlock.Builder builder = CodeBlock.builder();
        for (int i = 0; i < foreignKeyReferenceDefinitionList.size(); i++) {
            if (i > 0) {
                builder.add(",");
            }
            ForeignKeyReferenceDefinition referenceDefinition = foreignKeyReferenceDefinitionList.get(i);
            builder.add("$L", QueryBuilder.quote(referenceDefinition.columnName));
        }
        return builder.build();
    }

    @Override
    public CodeBlock getInsertStatementValuesString() {
        checkNeedsReferences();
        CodeBlock.Builder builder = CodeBlock.builder();
        for (int i = 0; i < foreignKeyReferenceDefinitionList.size(); i++) {
            if (i > 0) {
                builder.add(",");
            }
            builder.add("?");
        }
        return builder.build();
    }

    @Override
    public CodeBlock getCreationName() {
        checkNeedsReferences();
        CodeBlock.Builder builder = CodeBlock.builder();
        for (int i = 0; i < foreignKeyReferenceDefinitionList.size(); i++) {
            if (i > 0) {
                builder.add(" ,");
            }
            ForeignKeyReferenceDefinition referenceDefinition = foreignKeyReferenceDefinitionList.get(i);
            builder.add(referenceDefinition.getCreationStatement());
        }
        return builder.build();
    }

    @Override
    public String getPrimaryKeyName() {
        checkNeedsReferences();
        CodeBlock.Builder builder = CodeBlock.builder();
        for (int i = 0; i < foreignKeyReferenceDefinitionList.size(); i++) {
            if (i > 0) {
                builder.add(" ,");
            }
            ForeignKeyReferenceDefinition referenceDefinition = foreignKeyReferenceDefinitionList.get(i);
            builder.add(referenceDefinition.getPrimaryKeyName());
        }
        return builder.build().toString();
    }

    @Override
    public CodeBlock getContentValuesStatement(boolean isModelContainerAdapter) {
        if (nonModelColumn) {
            return super.getContentValuesStatement(isModelContainerAdapter);
        } else {
            checkNeedsReferences();
            CodeBlock.Builder builder = CodeBlock.builder();
            CodeBlock statement = columnAccess
                    .getColumnAccessString(elementTypeName, elementName, elementName,
                            ModelUtils.getVariable(), false);
            CodeBlock finalAccessStatement = getFinalAccessStatement(builder, statement);
            builder.beginControlFlow("if ($L != null)", finalAccessStatement);

            if (saveForeignKeyModel) {
                builder.addStatement("$L.save()", finalAccessStatement);
            }

            CodeBlock.Builder elseBuilder = CodeBlock.builder();
            for (ForeignKeyReferenceDefinition referenceDefinition : foreignKeyReferenceDefinitionList) {
                builder.add(referenceDefinition.getContentValuesStatement(isModelContainerAdapter));
                elseBuilder.addStatement("$L.putNull($S)", BindToContentValuesMethod.PARAM_CONTENT_VALUES, QueryBuilder.quote(referenceDefinition.columnName));
            }

            builder.nextControlFlow("else")
                    .add(elseBuilder.build())
                    .endControlFlow();
            return builder.build();
        }
    }

    @Override
    public CodeBlock getSQLiteStatementMethod(AtomicInteger index) {
        if (nonModelColumn) {
            return super.getSQLiteStatementMethod(index);
        } else {
            checkNeedsReferences();
            CodeBlock.Builder builder = CodeBlock.builder();
            CodeBlock statement = columnAccess
                    .getColumnAccessString(elementTypeName, elementName, elementName,
                            ModelUtils.getVariable(), true);
            CodeBlock finalAccessStatement = getFinalAccessStatement(builder, statement);
            builder.beginControlFlow("if ($L != null)", finalAccessStatement);

            if (saveForeignKeyModel) {
                builder.addStatement("$L.save()", finalAccessStatement);
            }

            CodeBlock.Builder elseBuilder = CodeBlock.builder();
            for (int i = 0; i < foreignKeyReferenceDefinitionList.size(); i++) {
                if (i > 0) {
                    index.incrementAndGet();
                }
                ForeignKeyReferenceDefinition referenceDefinition = foreignKeyReferenceDefinitionList.get(i);
                builder.add(referenceDefinition.getSQLiteStatementMethod(index));
                elseBuilder.addStatement("$L.bindNull($L)", BindToStatementMethod.PARAM_STATEMENT, index.intValue() + " + " + BindToStatementMethod.PARAM_START);
            }

            builder.nextControlFlow("else")
                    .add(elseBuilder.build())
                    .endControlFlow();
            return builder.build();
        }
    }

    @Override
    public CodeBlock getLoadFromCursorMethod(boolean endNonPrimitiveIf, AtomicInteger index) {
        if (nonModelColumn) {
            return super.getLoadFromCursorMethod(endNonPrimitiveIf, index);
        } else {
            checkNeedsReferences();
            CodeBlock.Builder builder = CodeBlock.builder()
                    .add("//// Only load model if references match, for efficiency\n");
            CodeBlock.Builder ifNullBuilder = CodeBlock.builder()
                    .add("if (");
            CodeBlock.Builder selectBuilder = CodeBlock.builder();

            // used for foreignkey containers only.
            String foreignKeyContainerRefName = "ref" + columnName;

            for (int i = 0; i < foreignKeyReferenceDefinitionList.size(); i++) {
                if (i > 0) {
                    index.incrementAndGet();
                }
                ForeignKeyReferenceDefinition referenceDefinition = foreignKeyReferenceDefinitionList.get(i);

                String indexName;
                if (!tableDefinition.orderedCursorLookUp || index.intValue() == -1) {
                    indexName = "index" + referenceDefinition.columnName;
                    builder.addStatement("int $L = $L.getColumnIndex($S)", indexName, LoadFromCursorMethod.PARAM_CURSOR, referenceDefinition.columnName);
                } else {
                    indexName = index.intValue() + "";
                }
                if (i > 0) {
                    ifNullBuilder.add(" && ");
                }

                if (!tableDefinition.orderedCursorLookUp || index.intValue() == -1) {
                    ifNullBuilder.add("$L != -1 && !$L.isNull($L)", indexName, LoadFromCursorMethod.PARAM_CURSOR, indexName);
                } else {
                    ifNullBuilder.add("!$L.isNull($L)", LoadFromCursorMethod.PARAM_CURSOR, indexName);
                }

                CodeBlock loadFromCursorBlock = CodeBlock.builder().add("$L.$L($L)", LoadFromCursorMethod.PARAM_CURSOR,
                        DefinitionUtils.getLoadFromCursorMethodString(referenceDefinition.columnClassName,
                                referenceDefinition.columnAccess), indexName).build();
                if (i > 0) {
                    selectBuilder.add("\n");
                }

                if (!isStubbedRelationship) {
                    ClassName generatedTableRef = ClassName.get(
                            referencedTableClassName.packageName(),
                            referencedTableClassName.simpleName()
                                    + tableDefinition.databaseDefinition.fieldRefSeparator
                                    + TableDefinition.DBFLOW_TABLE_TAG);
                    selectBuilder.add("\n.and($L.$L.eq($L))", generatedTableRef,
                            referenceDefinition.foreignColumnName, loadFromCursorBlock);
                } else {
                    selectBuilder.add(referenceDefinition
                            .getForeignKeyContainerMethod(foreignKeyContainerRefName,
                                    loadFromCursorBlock));
                }
            }
            ifNullBuilder.add(")");
            builder.beginControlFlow(ifNullBuilder.build().toString());

            CodeBlock.Builder initializer = CodeBlock.builder();


            CodeBlock selectBlock = selectBuilder.build();

            if (isStubbedRelationship) {
                builder.addStatement("$T $L = new $T()", elementTypeName,
                        foreignKeyContainerRefName, referencedTableClassName);
                builder.add(selectBlock).add("\n");
            } else {
                initializer.add("new $T().from($T.class).where()",
                        ClassNames.SELECT, referencedTableClassName)
                        .add(selectBuilder.build())
                        .add(".querySingle()");
            }

            initializer.add(foreignKeyContainerRefName);

            builder.add(columnAccess.setColumnAccessString(elementTypeName, elementName, elementName,
                    ModelUtils.getVariable(), initializer.build())
                    .toBuilder().add(";\n").build());

            if (endNonPrimitiveIf || !tableDefinition.assignDefaultValuesFromCursor) {
                builder.endControlFlow();
            }
            return builder.build();
        }
    }

    @Override
    public void appendPropertyComparisonAccessStatement(CodeBlock.Builder codeBuilder) {
        if (nonModelColumn || columnAccess instanceof TypeConverterAccess) {
            super.appendPropertyComparisonAccessStatement(codeBuilder);
        } else {
            CodeBlock origStatement = getColumnAccessString(false);
            if (isPrimaryKey) {
                CodeBlock statement;
                String variableName = "container" + elementName;
                TypeName typeName = elementTypeName;
                codeBuilder.addStatement("\n$T $L = ($T) $L", typeName, variableName, typeName, origStatement);
                codeBuilder.beginControlFlow("if ($L != null)", variableName);
                CodeBlock.Builder elseBuilder = CodeBlock.builder();
                for (ForeignKeyReferenceDefinition referenceDefinition : getForeignKeyReferenceDefinitionList()) {
                    if (isModel) {
                        statement = referenceDefinition.getPrimaryReferenceString();
                    } else {
                        statement = origStatement;
                    }
                    codeBuilder.addStatement("clause.and($T.$L.eq($L))", tableDefinition.getPropertyClassName(), referenceDefinition.columnName, statement);
                    elseBuilder.addStatement("clause.and($T.$L.eq(($T) $L))", tableDefinition.getPropertyClassName(), referenceDefinition.columnName, referenceDefinition.columnClassName, DefinitionUtils.getDefaultValueString(referenceDefinition.columnClassName));
                }
                codeBuilder.nextControlFlow("else");
                codeBuilder.add(elseBuilder.build());
                codeBuilder.endControlFlow();
            }
        }
    }

    CodeBlock getFinalAccessStatement(CodeBlock.Builder codeBuilder, CodeBlock statement) {
        CodeBlock finalAccessStatement = statement;
        if (columnAccess instanceof TypeConverterAccess) {
            finalAccessStatement = CodeBlock.of(getRefName());

            TypeName typeName;
            if (columnAccess instanceof TypeConverterAccess) {
                typeName = ((TypeConverterAccess) columnAccess).typeConverterDefinition.getDbTypeName();
            } else {
                typeName = referencedTableClassName;
            }

            codeBuilder.addStatement("$T $L = $L", typeName,
                    finalAccessStatement, statement);
        }

        return finalAccessStatement;
    }

    CodeBlock getForeignKeyReferenceAccess(CodeBlock statement) {
        if (columnAccess instanceof TypeConverterAccess) {
            return CodeBlock.of(getRefName());
        } else {
            return statement;
        }
    }

    public String getRefName() {
        return "ref" + elementName;
    }

    public List<ForeignKeyReferenceDefinition> getForeignKeyReferenceDefinitionList() {
        checkNeedsReferences();
        return foreignKeyReferenceDefinitionList;
    }

    /**
     * If {@link ForeignKey} has no {@link ForeignKeyReference}s, we use the primary key the referenced
     * table. We do this post-evaluation so all of the {@link TableDefinition} can be generated.
     */
    private void checkNeedsReferences() {
        TableDefinition referencedTableDefinition = manager.getTableDefinition(tableDefinition.databaseTypeName, referencedTableClassName);
        if (referencedTableDefinition == null) {
            manager.logError(ForeignKeyColumnDefinition.class,
                    "Could not find the referenced table definition %1s from %1s. Ensure it exists in the same" +
                            "database %1s", referencedTableClassName, tableDefinition.tableName, tableDefinition.databaseTypeName);
        } else {
            if (needsReferences) {
                List<ColumnDefinition> primaryColumns = referencedTableDefinition.getPrimaryColumnDefinitions();
                for (ColumnDefinition primaryColumn : primaryColumns) {
                    ForeignKeyReferenceDefinition foreignKeyReferenceDefinition =
                            new ForeignKeyReferenceDefinition(manager, elementName, primaryColumn,
                                    columnAccess, this, primaryColumns.size());
                    foreignKeyReferenceDefinitionList.add(foreignKeyReferenceDefinition);
                }
                if (nonModelColumn) {
                    columnName = foreignKeyReferenceDefinitionList.get(0).columnName;
                }
                needsReferences = false;
            }

            if (nonModelColumn && foreignKeyReferenceDefinitionList.size() == 1) {
                ForeignKeyReferenceDefinition foreignKeyReferenceDefinition = foreignKeyReferenceDefinitionList.get(0);
                columnName = foreignKeyReferenceDefinition.columnName;
            }
        }
    }
}
