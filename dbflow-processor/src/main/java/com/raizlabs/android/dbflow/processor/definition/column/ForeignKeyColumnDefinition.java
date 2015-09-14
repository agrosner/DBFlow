package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.SQLiteType;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.BindToContentValuesMethod;
import com.raizlabs.android.dbflow.processor.definition.method.BindToStatementMethod;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
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

    public boolean isModelContainer;

    public boolean isModel;

    public boolean needsReferences;

    public boolean nonModelColumn;

    public ForeignKeyColumnDefinition(ProcessorManager manager, TableDefinition tableDefinition, Element typeElement) {
        super(manager, typeElement);
        this.tableDefinition = tableDefinition;

        ForeignKey foreignKey = typeElement.getAnnotation(ForeignKey.class);
        onUpdate = foreignKey.onUpdate();
        onDelete = foreignKey.onDelete();

        try {
            foreignKey.tableClass();
        } catch (MirroredTypeException mte) {
            referencedTableClassName = ClassName.get(manager.getElements().getTypeElement(mte.getTypeMirror().toString()));
        }

        // hopefully intentionally left blank
        if (referencedTableClassName.equals(TypeName.OBJECT)) {
            if (elementTypeName instanceof ParameterizedTypeName) {
                List<TypeName> args = ((ParameterizedTypeName) elementTypeName).typeArguments;
                if (args.size() > 0) {
                    referencedTableClassName = ClassName.bestGuess(args.get(0).toString());
                    isModelContainer = true;
                }
            } else {
                referencedTableClassName = ClassName.bestGuess(elementTypeName.toString());
            }
        }

        if (referencedTableClassName == null) {
            manager.logError("Referenced was null for %1s within %1s", typeElement, elementTypeName);
        }

        TypeElement element = manager.getElements().getTypeElement(
                manager.getTypeUtils().erasure(typeElement.asType()).toString());

        isModel = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(), ClassNames.MODEL.toString(), element);
        isModelContainer = isModelContainer || ProcessorUtils.implementsClass(manager.getProcessingEnvironment(), ClassNames.MODEL_CONTAINER.toString(), element);
        nonModelColumn = !isModel && !isModelContainer;

        // we need to recheck for this instance
        if (columnAccess instanceof TypeConverterAccess) {
            if (typeElement.getModifiers().contains(Modifier.PRIVATE)) {
                columnAccess = new PrivateColumnAccess(typeElement.getAnnotation(Column.class));
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
        }

    }

    @Override
    public void addPropertyDefinition(TypeSpec.Builder typeBuilder, TypeName tableClassName) {
        checkNeedsReferences();
        for (ForeignKeyReferenceDefinition reference : foreignKeyReferenceDefinitionList) {
            ParameterizedTypeName propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, reference.columnClassName.isPrimitive() ? reference.columnClassName.box() : reference.columnClassName);
            typeBuilder.addField(FieldSpec.builder(propParam, reference.columnName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("new $T<>($T.class, $S)", ClassNames.PROPERTY, tableClassName, reference.columnName).build());
        }
    }

    @Override
    public void addPropertyCase(MethodSpec.Builder methodBuilder) {
        checkNeedsReferences();
        for (ForeignKeyReferenceDefinition reference : foreignKeyReferenceDefinitionList) {
            methodBuilder.beginControlFlow("case $S: ", reference.columnName);
            methodBuilder.addStatement("return $L", reference.columnName);
            methodBuilder.endControlFlow();
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
    public CodeBlock getContentValuesStatement(boolean isModelContainerAdapter) {
        if (nonModelColumn) {
            return super.getContentValuesStatement(isModelContainerAdapter);
        } else {
            checkNeedsReferences();
            CodeBlock.Builder builder = CodeBlock.builder();
            String statement = columnAccess
                    .getColumnAccessString(elementTypeName, elementName, elementName,
                            ModelUtils.getVariable(isModelContainerAdapter), isModelContainerAdapter);
            String finalAccessStatement = getFinalAccessStatement(builder, isModelContainerAdapter, statement);
            builder.addStatement("// original statement: $L", statement);
            builder.beginControlFlow("if ($L != null)", finalAccessStatement);
            CodeBlock.Builder elseBuilder = CodeBlock.builder();
            for (ForeignKeyReferenceDefinition referenceDefinition : foreignKeyReferenceDefinitionList) {
                builder.add(referenceDefinition.getContentValuesStatement(isModelContainerAdapter));
                elseBuilder.addStatement("$L.putNull($S)", BindToContentValuesMethod.PARAM_CONTENT_VALUES, referenceDefinition.columnName);
            }
            builder.nextControlFlow("else")
                    .add(elseBuilder.build())
                    .endControlFlow();
            return builder.build();
        }
    }

    @Override
    public CodeBlock getSQLiteStatementMethod(AtomicInteger index, boolean isModelContainerAdapter) {
        if (nonModelColumn) {
            return super.getSQLiteStatementMethod(index, isModelContainerAdapter);
        } else {
            checkNeedsReferences();
            CodeBlock.Builder builder = CodeBlock.builder();
            String statement = columnAccess
                    .getColumnAccessString(elementTypeName, elementName, elementName,
                            ModelUtils.getVariable(isModelContainerAdapter), isModelContainerAdapter);
            String finalAccessStatement = getFinalAccessStatement(builder, isModelContainerAdapter, statement);
            builder.beginControlFlow("if ($L != null)", finalAccessStatement);
            CodeBlock.Builder elseBuilder = CodeBlock.builder();
            for (ForeignKeyReferenceDefinition referenceDefinition : foreignKeyReferenceDefinitionList) {
                builder.add(referenceDefinition.getSQLiteStatementMethod(index, isModelContainerAdapter));
                elseBuilder.addStatement("$L.bindNull($L)", BindToStatementMethod.PARAM_STATEMENT, index.intValue());
                index.incrementAndGet();
            }
            builder.nextControlFlow("else")
                    .add(elseBuilder.build())
                    .endControlFlow();
            return builder.build();
        }
    }

    @Override
    public CodeBlock getLoadFromCursorMethod(boolean isModelContainerAdapter) {
        if (nonModelColumn) {
            return super.getLoadFromCursorMethod(isModelContainerAdapter);
        } else {
            checkNeedsReferences();
            CodeBlock.Builder builder = CodeBlock.builder()
                    .add("//// Only load model if references match, for efficiency\n");
            CodeBlock.Builder ifNullBuilder = CodeBlock.builder()
                    .add("if (");
            CodeBlock.Builder selectBuilder = CodeBlock.builder();
            for (int i = 0; i < foreignKeyReferenceDefinitionList.size(); i++) {
                ForeignKeyReferenceDefinition referenceDefinition = foreignKeyReferenceDefinitionList.get(i);
                String indexName = "index" + referenceDefinition.columnName;
                builder.addStatement("int $L = $L.getColumnIndex($S)", indexName, LoadFromCursorMethod.PARAM_CURSOR, referenceDefinition.columnName);
                if (i > 0) {
                    ifNullBuilder.add(" && ");
                }
                ifNullBuilder.add("$L != -1 && !$L.isNull($L)", indexName, LoadFromCursorMethod.PARAM_CURSOR, indexName);

                String accessString;

                if (!isModelContainerAdapter) {
                    accessString = ModelUtils.getVariable(false) + "." + columnAccess.getShortAccessString(referenceDefinition.columnClassName, elementName, false) + "." +
                            referenceDefinition.columnAccess.getShortAccessString(referenceDefinition.columnClassName, referenceDefinition.foreignColumnName, false);
                } else {
                    accessString = columnAccess.getColumnAccessString(referenceDefinition.columnClassName, elementName, elementName, ModelUtils.getVariable(true), true);
                }

                // TODO: respect separator here.
                selectBuilder.add("\n.and($L.$L.eq($L))",
                        ClassName.get(referencedTableClassName.packageName(), referencedTableClassName.simpleName() + "_" + TableDefinition.DBFLOW_TABLE_TAG),
                        referenceDefinition.foreignColumnName, accessString);
            }
            ifNullBuilder.add(")");
            builder.beginControlFlow(ifNullBuilder.build().toString());

            CodeBlock.Builder initializer = CodeBlock.builder();

            // need to cast to type we're initializing.
            if (isModelContainer) {
                initializer.add("($T)", elementTypeName);
            }
            initializer.add("new $T().from($T.class).where()", ClassNames.SELECT, referencedTableClassName)
                    .add(selectBuilder.build());
            if (!isModelContainerAdapter && !isModelContainer) {
                initializer.add(".querySingle()");
            } else {
                if (isModelContainerAdapter) {
                    initializer.add(".queryModelContainer($L.getInstance($L.newDataInstance(), $T.class)).getData()", ModelUtils.getVariable(true),
                            ModelUtils.getVariable(true), referencedTableClassName);
                } else {
                    initializer.add(".queryModelContainer(new $T($T.class))", elementTypeName, referencedTableClassName);
                }
            }

            builder.addStatement(columnAccess.setColumnAccessString(elementTypeName, elementName, elementName,
                    isModelContainerAdapter, ModelUtils.getVariable(isModelContainerAdapter), initializer.build()));
            builder.endControlFlow();
            return builder.build();
        }
    }

    @Override
    public CodeBlock getToModelMethod(boolean isModelContainerAdapter) {
        checkNeedsReferences();
        if (nonModelColumn) {
            return super.getToModelMethod(isModelContainerAdapter);
        } else {
            CodeBlock.Builder builder = CodeBlock.builder();
            CodeBlock.Builder ifContains = CodeBlock.builder();
            CodeBlock.Builder selectBuilder = CodeBlock.builder();

            for (int i = 0; i < foreignKeyReferenceDefinitionList.size(); i++) {
                ForeignKeyReferenceDefinition referenceDefinition = foreignKeyReferenceDefinitionList.get(i);
                String method = SQLiteType.getModelContainerMethod(referenceDefinition.columnClassName);
                if (method == null) {
                    method = "get";
                }
                String accessStatement = getRefName() + referenceDefinition.columnName;
                CodeBlock.Builder codeBuilder = CodeBlock.builder()
                        .addStatement("$T $L = $L.$LValue($S)",
                                referenceDefinition.columnClassName,
                                accessStatement, ModelUtils.getVariable(true),
                                method, referenceDefinition.foreignColumnName);

                if (i > 0) {
                    ifContains.add(" && ");
                }
                ifContains.add("$L != $L", accessStatement, referenceDefinition.columnClassName.isPrimitive() ? "0" : "null");

                builder.add(codeBuilder.build());

                selectBuilder.add("\n.and($L.$L.eq($L))",
                        ClassName.get(referencedTableClassName.packageName(), referencedTableClassName.simpleName() + "_" + TableDefinition.DBFLOW_TABLE_TAG),
                        referenceDefinition.foreignColumnName, accessStatement);
            }

            builder.beginControlFlow("if ($L)", ifContains.build().toString());

            CodeBlock.Builder initializer = CodeBlock.builder();

            // need to cast to type we're initializing.
            if (isModelContainer) {
                initializer.add("($T)", elementTypeName);
            }
            initializer.add("new $T().from($T.class).where()", ClassNames.SELECT, referencedTableClassName)
                    .add(selectBuilder.build());
            if (!isModelContainerAdapter && !isModelContainer) {
                initializer.add(".querySingle()");
            } else {
                if (isModelContainerAdapter) {
                    initializer.add(".queryModelContainer($L.getInstance($L.newDataInstance(), $T.class)).getData()", ModelUtils.getVariable(true),
                            ModelUtils.getVariable(true), referencedTableClassName);
                } else {
                    initializer.add(".queryModelContainer(new $T($T.class))", elementTypeName, referencedTableClassName);
                }
            }
            builder.addStatement(columnAccess.setColumnAccessString(elementTypeName, elementName, elementName,
                    isModelContainerAdapter, ModelUtils.getVariable(isModelContainerAdapter), initializer.build()));

            builder.endControlFlow();
            return builder.build();
        }
    }

    String getFinalAccessStatement(CodeBlock.Builder codeBuilder, boolean isModelContainerAdapter, String statement) {
        String finalAccessStatement = statement;
        if (columnAccess instanceof TypeConverterAccess ||
                columnAccess instanceof ModelContainerAccess ||
                isModelContainerAdapter) {
            finalAccessStatement = getRefName();

            TypeName typeName;
            if (columnAccess instanceof TypeConverterAccess) {
                typeName = ((TypeConverterAccess) columnAccess).typeConverterDefinition.getDbTypeName();
            } else if (columnAccess instanceof ModelContainerAccess) {
                typeName = ModelUtils.getModelContainerType(manager, elementTypeName);
            } else {
                if (isModelContainer || isModel) {
                    typeName = ModelUtils.getModelContainerType(manager, elementTypeName);
                    statement = ModelUtils.getVariable(isModelContainerAdapter) + ".getInstance(" + statement + ", " + referencedTableClassName + ".class)";
                } else {
                    typeName = elementTypeName;
                }
            }

            codeBuilder.addStatement("$T $L = $L", typeName,
                    finalAccessStatement, statement);
        }
        return finalAccessStatement;
    }

    String getForeignKeyReferenceAccess(boolean isModelContainerAdapter, String statement) {
        if (columnAccess instanceof TypeConverterAccess ||
                columnAccess instanceof ModelContainerAccess ||
                isModelContainerAdapter) {
            return getRefName();
        } else {
            return statement;
        }
    }

    public String getRefName() {
        return "ref" + elementName;
    }

    /**
     * If {@link ForeignKey} has no {@link ForeignKeyReference}s, we use the primary key the referenced
     * table. We do this post-evaluation so all of the {@link TableDefinition} can be generated.
     */
    private void checkNeedsReferences() {
        TableDefinition referencedTableDefinition = manager.getTableDefinition(tableDefinition.databaseTypeName, referencedTableClassName);
        if (referencedTableDefinition == null) {
            manager.logError("Could not find the referenced table definition %1s from %1s. Ensure it exists in the same" +
                    "database %1s", referencedTableClassName, tableDefinition.tableName, tableDefinition.databaseTypeName);
        } else {
            if (needsReferences) {
                List<ColumnDefinition> primaryColumns = referencedTableDefinition.getPrimaryColumnDefinitions();
                for (ColumnDefinition primaryColumn : primaryColumns) {
                    ForeignKeyReferenceDefinition foreignKeyReferenceDefinition =
                            new ForeignKeyReferenceDefinition(manager, elementName, primaryColumn, columnAccess, this);
                    foreignKeyReferenceDefinitionList.add(foreignKeyReferenceDefinition);
                }
                if (nonModelColumn) {
                    columnName = foreignKeyReferenceDefinitionList.get(0).columnName;
                }
                needsReferences = false;
            }
        }
    }
}
