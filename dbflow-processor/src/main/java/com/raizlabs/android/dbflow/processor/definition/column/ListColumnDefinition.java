package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ListColumn;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description: Represents a {@link List} of {@link TableDefinition}.
 * Must reference an existing table.
 *
 * @author Andrew Grosner (fuzz)
 */

public class ListColumnDefinition extends ColumnDefinition {

    private TableDefinition referencedTableDefinition;

    private ClassName referencedTableClassName;
    private TypeName listConverterTypeName;

    private boolean isFlat; // flag that if no table referenced, we flatten it to a singular column.

    public ListColumnDefinition(ProcessorManager processorManager, Element element, BaseTableDefinition baseTableDefinition,
                                boolean isPackagePrivate, Column column, PrimaryKey primaryKey) {
        super(processorManager, element, baseTableDefinition, isPackagePrivate, column, primaryKey);

        ListColumn listColumn = element.getAnnotation(ListColumn.class);
        if (listColumn != null) {

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

            // find referenced child table definition
            TableDefinition tableDefinition = manager
                    .getTableDefinition(((TableDefinition) baseTableDefinition)
                                    .databaseTypeName,
                            referencedTableClassName);

            if (tableDefinition == null) {
                isFlat = true;

                // TODO: create a column definition that will be used as holder for field.
            } else {
                referencedTableDefinition = tableDefinition;
            }
        }
    }
}
