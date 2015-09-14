package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.concurrent.atomic.AtomicInteger;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * Description:
 */
public class ForeignKeyReferenceDefinition {

    private final ProcessorManager manager;
    private final String foreignKeyFieldName;

    public final String columnName;
    public final String foreignColumnName;
    public final TypeName columnClassName;

    private boolean isReferencedFieldPrivate;

    public BaseColumnAccess columnAccess;

    private final BaseColumnAccess tableColumnAccess;
    private final ForeignKeyColumnDefinition foreignKeyColumnDefinition;

    private final BaseColumnAccess simpleColumnAccess = new SimpleColumnAccess();

    public ForeignKeyReferenceDefinition(ProcessorManager manager, String foreignKeyFieldName,
                                         ColumnDefinition referencedColumn,
                                         BaseColumnAccess tableColumnAccess,
                                         ForeignKeyColumnDefinition foreignKeyColumnDefinition) {
        this.manager = manager;
        this.foreignKeyColumnDefinition = foreignKeyColumnDefinition;
        this.tableColumnAccess = tableColumnAccess;
        this.foreignKeyFieldName = foreignKeyFieldName;

        columnName = foreignKeyFieldName + "_" + referencedColumn.columnFieldName;
        foreignColumnName = referencedColumn.columnName;
        columnClassName = referencedColumn.elementClassName;

        if (referencedColumn.columnAccess instanceof WrapperColumnAccess) {
            isReferencedFieldPrivate = (((WrapperColumnAccess) referencedColumn.columnAccess).existingColumnAccess instanceof PrivateColumnAccess);
        } else {
            isReferencedFieldPrivate = (referencedColumn.columnAccess instanceof PrivateColumnAccess);
        }
        if (isReferencedFieldPrivate && !foreignKeyColumnDefinition.isModelContainer) {
            columnAccess = new PrivateColumnAccess(referencedColumn.column);
        } else {
            if (foreignKeyColumnDefinition.isModelContainer) {
                columnAccess = new ModelContainerAccess(tableColumnAccess, foreignColumnName);
            } else {
                columnAccess = new SimpleColumnAccess();
            }
        }
    }

    public ForeignKeyReferenceDefinition(ProcessorManager manager, String foreignKeyFieldName,
                                         ForeignKeyReference foreignKeyReference, BaseColumnAccess tableColumnAccess,
                                         ForeignKeyColumnDefinition foreignKeyColumnDefinition) {
        this.manager = manager;
        this.tableColumnAccess = tableColumnAccess;
        this.foreignKeyColumnDefinition = foreignKeyColumnDefinition;
        this.foreignKeyFieldName = foreignKeyFieldName;

        columnName = foreignKeyReference.columnName();
        foreignColumnName = foreignKeyReference.foreignKeyColumnName();

        TypeMirror columnClass = null;
        try {
            foreignKeyReference.columnType();
        } catch (MirroredTypeException mte) {
            columnClass = mte.getTypeMirror();
        }
        columnClassName = TypeName.get(columnClass);
        isReferencedFieldPrivate = foreignKeyReference.referencedFieldIsPrivate();
        if (isReferencedFieldPrivate && !foreignKeyColumnDefinition.isModelContainer) {
            columnAccess = new PrivateColumnAccess(foreignKeyReference);
        } else {
            if (foreignKeyColumnDefinition.isModelContainer) {
                columnAccess = new ModelContainerAccess(tableColumnAccess, foreignColumnName);
            } else {
                columnAccess = new SimpleColumnAccess();
            }
        }
    }

    CodeBlock getCreationStatement() {
        return DefinitionUtils.getCreationStatement(columnClassName, null, columnName).build();
    }

    CodeBlock getContentValuesStatement(boolean isModelContainerAdapter) {
        // fix its access here.
        String shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, isModelContainerAdapter);
        shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(isModelContainerAdapter, shortAccess);

        String columnShortAccess = getShortColumnAccess(isModelContainerAdapter);

        String combined = shortAccess + (isModelContainerAdapter ? "" : ".") + columnShortAccess;
        return DefinitionUtils.getContentValuesStatement(columnShortAccess, combined,
                columnName, columnClassName, isModelContainerAdapter, simpleColumnAccess, getForeignKeyColumnVariable(isModelContainerAdapter)).build();
    }

    CodeBlock getSQLiteStatementMethod(AtomicInteger index, boolean isModelContainerAdapter) {
        String shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, isModelContainerAdapter);
        shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(isModelContainerAdapter, shortAccess);

        String columnShortAccess = getShortColumnAccess(isModelContainerAdapter);
        String combined = shortAccess + (isModelContainerAdapter ? "" : ".") + columnShortAccess;
        return DefinitionUtils.getSQLiteStatementMethod(
                index, columnShortAccess, combined,
                columnClassName, isModelContainerAdapter, simpleColumnAccess, getForeignKeyColumnVariable(isModelContainerAdapter)).build();
    }

    private String getForeignKeyColumnVariable(boolean isModelContainerAdapter) {
        return isModelContainerAdapter ? foreignKeyColumnDefinition.getRefName() : ModelUtils.getVariable(isModelContainerAdapter);
    }

    private String getShortColumnAccess(boolean isModelContainerAdapter) {
        return isModelContainerAdapter ? foreignColumnName
                : columnAccess.getShortAccessString(columnClassName, foreignColumnName, isModelContainerAdapter);
    }

}
