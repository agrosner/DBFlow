package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.definition.DefinitionUtils;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
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

    public final BaseColumnAccess columnAccess;

    private final BaseColumnAccess tableColumnAccess;

    private final BaseColumnAccess simpleColumnAccess = new SimpleColumnAccess();

    public ForeignKeyReferenceDefinition(ProcessorManager manager, String foreignKeyFieldName, ForeignKeyReference foreignKeyReference, BaseColumnAccess tableColumnAccess) {
        this.manager = manager;
        this.foreignKeyFieldName = foreignKeyFieldName;
        this.tableColumnAccess = tableColumnAccess;

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
        if (isReferencedFieldPrivate) {
            columnAccess = new PrivateColumnAccess(foreignKeyReference);
        } else {
            columnAccess = new SimpleColumnAccess();
        }
    }

    CodeBlock getCreationStatement() {
        return DefinitionUtils.getCreationStatement(columnClassName, null, columnName).build();
    }

    CodeBlock getContentValuesStatement() {
        return DefinitionUtils.getContentValuesStatement(
                tableColumnAccess.getShortAccessString(foreignKeyFieldName) + "." + columnAccess.getShortAccessString(foreignColumnName),
                simpleColumnAccess,
                columnName, columnClassName).build();
    }

    CodeBlock getSQLiteStatementMethod(AtomicInteger index) {
        return DefinitionUtils.getSQLiteStatementMethod(
                index,
                tableColumnAccess.getShortAccessString(foreignKeyFieldName) + "." + columnAccess.getShortAccessString(foreignColumnName),
                simpleColumnAccess,
                columnClassName).build();
    }

    CodeBlock getLoadFromCursorMethod() {
        return DefinitionUtils.getLoadFromCursorMethod(
                tableColumnAccess.getShortAccessString(foreignKeyFieldName) + "." + columnAccess.getShortAccessString(foreignColumnName),
                simpleColumnAccess,
                columnClassName, columnName).build();
    }
}
