package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.EnumColumnAccess;
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;

/**
 * Description: Ensures the integrity of the annotation processor for columns.
 */
public class ColumnValidator implements Validator<ColumnDefinition> {

    private ColumnDefinition autoIncrementingPrimaryKey;

    @Override
    public boolean validate(ProcessorManager processorManager, ColumnDefinition columnDefinition) {

        boolean success = true;

        if (!StringUtils.isNullOrEmpty(columnDefinition.defaultValue)) {
            if (columnDefinition instanceof ForeignKeyColumnDefinition && (((ForeignKeyColumnDefinition) columnDefinition).isModelContainer
                || ((ForeignKeyColumnDefinition) columnDefinition).isModel)) {
                processorManager.logError(ColumnValidator.class, "Default values cannot be specified for model or model container fields");
            } else if (columnDefinition.elementTypeName.isPrimitive()) {
                processorManager.logWarning(ColumnValidator.class, "Primitive column types will not respect default values");
            }
        }

        if (columnDefinition.columnName == null || columnDefinition.columnName.isEmpty()) {
            success = false;
            processorManager.logError("Field %1s cannot have a null column name for column: %1s and type: %1s",
                columnDefinition.elementName, columnDefinition.columnName,
                columnDefinition.elementTypeName);
        }

        if (columnDefinition.columnAccess instanceof EnumColumnAccess) {
            if (columnDefinition.isPrimaryKey) {
                success = false;
                processorManager.logError("Enums cannot be primary keys. Column: %1s and type: %1s", columnDefinition.columnName,
                    columnDefinition.elementTypeName);
            } else if (columnDefinition instanceof ForeignKeyColumnDefinition) {
                success = false;
                processorManager.logError("Enums cannot be foreign keys. Column: %1s and type: %1s", columnDefinition.columnName,
                    columnDefinition.elementTypeName);
            }
        }

        if (columnDefinition instanceof ForeignKeyColumnDefinition) {
            if (columnDefinition.column != null && columnDefinition.column.name()
                .length() > 0) {
                success = false;
                processorManager.logError("Foreign Key %1s cannot specify the column() field. " +
                        "Use a @ForeignKeyReference(columnName = {NAME} instead. Column: %1s and type: %1s",
                    ((ForeignKeyColumnDefinition) columnDefinition).elementName, columnDefinition.columnName,
                    columnDefinition.elementTypeName);
            }

        } else {
            if (autoIncrementingPrimaryKey != null && columnDefinition.isPrimaryKey) {
                processorManager.logError("You cannot mix and match autoincrementing and composite primary keys.");
                success = false;
            }

            if (columnDefinition.isPrimaryKeyAutoIncrement) {
                if (autoIncrementingPrimaryKey == null) {
                    autoIncrementingPrimaryKey = columnDefinition;
                } else if (!autoIncrementingPrimaryKey.equals(columnDefinition)) {
                    processorManager.logError(
                        "Only one autoincrementing primary key is allowed on a table. Found Column: %1s and type: %1s",
                        columnDefinition.columnName, columnDefinition.elementTypeName);
                    success = false;
                }
            }
        }

        return success;
    }
}
