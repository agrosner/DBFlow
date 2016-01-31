package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.concurrent.atomic.AtomicInteger;

import javax.lang.model.element.TypeElement;
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
    private boolean isReferencedFieldPackagePrivate;

    public BaseColumnAccess columnAccess;

    private final BaseColumnAccess tableColumnAccess;
    private final BaseForeignKeyColumnDefinition foreignKeyColumnDefinition;

    private final BaseColumnAccess simpleColumnAccess;

    public ForeignKeyReferenceDefinition(ProcessorManager manager,
                                         String foreignKeyFieldName,
                                         ColumnDefinition referencedColumn,
                                         BaseColumnAccess tableColumnAccess,
                                         BaseForeignKeyColumnDefinition foreignKeyColumnDefinition) {
        this.manager = manager;
        this.foreignKeyColumnDefinition = foreignKeyColumnDefinition;
        this.tableColumnAccess = tableColumnAccess;
        this.foreignKeyFieldName = foreignKeyFieldName;

        if (!foreignKeyColumnDefinition.isPrimaryKey && !foreignKeyColumnDefinition.isPrimaryKeyAutoIncrement) {
            columnName = foreignKeyFieldName + "_" + referencedColumn.columnName;
        } else {
            columnName = foreignKeyFieldName;
        }
        foreignColumnName = referencedColumn.columnName;
        columnClassName = referencedColumn.elementTypeName;

        if (referencedColumn.columnAccess instanceof WrapperColumnAccess) {
            isReferencedFieldPrivate = (((WrapperColumnAccess) referencedColumn.columnAccess).existingColumnAccess instanceof PrivateColumnAccess);
            isReferencedFieldPackagePrivate = ((WrapperColumnAccess) referencedColumn.columnAccess).existingColumnAccess instanceof PackagePrivateAccess;
        } else {
            isReferencedFieldPrivate = (referencedColumn.columnAccess instanceof PrivateColumnAccess);
            isReferencedFieldPackagePrivate = referencedColumn.columnAccess instanceof PackagePrivateAccess;
        }
        if (isReferencedFieldPrivate && !foreignKeyColumnDefinition.isModelContainer) {
            columnAccess = new PrivateColumnAccess(referencedColumn.column, false);
        } else if (isReferencedFieldPackagePrivate && !foreignKeyColumnDefinition.isModelContainer) {
            columnAccess = new PackagePrivateAccess(referencedColumn.packageName,
                    foreignKeyColumnDefinition.tableDefinition.databaseDefinition.classSeparator,
                    ClassName.get((TypeElement) referencedColumn.element.getEnclosingElement()).simpleName());
        } else {
            if (foreignKeyColumnDefinition.isModelContainer) {
                columnAccess = new ModelContainerAccess(tableColumnAccess, foreignColumnName);
            } else {
                columnAccess = new SimpleColumnAccess();
            }
        }

        simpleColumnAccess = new SimpleColumnAccess(columnAccess instanceof PackagePrivateAccess);
    }

    public ForeignKeyReferenceDefinition(ProcessorManager manager,
                                         String foreignKeyFieldName,
                                         ForeignKeyReference foreignKeyReference,
                                         BaseColumnAccess tableColumnAccess,
                                         BaseForeignKeyColumnDefinition foreignKeyColumnDefinition) {
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
        isReferencedFieldPackagePrivate = foreignKeyReference.referencedFieldIsPackagePrivate();
        if (isReferencedFieldPrivate && !foreignKeyColumnDefinition.isModelContainer) {
            columnAccess = new PrivateColumnAccess(foreignKeyReference);
        } else if (isReferencedFieldPackagePrivate && !foreignKeyColumnDefinition.isModelContainer) {
            columnAccess = new PackagePrivateAccess(foreignKeyColumnDefinition.referencedTableClassName.packageName(),
                    foreignKeyColumnDefinition.tableDefinition.databaseDefinition.classSeparator,
                    foreignKeyColumnDefinition.referencedTableClassName.simpleName());
        } else {
            if (foreignKeyColumnDefinition.isModelContainer) {
                columnAccess = new ModelContainerAccess(tableColumnAccess, foreignColumnName);
            } else {
                columnAccess = new SimpleColumnAccess();
            }
        }

        simpleColumnAccess = new SimpleColumnAccess(columnAccess instanceof PackagePrivateAccess);
    }

    CodeBlock getCreationStatement() {
        return DefinitionUtils.getCreationStatement(columnClassName, null, columnName).build();
    }

    CodeBlock getContentValuesStatement(boolean isModelContainerAdapter) {
        // fix its access here.
        String shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, isModelContainerAdapter, false);
        shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(isModelContainerAdapter, shortAccess);

        String columnShortAccess = getShortColumnAccess(isModelContainerAdapter, false, shortAccess);

        String combined;
        if (!(columnAccess instanceof PackagePrivateAccess)) {
            combined = shortAccess + (isModelContainerAdapter ? "" : ".") + columnShortAccess;
        } else {
            combined = columnShortAccess;
        }
        return DefinitionUtils.getContentValuesStatement(columnShortAccess, combined,
                columnName, columnClassName, isModelContainerAdapter, simpleColumnAccess, getForeignKeyColumnVariable(isModelContainerAdapter)).build();
    }

    CodeBlock getSQLiteStatementMethod(AtomicInteger index, boolean isModelContainerAdapter) {
        String shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, isModelContainerAdapter, true);
        shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(isModelContainerAdapter, shortAccess);

        String columnShortAccess = getShortColumnAccess(isModelContainerAdapter, true, shortAccess);
        String combined = shortAccess + (isModelContainerAdapter ? "" : ".") + columnShortAccess;
        return DefinitionUtils.getSQLiteStatementMethod(
                index, columnShortAccess, combined,
                columnClassName, isModelContainerAdapter, simpleColumnAccess,
                getForeignKeyColumnVariable(isModelContainerAdapter), false).build();
    }

    private String getForeignKeyColumnVariable(boolean isModelContainerAdapter) {
        return isModelContainerAdapter ? foreignKeyColumnDefinition.getRefName() : ModelUtils.getVariable(isModelContainerAdapter);
    }

    private String getShortColumnAccess(boolean isModelContainerAdapter, boolean isSqliteMethod, String shortAccess) {
        if (isModelContainerAdapter) {
            return foreignColumnName;
        } else {
            if (columnAccess instanceof PackagePrivateAccess) {
                return columnAccess.getColumnAccessString(columnClassName, foreignColumnName, "", ModelUtils.getVariable(isModelContainerAdapter) + "." + shortAccess, isModelContainerAdapter, isSqliteMethod);
            } else {
                return columnAccess.getShortAccessString(columnClassName, foreignColumnName, isModelContainerAdapter, isSqliteMethod);
            }
        }
    }

}
