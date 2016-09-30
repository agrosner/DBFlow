package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.ListColumn;
import com.raizlabs.android.dbflow.processor.definition.BaseDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description: Represents a {@link List} of {@link TableDefinition}.
 * Must reference an existing table.
 *
 * @author Andrew Grosner (fuzz)
 */

public class ListColumnDefinition extends BaseDefinition {

    private BaseColumnAccess columnAccess;

    private boolean isPrivate;
    private boolean isPackagePrivate;

    private TableDefinition parentTableDefinition;

    private TableDefinition referencedTableDefinition;

    private ClassName referencedTableClassName;
    private TypeName listConverterTypeName;

    private boolean isFlat; // flag that if no table referenced, we flatten it to a singular column.
    private ColumnDefinition containedColumn; // if flat we create a column

    public ListColumnDefinition(Element element, ProcessorManager processorManager,
                                TableDefinition parentTableDefinition,
                                boolean isPackagePrivate, boolean isPackagePrivateNotInSamePackage) {
        super(element, processorManager);

        ListColumn listColumn = element.getAnnotation(ListColumn.class);
        if (listColumn != null) {

            this.parentTableDefinition = parentTableDefinition;

            if (elementTypeName instanceof ParameterizedTypeName) {
                List<TypeName> args = ((ParameterizedTypeName) elementTypeName).typeArguments;
                if (args.size() > 0) {
                    referencedTableClassName = ClassName.bestGuess(args.get(0).toString());
                }
            }

            try {
                listColumn.listConverter();
            } catch (MirroredTypeException mte) {
                listConverterTypeName = ClassName.get(mte.getTypeMirror());
            }

            isPrivate = element.getModifiers().contains(Modifier.PRIVATE);

            // find referenced child table definition
            TableDefinition tableDefinition = manager
                    .getTableDefinition(parentTableDefinition.databaseTypeName,
                            referencedTableClassName);

            if (tableDefinition == null) {
                isFlat = true;

                // TODO: create a column definition that will be used as holder for field.
            } else {
                referencedTableDefinition = tableDefinition;
            }

            if (isPrivate) {
                columnAccess = new PrivateColumnAccess(false);
            } else if (isPackagePrivateNotInSamePackage) {
                columnAccess = PackagePrivateAccess.from(processorManager, element,
                        referencedTableDefinition.databaseDefinition.classSeparator);
            } else {
                columnAccess = new SimpleColumnAccess();
            }
        }
    }

    public void writeLoad(MethodSpec.Builder builder) {
        CodeBlock accessCode = columnAccess.setColumnAccessString(elementTypeName,
                elementName, elementName, ModelUtils.getVariable(),
                CodeBlock.of("new $T().toList(cursor.getString(cursor.getColumnIndex($S)))", listConverterTypeName,
                        elementName));

        builder.addCode(accessCode.toBuilder().add(";\n").build());
    }

    public void writeStatement(MethodSpec.Builder builder) {

    }
}
