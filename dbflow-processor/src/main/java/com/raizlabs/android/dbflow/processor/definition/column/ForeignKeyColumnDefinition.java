package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.SQLiteHelper;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description:
 */
public class ForeignKeyColumnDefinition extends BaseForeignKeyColumnDefinition {
    public ForeignKeyAction onDelete;
    public ForeignKeyAction onUpdate;

    public ForeignKeyColumnDefinition(ProcessorManager manager, TableDefinition tableDefinition, Element typeElement, boolean isPackagePrivate) {
        super(manager, tableDefinition, typeElement, isPackagePrivate);

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

    /**
     * If {@link ForeignKey} has no {@link ForeignKeyReference}s, we use the primary key the referenced
     * table. We do this post-evaluation so all of the {@link TableDefinition} can be generated.
     */
    protected void checkNeedsReferences() {
        TableDefinition referencedTableDefinition = manager.getTableDefinition(tableDefinition.databaseTypeName, referencedTableClassName);
        if (referencedTableDefinition == null) {
            System.err.println ("Must use @ExternalForeignKey");
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

    @Override
    public String getPropertyComparisonAccessStatement(boolean isModelContainerAdapter) {
        String statement = super.getPropertyComparisonAccessStatement(isModelContainerAdapter);

        if (isPrimaryKey) {
            if (isModelContainer) {
                TableDefinition referenced =
                    manager.getTableDefinition (
                        tableDefinition.databaseDefinition.elementTypeName,
                        referencedTableClassName);

                ForeignKeyReferenceDefinition referenceDefinition = getForeignKeyReferenceDefinitionList().get(0);
                // check for null and retrieve proper value
                String method = SQLiteHelper.getModelContainerMethod(referenceDefinition.columnClassName);

                if (method == null) {
                    method = "get";
                }

                statement = String
                    .format("%1s != null ? %1sValue(%1s.%1s.getContainerKey()) : null",
                        statement, method, referenced.outputClassName, referenceDefinition.foreignColumnName);
            }
            else {
                statement = String.format ("%s.%s", statement, foreignKeyReferenceDefinitionList.get(0).foreignColumnName);
            }
        }

        return statement;
    }
}
