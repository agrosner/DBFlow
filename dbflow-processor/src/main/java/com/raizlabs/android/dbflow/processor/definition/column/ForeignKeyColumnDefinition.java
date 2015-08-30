package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.BindToContentValuesMethod;
import com.raizlabs.android.dbflow.processor.definition.method.BindToStatementMethod;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
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

    public ClassName referencedTableClassName;

    public ForeignKeyAction onDelete;
    public ForeignKeyAction onUpdate;

    public boolean isModel;

    public ForeignKeyColumnDefinition(ProcessorManager manager, Element typeElement) {
        super(manager, typeElement);

        ForeignKey foreignKey = typeElement.getAnnotation(ForeignKey.class);
        onUpdate = foreignKey.onUpdate();
        onDelete = foreignKey.onDelete();

        try {
            foreignKey.tableClass();
        } catch (MirroredTypeException mte) {
            referencedTableClassName = ClassName.get(manager.getElements().getTypeElement(mte.getTypeMirror().toString()));
        }

        // hopefully intentionally left blank
        if (!referencedTableClassName.equals(TypeName.OBJECT)) {
            referencedTableClassName = ClassName.get(manager.getElements().getTypeElement(typeElement.asType().toString()));
        } else {
            referencedTableClassName = elementClassName;
        }

        TypeElement element = manager.getProcessingEnvironment().getElementUtils().getTypeElement(elementTypeName.toString());
        isModel = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(), ClassNames.MODEL.toString(), element);

        // we need to recheck for this instance
        if (columnAccess instanceof TypeConverterAccess) {
            if (typeElement.getModifiers().contains(Modifier.PRIVATE)) {
                columnAccess = new PrivateColumnAccess(typeElement.getAnnotation(Column.class));
            } else {
                columnAccess = new SimpleColumnAccess();
            }
        }

        ForeignKeyReference[] references = foreignKey.references();
        for (ForeignKeyReference reference : references) {
            ForeignKeyReferenceDefinition referenceDefinition = new ForeignKeyReferenceDefinition(manager, elementName, reference, columnAccess);
            // TODO: add validation
            foreignKeyReferenceDefinitionList.add(referenceDefinition);
        }

    }

    @Override
    public void addPropertyDefinition(TypeSpec.Builder typeBuilder) {
        for (ForeignKeyReferenceDefinition reference : foreignKeyReferenceDefinitionList) {
            ParameterizedTypeName propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, reference.columnClassName.isPrimitive() ? reference.columnClassName.box() : reference.columnClassName);
            typeBuilder.addField(FieldSpec.builder(propParam, reference.columnName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("new $T<>($S)", ClassNames.PROPERTY, reference.columnName).build());
        }
    }

    @Override
    public CodeBlock getInsertStatementColumnName() {
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
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.beginControlFlow("if ($L != null)", columnAccess
                .getColumnAccessString(elementTypeName, elementName, isModelContainerAdapter, BindToContentValuesMethod.PARAM_MODEL));
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

    @Override
    public CodeBlock getSQLiteStatementMethod(AtomicInteger index, boolean isModelContainerAdapter) {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.beginControlFlow("if ($L != null)", columnAccess
                .getColumnAccessString(elementTypeName, elementName, isModelContainerAdapter, BindToStatementMethod.PARAM_MODEL));
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

    @Override
    public CodeBlock getLoadFromCursorMethod(boolean isModelContainerAdapter) {
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

            // TODO: respect separator here.
            selectBuilder.add("\n.and($L.$L.eq($L.$L))",
                    ClassName.get(referencedTableClassName.packageName(), referencedTableClassName.simpleName() + "_" + TableDefinition.DBFLOW_TABLE_TAG),
                    referenceDefinition.foreignColumnName, LoadFromCursorMethod.PARAM_MODEL,
                    columnAccess.getShortAccessString(elementName, isModelContainerAdapter) + "." +
                            referenceDefinition.columnAccess.getShortAccessString(referenceDefinition.foreignColumnName, isModelContainerAdapter));
        }
        ifNullBuilder.add(")");
        builder.beginControlFlow(ifNullBuilder.build().toString());
        builder.addStatement(columnAccess.setColumnAccessString(elementTypeName, elementName,
                CodeBlock.builder()
                        .add("new $T().from($T.class).where()", ClassNames.SELECT, referencedTableClassName)
                        .add(selectBuilder.build())
                        .add(".querySingle()")
                        .build().toString(), isModelContainerAdapter, LoadFromCursorMethod.PARAM_MODEL));
        builder.endControlFlow();
        return builder.build();
    }
}
