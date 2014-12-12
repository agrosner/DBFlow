package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Ensures the integrity of the annotation processor for columns.
 */
public class ColumnValidator implements Validator<ColumnDefinition> {

    private ColumnDefinition autoIncrementingPrimaryKey;

    @Override
    public boolean validate(ProcessorManager processorManager, ColumnDefinition columnDefinition) {

        boolean success = true;

        if(columnDefinition.columnName == null || columnDefinition.columnName.isEmpty()) {
            success = false;
            processorManager.logError("Field %1s cannot have a null column name", columnDefinition.columnFieldName);
        }

        /*if(columnDefinition.isModelContainer) {
            success = false;
            processorManager.logError("Fields that are model containers are not currently supported");
        }*/

        int columnType = columnDefinition.columnType;
        if(columnType == Column.FOREIGN_KEY) {
            if(columnDefinition.foreignKeyReferences == null || columnDefinition.foreignKeyReferences.length == 0) {
                success = false;
                processorManager.logError("Foreign Key for field %1s is missing it's references.", columnDefinition.columnFieldName);
            }

        } else if (columnType == Column.NORMAL) {
            if(columnDefinition.foreignKeyReferences != null) {
                processorManager.logError("A non-foreign key field %1s defines references.", columnDefinition.columnFieldName);
                success = false;
            }
        } else if (columnType == Column.PRIMARY_KEY || columnType == Column.PRIMARY_KEY_AUTO_INCREMENT) {
            if(columnDefinition.foreignKeyReferences != null) {
                processorManager.logError("A non-foreign key field %1s defines references.", columnDefinition.columnFieldName);
                success = false;
            }

            if(columnType == Column.PRIMARY_KEY_AUTO_INCREMENT) {
                if(autoIncrementingPrimaryKey == null) {
                    autoIncrementingPrimaryKey = columnDefinition;
                } else if(!autoIncrementingPrimaryKey.equals(columnDefinition)) {
                    processorManager.logError("Only one autoincrementing primary key is allowed on table");
                    success = false;
                }
            } else {
                if(columnDefinition.isModel) {
                    processorManager.logError("Primary keys cannot be model objects");
                    success = false;
                }
            }
        }

        return success;
    }
}
