package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.annotation.Column;
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

        int columnType = columnDefinition.columnType;
        if (columnType == Column.FOREIGN_KEY) {
            if (columnDefinition.foreignKeyReferences == null || columnDefinition.foreignKeyReferences.length == 0) {
                success = false;
                processorManager.logError("Foreign Key for field %1s is missing it's references.", columnDefinition.columnFieldName);
            }

            if(columnDefinition.column.name().length() > 0) {
                success = false;
                processorManager.logError("Foreign Key cannot specify the column() field. " +
                        "Use a @ForeignKeyReference(columnName = {NAME} instead");
            }

        } else if (columnType == Column.NORMAL) {
            if (columnDefinition.foreignKeyReferences != null) {
                processorManager.logError("A non-foreign key field %1s defines references.", columnDefinition.columnFieldName);
                success = false;
            }
        } else if (columnType == Column.PRIMARY_KEY || columnType == Column.PRIMARY_KEY_AUTO_INCREMENT) {
            if (autoIncrementingPrimaryKey != null && columnType == Column.PRIMARY_KEY) {
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

            if (columnType == Column.PRIMARY_KEY_AUTO_INCREMENT) {
                if (autoIncrementingPrimaryKey == null) {
                    autoIncrementingPrimaryKey = columnDefinition;
                } else if (!autoIncrementingPrimaryKey.equals(columnDefinition)) {
                    processorManager.logError("Only one autoincrementing primary key is allowed on table");
                    success = false;
                }
            }
        }

        if (columnType != Column.FOREIGN_KEY && (columnDefinition.isModel || columnDefinition.isModelContainer)) {
            processorManager.logError("A Model or ModelContainer field must be a Column.FOREIGN_KEY_REFERENCE");
        }

        return success;
    }
}
