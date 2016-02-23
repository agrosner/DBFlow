package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description:
 */
public class ForeignKeyColumnDefinition extends BaseForeignKeyColumnDefinition {
    public ForeignKeyAction onDelete;
    public ForeignKeyAction onUpdate;

    public boolean isForeignKeyContainer;

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

        TypeElement element = manager.getElements().getTypeElement(
            manager.getTypeUtils().erasure(typeElement.asType()).toString());

        isForeignKeyContainer =
            isModelContainer &&
            ProcessorUtils.implementsClass(
                manager.getProcessingEnvironment(),
                ClassNames.FOREIGN_KEY_CONTAINER.toString(),
                element);

        saveForeignKeyModel = foreignKey.saveForeignKeyModel();

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

            if (nonModelColumn && foreignKeyReferenceDefinitionList.size() == 1) {
                ForeignKeyReferenceDefinition foreignKeyReferenceDefinition = foreignKeyReferenceDefinitionList.get(0);
                columnName = foreignKeyReferenceDefinition.columnName;
            }
        }
    }

    @Override
    public CodeBlock getForeignKeyContainerMethod(ClassName tableClassName) {
        if (nonModelColumn) {
            return super.getForeignKeyContainerMethod(tableClassName);
        } else {
            String access = columnAccess.getColumnAccessString(elementTypeName, containerKeyName, elementName,
                    ModelUtils.getVariable(false), false, false);
            CodeBlock.Builder builder = CodeBlock.builder();
            CodeBlock.Builder elseBuilder = CodeBlock.builder();
            builder.beginControlFlow("if ($L != null)", access);
            for (ForeignKeyReferenceDefinition referenceDefinition : foreignKeyReferenceDefinitionList) {
                builder.add(referenceDefinition.getForeignKeyContainerMethod(tableClassName));
                elseBuilder.addStatement("$L.putDefault($T.$L)", ModelUtils.getVariable(true), tableClassName, referenceDefinition.columnName);
            }
            builder.nextControlFlow("else");
            builder.add(elseBuilder.build());
            builder.endControlFlow();
            return builder.build();
        }
    }

    /**
     * If {@link ForeignKey} has no {@link ForeignKeyReference}s, we use the primary key the referenced
     * table. We do this post-evaluation so all of the {@link TableDefinition} can be generated.
     */
    @Override
    protected void checkNeedsReferences() {
        TableDefinition referencedTableDefinition = manager.getTableDefinition(tableDefinition.databaseTypeName, referencedTableClassName);
        if (referencedTableDefinition == null) {
            System.err.println ("Must use @ExternalForeignKey");
        } else {
            if (needsReferences) {
                List<ColumnDefinition> primaryColumns = referencedTableDefinition.getPrimaryColumnDefinitions();
                for (ColumnDefinition primaryColumn : primaryColumns) {
                    ForeignKeyReferenceDefinition foreignKeyReferenceDefinition =
                            new ForeignKeyReferenceDefinition(manager, elementName, primaryColumn,
                                    columnAccess, this, primaryColumns.size());
                    foreignKeyReferenceDefinitionList.add(foreignKeyReferenceDefinition);
                }
                if (nonModelColumn) {
                    columnName = foreignKeyReferenceDefinitionList.get(0).columnName;
                }
                needsReferences = false;
            }

            if (nonModelColumn && foreignKeyReferenceDefinitionList.size() == 1) {
                ForeignKeyReferenceDefinition foreignKeyReferenceDefinition = foreignKeyReferenceDefinitionList.get(0);
                columnName = foreignKeyReferenceDefinition.columnName;
            }
        }
    }
}
