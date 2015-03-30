package com.raizlabs.android.dbflow.processor.validator;

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

        if (columnDefinition.isForeignKey) {
            if (columnDefinition.foreignKeyReferences == null || columnDefinition.foreignKeyReferences.length == 0) {
                success = false;
                processorManager.logError("Foreign Key for field %1s is missing it's references.", columnDefinition.columnFieldName);
            }

            if (columnDefinition.column.name().length() > 0) {
                success = false;
                processorManager.logError("Foreign Key cannot specify the column() field. " +
                        "Use a @ForeignKeyReference(columnName = {NAME} instead");
            }

        } else if (!columnDefinition.isForeignKey && !columnDefinition.isPrimaryKey && !columnDefinition.isPrimaryKeyAutoIncrement) {
            if (columnDefinition.foreignKeyReferences != null) {
                processorManager.logError("A non-foreign key field %1s defines references.", columnDefinition.columnFieldName);
                success = false;
            }
        } else if (columnDefinition.isPrimaryKey || columnDefinition.isPrimaryKeyAutoIncrement) {
            if (autoIncrementingPrimaryKey != null && columnDefinition.isPrimaryKey) {
                processorManager.logError("You cannot mix and match autoincrementing and composite primary keys.");
                success = false;
            }
            if (columnDefinition.foreignKeyReferences != null) {
                processorManager.logError("A non-foreign key field %1s defines references.", columnDefinition.columnFieldName);
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

        if (!columnDefinition.isForeignKey && (columnDefinition.isModel || columnDefinition.isModelContainer)) {
            processorManager.logError("A Model or ModelContainer field must be a Column.FOREIGN_KEY_REFERENCE");
        }

        return success;
    }
}
