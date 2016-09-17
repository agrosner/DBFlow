package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ElementUtility;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
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
    private final ForeignKeyColumnDefinition foreignKeyColumnDefinition;

    private final BaseColumnAccess simpleColumnAccess;

    public ForeignKeyReferenceDefinition(ProcessorManager manager, String foreignKeyFieldName,
                                         ColumnDefinition referencedColumn,
                                         BaseColumnAccess tableColumnAccess,
                                         ForeignKeyColumnDefinition foreignKeyColumnDefinition, int referenceCount) {
        this.manager = manager;
        this.foreignKeyColumnDefinition = foreignKeyColumnDefinition;
        this.tableColumnAccess = tableColumnAccess;
        this.foreignKeyFieldName = foreignKeyFieldName;

        if (!foreignKeyColumnDefinition.isPrimaryKey && !foreignKeyColumnDefinition.isPrimaryKeyAutoIncrement() && !foreignKeyColumnDefinition.isRowId
                || referenceCount > 0) {
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

            // fix here to ensure we can access it otherwise we generate helper
            boolean isPackagePrivate = ElementUtility.isPackagePrivate(referencedColumn.element);
            boolean isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, referencedColumn.element, foreignKeyColumnDefinition.element);

            isReferencedFieldPackagePrivate = referencedColumn.columnAccess instanceof PackagePrivateAccess
                    || isPackagePrivateNotInSamePackage;
        }
        if (isReferencedFieldPrivate) {
            columnAccess = new PrivateColumnAccess(referencedColumn.column, false);
        } else if (isReferencedFieldPackagePrivate) {
            columnAccess = new PackagePrivateAccess(referencedColumn.packageName,
                    foreignKeyColumnDefinition.tableDefinition.databaseDefinition.classSeparator,
                    ClassName.get((TypeElement) referencedColumn.element.getEnclosingElement()).simpleName());
            PackagePrivateAccess.putElement(((PackagePrivateAccess) columnAccess).helperClassName, foreignColumnName);
        } else {
            columnAccess = new SimpleColumnAccess();
        }

        simpleColumnAccess = new SimpleColumnAccess(columnAccess instanceof PackagePrivateAccess);
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
        isReferencedFieldPackagePrivate = foreignKeyReference.referencedFieldIsPackagePrivate();
        if (isReferencedFieldPrivate) {
            columnAccess = new PrivateColumnAccess(foreignKeyReference);
        } else if (isReferencedFieldPackagePrivate) {
            columnAccess = new PackagePrivateAccess(foreignKeyColumnDefinition.referencedTableClassName.packageName(),
                    foreignKeyColumnDefinition.tableDefinition.databaseDefinition.classSeparator,
                    foreignKeyColumnDefinition.referencedTableClassName.simpleName());
            PackagePrivateAccess.putElement(((PackagePrivateAccess) columnAccess).helperClassName, foreignColumnName);
        } else {
            columnAccess = new SimpleColumnAccess();
        }

        simpleColumnAccess = new SimpleColumnAccess(columnAccess instanceof PackagePrivateAccess);
    }

    CodeBlock getCreationStatement() {
        return DefinitionUtils.getCreationStatement(columnClassName, null, columnName).build();
    }

    String getPrimaryKeyName() {
        return QueryBuilder.quote(columnName);
    }

    CodeBlock getContentValuesStatement(boolean isModelContainerAdapter) {
        // fix its access here.
        String shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, isModelContainerAdapter, false);
        shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(shortAccess);

        String columnShortAccess = getShortColumnAccess(isModelContainerAdapter, false, shortAccess);

        String combined;
        if (!(columnAccess instanceof PackagePrivateAccess)) {
            combined = shortAccess + (isModelContainerAdapter ? "" : ".") + columnShortAccess;
        } else {
            combined = columnShortAccess;
        }
        return DefinitionUtils.getContentValuesStatement(columnShortAccess, combined,
                columnName, columnClassName, isModelContainerAdapter, simpleColumnAccess,
                getForeignKeyColumnVariable(isModelContainerAdapter), null,
                foreignKeyColumnDefinition.tableDefinition.outputClassName).build();
    }

    public String getPrimaryReferenceString(boolean isModelContainerAdapter) {
        String shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, isModelContainerAdapter, false);
        shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(shortAccess);
        String columnShortAccess = getShortColumnAccess(isModelContainerAdapter, false, shortAccess);
        String combined;
        if (!(columnAccess instanceof PackagePrivateAccess)) {
            combined = ModelUtils.getVariable(isModelContainerAdapter) + "." + shortAccess + (isModelContainerAdapter ? "" : ".") + columnShortAccess;
        } else {
            combined = columnShortAccess;
        }
        return combined;
    }

    CodeBlock getSQLiteStatementMethod(AtomicInteger index, boolean isModelContainerAdapter) {
        String shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, isModelContainerAdapter, true);
        shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(shortAccess);

        String columnShortAccess = getShortColumnAccess(isModelContainerAdapter, true, shortAccess);
        String combined = shortAccess + (isModelContainerAdapter ? "" : ".") + columnShortAccess;
        return DefinitionUtils.getSQLiteStatementMethod(
                index, columnShortAccess, combined,
                columnClassName, isModelContainerAdapter, simpleColumnAccess,
                getForeignKeyColumnVariable(isModelContainerAdapter), false, null).build();
    }

    CodeBlock getForeignKeyContainerMethod(ClassName tableClassName) {

        String access = getShortColumnAccess(false, false, tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, false, false));
        if (foreignKeyColumnDefinition.isModel && !isReferencedFieldPackagePrivate) {
            access = foreignKeyColumnDefinition.getColumnAccessString(false, false) + "." + access;
        }

        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.addStatement("$L.put($T.$L, $L)", ModelUtils.getVariable(true), tableClassName, columnName, access);
        return codeBuilder.build();
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
