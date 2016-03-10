package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.ExternalForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

public class ExternalForeignKeyColumnDefinition extends BaseForeignKeyColumnDefinition {
    public ClassName referencedDatabaseClassName;

    public ExternalForeignKeyColumnDefinition(ProcessorManager manager, TableDefinition tableDefinition, Element typeElement, boolean isPackagePrivate) {
        super(manager, tableDefinition, typeElement, isPackagePrivate);

        TypeElement referencedTableElement = null;
        ExternalForeignKey extForeignKey = typeElement.getAnnotation(ExternalForeignKey.class);

        try {
            extForeignKey.tableClass();
        } catch (MirroredTypeException mte) {
            referencedTableElement = manager.getElements().getTypeElement(mte.getTypeMirror().toString());
            referencedTableClassName = ClassName.get(referencedTableElement);
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
        } else if (elementTypeName instanceof ParameterizedTypeName) {
            isModelContainer = true;
        }

        if (referencedTableElement != null) {
            Table referencedTable = referencedTableElement.getAnnotation(Table.class);

            if (referencedTable != null) {
                try {
                    referencedTable.database();
                } catch (MirroredTypeException mte) {
                    referencedDatabaseClassName = ClassName.get(manager.getElements().getTypeElement(mte.getTypeMirror().toString()));
                }
            }
        }

        if (referencedDatabaseClassName == null) {
            // The table is in a database module. So, we have to rely on the user
            // giving us hints about the database and the foreign reference keys.
            try {
                extForeignKey.databaseClass();
            } catch (MirroredTypeException mte) {
                referencedDatabaseClassName = ClassName.get(manager.getElements().getTypeElement(mte.getTypeMirror().toString()));
            }
        }

        ForeignKeyReference[] references = extForeignKey.references();
        if (references.length == 0) {
            // TODO This should be an error. We need to have references!
            // no references specified we will delegate references call to post-evaluation
            needsReferences = true;
        } else {
            for (ForeignKeyReference reference : references) {
                ForeignKeyReferenceDefinition referenceDefinition =
                    new ForeignKeyReferenceDefinition(manager, elementName, reference, columnAccess, this);
                foreignKeyReferenceDefinitionList.add(referenceDefinition);
            }
        }
    }

    @Override
    protected void checkNeedsReferences() {
        if (needsReferences) {
            TableDefinition referencedTableDefinition = manager.getTableDefinition(referencedDatabaseClassName, referencedTableClassName);
            List<ColumnDefinition> primaryColumns = referencedTableDefinition.getPrimaryColumnDefinitions();

            for (ColumnDefinition primaryColumn : primaryColumns) {
                ForeignKeyReferenceDefinition foreignKeyReferenceDefinition =
                    new ForeignKeyReferenceDefinition (
                        manager,
                        elementName,
                        primaryColumn,
                        columnAccess,
                        this,
                        primaryColumns.size());

                foreignKeyReferenceDefinitionList.add(foreignKeyReferenceDefinition);
            }
            if (nonModelColumn) {
                columnName = foreignKeyReferenceDefinitionList.get(0).columnName;
            }

            needsReferences = false;
        }

        /*
        if (nonModelColumn && foreignKeyReferenceDefinitionList.size() == 1) {
            ForeignKeyReferenceDefinition foreignKeyReferenceDefinition = foreignKeyReferenceDefinitionList.get(0);
            columnName = foreignKeyReferenceDefinition.columnName;
        }*/
    }
}
