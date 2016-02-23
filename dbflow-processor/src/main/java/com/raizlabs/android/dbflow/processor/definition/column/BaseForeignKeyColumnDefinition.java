package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.SQLiteHelper;
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
import com.squareup.javapoet.WildcardTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Description:
 */
public abstract class BaseForeignKeyColumnDefinition extends ColumnDefinition {

    public final List<ForeignKeyReferenceDefinition> foreignKeyReferenceDefinitionList = new ArrayList<>();

    public final TableDefinition tableDefinition;

    public ClassName referencedTableClassName;

    public boolean isModelContainer;

    public boolean isModel;

    public boolean isForeignKeyContainer = false;

    public boolean nonModelColumn;

    public boolean saveForeignKeyModel = false;

    public boolean needsReferences;

    public BaseForeignKeyColumnDefinition(ProcessorManager manager, TableDefinition tableDefinition, Element typeElement, boolean isPackagePrivate) {
        super(manager, typeElement, tableDefinition, isPackagePrivate);
        this.tableDefinition = tableDefinition;

        TypeElement element = manager.getElements().getTypeElement(
                manager.getTypeUtils().erasure(typeElement.asType()).toString());

        isModel = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(), ClassNames.MODEL.toString(), element);
        isModelContainer = isModelContainer || ProcessorUtils.implementsClass(manager.getProcessingEnvironment(), ClassNames.MODEL_CONTAINER.toString(), element);
        nonModelColumn = !isModel && !isModelContainer;

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
                    ModelUtils.getVariable(isModelContainerAdapter), isModelContainerAdapter, false);
            String finalAccessStatement = getFinalAccessStatement(builder, isModelContainerAdapter, statement);
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
    public CodeBlock getSQLiteStatementMethod(AtomicInteger index, boolean isModelContainerAdapter) {
        if (nonModelColumn) {
            return super.getSQLiteStatementMethod(index, isModelContainerAdapter);
        } else {
            checkNeedsReferences();
            CodeBlock.Builder builder = CodeBlock.builder();
            String statement = columnAccess
                .getColumnAccessString(elementTypeName, elementName, elementName,
                    ModelUtils.getVariable(isModelContainerAdapter), isModelContainerAdapter, true);
            String finalAccessStatement = getFinalAccessStatement(builder, isModelContainerAdapter, statement);
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
                builder.add(referenceDefinition.getSQLiteStatementMethod(index, isModelContainerAdapter));
                elseBuilder.addStatement("$L.bindNull($L)", BindToStatementMethod.PARAM_STATEMENT, index.intValue() + " + " + BindToStatementMethod.PARAM_START);
            }

            builder.nextControlFlow("else")
                .add(elseBuilder.build())
                .endControlFlow();
            return builder.build();
        }
    }

    @Override
    public CodeBlock getLoadFromCursorMethod(boolean isModelContainerAdapter, boolean putNullForContainerAdapter,
                                             boolean endNonPrimitiveIf) {
        if (nonModelColumn)
            return super.getLoadFromCursorMethod(isModelContainerAdapter, putNullForContainerAdapter, endNonPrimitiveIf);

        checkNeedsReferences();
        CodeBlock.Builder builder = CodeBlock.builder()
            .add("//// Only load model if references match, for efficiency\n");
        CodeBlock.Builder ifNullBuilder = CodeBlock.builder()
            .add("if (");
        CodeBlock.Builder selectBuilder = CodeBlock.builder();

        // used for foreignkey containers only.
        String foreignKeyContainerRefName = "ref" + columnName;

        for (int i = 0; i < foreignKeyReferenceDefinitionList.size(); i++) {
            ForeignKeyReferenceDefinition referenceDefinition = foreignKeyReferenceDefinitionList.get(i);
            String indexName = "index" + referenceDefinition.columnName;
            builder.addStatement("int $L = $L.getColumnIndex($S)", indexName, LoadFromCursorMethod.PARAM_CURSOR, referenceDefinition.columnName);
            if (i > 0) {
                ifNullBuilder.add(" && ");
            }
            ifNullBuilder.add("$L != -1 && !$L.isNull($L)", indexName, LoadFromCursorMethod.PARAM_CURSOR, indexName);

            CodeBlock loadFromCursorBlock = CodeBlock.builder().add("$L.$L($L)", LoadFromCursorMethod.PARAM_CURSOR,
                DefinitionUtils.getLoadFromCursorMethodString(referenceDefinition.columnClassName,
                    referenceDefinition.columnAccess), indexName).build();
            ClassName generatedTableRef = ClassName.get(referencedTableClassName.packageName(), referencedTableClassName.simpleName()
                + tableDefinition.databaseDefinition.classSeparator + TableDefinition.DBFLOW_TABLE_TAG);
            if (!isForeignKeyContainer) {
                selectBuilder.add("\n.and($L.$L.eq($L))", generatedTableRef,
                    referenceDefinition.foreignColumnName, loadFromCursorBlock);
            } else {
                selectBuilder.add("\n$L.put($S, $L);", foreignKeyContainerRefName, referenceDefinition.foreignColumnName,
                    loadFromCursorBlock);
            }
        }
        ifNullBuilder.add(")");
        builder.beginControlFlow(ifNullBuilder.build().toString());

        CodeBlock.Builder initializer = CodeBlock.builder();

        if (isForeignKeyContainer) {

            builder.addStatement("$T $L = new $T<>($T.class)",
                ParameterizedTypeName.get(ClassNames.FOREIGN_KEY_CONTAINER, referencedTableClassName),
                foreignKeyContainerRefName,
                ClassNames.FOREIGN_KEY_CONTAINER, referencedTableClassName);

            builder.add(selectBuilder.build()).add("\n");

            initializer.add(foreignKeyContainerRefName);
        } else {
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
        }

        builder.addStatement(columnAccess.setColumnAccessString(elementTypeName, elementName, elementName,
            isModelContainerAdapter, ModelUtils.getVariable(isModelContainerAdapter), initializer.build(), false));

        boolean putDefaultValue = putNullForContainerAdapter;
        if (putContainerDefaultValue != putDefaultValue && isModelContainerAdapter) {
            putDefaultValue = putContainerDefaultValue;
        }
        if (putDefaultValue) {
            builder.nextControlFlow("else");
            builder.addStatement("$L.putDefault($S)", ModelUtils.getVariable(true), columnName);
        }
        if (endNonPrimitiveIf) {
            builder.endControlFlow();
        }
        return builder.build();
    }

    @Override
    public CodeBlock getToModelMethod() {
        checkNeedsReferences();
        if (nonModelColumn) {
            return super.getToModelMethod();
        } else {
            CodeBlock.Builder builder = CodeBlock.builder();
            String statement = columnAccess
                .getColumnAccessString(elementTypeName, elementName, elementName,
                    ModelUtils.getVariable(true), true, true);
            String finalAccessStatement = getFinalAccessStatement(builder, true, statement);

            builder.beginControlFlow("if ($L != null)", finalAccessStatement);
            if (!isModelContainer) {
                CodeBlock.Builder modelContainerRetrieval = CodeBlock.builder();
                modelContainerRetrieval.add("$L.getContainerAdapter($T.class).toModel($L)", ClassNames.FLOW_MANAGER,
                    referencedTableClassName, finalAccessStatement);
                builder.addStatement(columnAccess.setColumnAccessString(elementTypeName, elementName, elementName,
                    false, ModelUtils.getVariable(false), modelContainerRetrieval.build(), true));
            } else {
                builder.addStatement(columnAccess.setColumnAccessString(elementTypeName, elementName, elementName,
                    false, ModelUtils.getVariable(false), CodeBlock.builder().add("new $T($L)",
                        elementTypeName, finalAccessStatement).build(), true));
            }
            builder.endControlFlow();
            return builder.build();
        }
    }

    @Override
    public void appendPropertyComparisonAccessStatement(boolean isModelContainerAdapter, CodeBlock.Builder codeBuilder) {
        if (!(columnAccess instanceof TypeConverterAccess)) {
            String origStatement = getColumnAccessString(isModelContainerAdapter, false);
            if (isPrimaryKey) {
                TableDefinition referenced = manager.getTableDefinition(tableDefinition.databaseDefinition.elementTypeName,
                    referencedTableClassName);
                String statement = "";
                String variableName = "container" + elementName;
                TypeName typeName = elementTypeName;
                if (isModelContainerAdapter) {
                    typeName = ParameterizedTypeName.get(ClassNames.MODEL_CONTAINER, elementTypeName, WildcardTypeName.subtypeOf(Object.class));
                }
                codeBuilder.addStatement("\n$T $L = ($T) $L", typeName, variableName, typeName, origStatement);
                codeBuilder.beginControlFlow("if ($L != null)", variableName);
                CodeBlock.Builder elseBuilder = CodeBlock.builder();
                for (ForeignKeyReferenceDefinition referenceDefinition : getForeignKeyReferenceDefinitionList()) {
                    if (isModelContainer || isModelContainerAdapter) {
                        // check for null and retrieve proper value
                        String method = SQLiteHelper.getModelContainerMethod(referenceDefinition.columnClassName);
                        if (method == null) {
                            method = "get";
                        }
                        statement = String
                            .format("%1s.%1sValue(%1s.%1s.getContainerKey())", variableName, method, referenced.outputClassName, referenceDefinition.foreignColumnName);
                    } else if (isModel) {
                        statement = referenceDefinition.getPrimaryReferenceString(isModelContainerAdapter);
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
        } else {
            super.appendPropertyComparisonAccessStatement(isModelContainerAdapter, codeBuilder);
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
                typeName = ModelUtils.getModelContainerType(manager, referencedTableClassName);
            } else {
                if (isModelContainer || isModel) {
                    typeName = ModelUtils.getModelContainerType(manager, referencedTableClassName);
                    statement = ModelUtils.getVariable(isModelContainerAdapter) + ".getInstance(" + statement + ", " + referencedTableClassName + ".class)";
                } else {
                    typeName = referencedTableClassName;
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

    public String getRefName() {
        return "ref" + elementName;
    }

    public List<ForeignKeyReferenceDefinition> getForeignKeyReferenceDefinitionList() {
        checkNeedsReferences();
        return foreignKeyReferenceDefinitionList;
    }

    protected abstract void checkNeedsReferences ();
}
