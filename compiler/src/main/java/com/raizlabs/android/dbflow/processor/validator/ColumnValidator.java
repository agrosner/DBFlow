package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Description: Ensures the integrity of the annotation processor for columns.
 */
public class ColumnValidator implements Validator<ColumnDefinition> {

    private ColumnDefinition autoIncrementingPrimaryKey;

    @Override
    public boolean validate(ProcessorManager processorManager, ColumnDefinition columnDefinition) {

        boolean success = true;

        if (columnDefinition.columnName == null || columnDefinition.columnName.isEmpty()) {
            success = false;
            processorManager.logError("Field %1s cannot have a null column name", columnDefinition.columnFieldName);
        }

        if(columnDefinition.columnFieldIsPrimitive) {

        }

        if (columnDefinition.isForeignKey) {
            ForeignKeyReference[] references = columnDefinition.foreignKeyReferences;
            if (references == null || references.length == 0) {
                success = false;
                processorManager.logError("Foreign Key for field %1s is missing it's references.",
                                          columnDefinition.columnFieldName);
            }

            if (columnDefinition.column.name().length() > 0) {
                success = false;
                processorManager.logError("Foreign Key %1s cannot specify the column() field. " +
                                          "Use a @ForeignKeyReference(columnName = {NAME} instead",
                                          columnDefinition.columnFieldName);
            }

            if (references != null && references.length > 1 &&
                (!columnDefinition.isModel && !columnDefinition.fieldIsModelContainer)) {
                success = false;
                processorManager.logError("IsModel:" + columnDefinition.isModel + " isModelContainer:" + columnDefinition.fieldIsModelContainer);
                processorManager.logError("Foreign key %1s cannot specify more than 1 reference for a non-model field.",
                                          columnDefinition.columnFieldName);
            }

        } else if (!columnDefinition.isPrimaryKey && !columnDefinition.isPrimaryKeyAutoIncrement) {
            if (columnDefinition.foreignKeyReferences != null) {
                processorManager.logError("A non-foreign key field %1s defines references.",
                                          columnDefinition.columnFieldName);
                success = false;
            }
        } else {
            if (autoIncrementingPrimaryKey != null && columnDefinition.isPrimaryKey) {
                processorManager.logError("You cannot mix and match autoincrementing and composite primary keys.");
                success = false;
            }
            if (columnDefinition.foreignKeyReferences != null) {
                processorManager.logError("A non-foreign key field %1s defines references.",
                                          columnDefinition.columnFieldName);
                success = false;
            } else if (columnDefinition.isModel) {
                processorManager.logError("Primary keys cannot be Model objects");
                success = false;
            }

            if (columnDefinition.isPrimaryKeyAutoIncrement) {
                if (autoIncrementingPrimaryKey == null) {
                    autoIncrementingPrimaryKey = columnDefinition;
                } else if (!autoIncrementingPrimaryKey.equals(columnDefinition)) {
                    processorManager.logError("Only one autoincrementing primary key is allowed on table");
                    success = false;
                }
            }
        }

        if (!columnDefinition.isForeignKey && (columnDefinition.isModel || columnDefinition.fieldIsModelContainer)) {
            processorManager.logError("A Model or ModelContainer field must be a Column.FOREIGN_KEY_REFERENCE");
        }

        return success;
    }
}
