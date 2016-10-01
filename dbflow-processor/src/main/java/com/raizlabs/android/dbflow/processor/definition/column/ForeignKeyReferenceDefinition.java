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

        if (!foreignKeyColumnDefinition.getIsPrimaryKey() && !foreignKeyColumnDefinition.isPrimaryKeyAutoIncrement() && !foreignKeyColumnDefinition.getIsRowId()
                || referenceCount > 0) {
            columnName = foreignKeyFieldName + "_" + referencedColumn.getColumnName();
        } else {
            columnName = foreignKeyFieldName;
        }
        foreignColumnName = referencedColumn.getColumnName();
        columnClassName = referencedColumn.elementTypeName;

        if (referencedColumn.getColumnAccess() instanceof WrapperColumnAccess) {
            isReferencedFieldPrivate = (((WrapperColumnAccess) referencedColumn.getColumnAccess()).existingColumnAccess instanceof PrivateColumnAccess);
            isReferencedFieldPackagePrivate = ((WrapperColumnAccess) referencedColumn.getColumnAccess()).existingColumnAccess instanceof PackagePrivateAccess;
        } else {
            isReferencedFieldPrivate = (referencedColumn.getColumnAccess() instanceof PrivateColumnAccess);

            // fix here to ensure we can access it otherwise we generate helper
            boolean isPackagePrivate = ElementUtility.isPackagePrivate(referencedColumn.element);
            boolean isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, referencedColumn.element, foreignKeyColumnDefinition.element);

            isReferencedFieldPackagePrivate = referencedColumn.getColumnAccess() instanceof PackagePrivateAccess
                    || isPackagePrivateNotInSamePackage;
        }
        if (isReferencedFieldPrivate) {
            columnAccess = new PrivateColumnAccess(referencedColumn.getColumn(), false);
        } else if (isReferencedFieldPackagePrivate) {
            columnAccess = new PackagePrivateAccess(referencedColumn.packageName,
                    foreignKeyColumnDefinition.tableDefinition.getDatabaseDefinition().classSeparator,
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
                    foreignKeyColumnDefinition.tableDefinition.getDatabaseDefinition().classSeparator,
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
        CodeBlock shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, false);
        shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(shortAccess);

        CodeBlock columnShortAccess = getShortColumnAccess(false, shortAccess);

        CodeBlock combined;
        if (!(columnAccess instanceof PackagePrivateAccess)) {
            combined = CodeBlock.of("$L$L$L", shortAccess, (isModelContainerAdapter ? "" : "."),
                    columnShortAccess);
        } else {
            combined = columnShortAccess;
        }
        return DefinitionUtils.getContentValuesStatement(columnShortAccess.toString(),
                combined.toString(),
                columnName, columnClassName, simpleColumnAccess,
                getForeignKeyColumnVariable(), null,
                foreignKeyColumnDefinition.tableDefinition.outputClassName).build();
    }

    public CodeBlock getPrimaryReferenceString() {
        CodeBlock shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, false);
        shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(shortAccess);
        CodeBlock columnShortAccess = getShortColumnAccess(false, shortAccess);
        CodeBlock combined;
        if (!(columnAccess instanceof PackagePrivateAccess)) {
            combined = CodeBlock.of("$L.$L.$L", ModelUtils.getVariable(),
                    shortAccess, columnShortAccess);
        } else {
            combined = columnShortAccess;
        }
        return combined;
    }

    CodeBlock getSQLiteStatementMethod(AtomicInteger index) {
        CodeBlock shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, true);
        shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(shortAccess);

        CodeBlock columnShortAccess = getShortColumnAccess(true, shortAccess);
        CodeBlock combined = shortAccess.toBuilder()
                .add(".")
                .add(columnShortAccess).build();
        return DefinitionUtils.getSQLiteStatementMethod(
                index, columnShortAccess.toString(), combined.toString(),
                columnClassName, simpleColumnAccess,
                getForeignKeyColumnVariable(), false, null).build();
    }

    CodeBlock getForeignKeyContainerMethod(String referenceFieldName, CodeBlock loadFromCursorBlock) {

        CodeBlock codeBlock = columnAccess
                .setColumnAccessString(columnClassName, foreignColumnName, foreignColumnName,
                        referenceFieldName, loadFromCursorBlock);
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.addStatement("$L", codeBlock);
        return codeBuilder.build();
    }

    private String getForeignKeyColumnVariable() {
        return ModelUtils.getVariable();
    }

    private CodeBlock getShortColumnAccess(boolean isSqliteMethod, CodeBlock shortAccess) {
        if (columnAccess instanceof PackagePrivateAccess) {
            return columnAccess.getColumnAccessString(columnClassName, foreignColumnName, "", ModelUtils.getVariable() + "." + shortAccess, isSqliteMethod);
        } else {
            return columnAccess.getShortAccessString(columnClassName, foreignColumnName, isSqliteMethod);
        }
    }

}
