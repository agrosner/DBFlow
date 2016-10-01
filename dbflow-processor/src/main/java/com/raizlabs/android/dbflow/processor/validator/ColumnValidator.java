package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.EnumColumnAccess;
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.PrivateColumnAccess;
import com.raizlabs.android.dbflow.processor.definition.column.WrapperColumnAccess;
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

        // validate getter and setters.
        if (columnDefinition.getColumnAccess() instanceof PrivateColumnAccess ||
                columnDefinition.getColumnAccess() instanceof WrapperColumnAccess && ((WrapperColumnAccess) columnDefinition.getColumnAccess()).getExistingColumnAccess() instanceof PrivateColumnAccess) {
            PrivateColumnAccess privateColumnAccess = columnDefinition.getColumnAccess() instanceof PrivateColumnAccess ? ((PrivateColumnAccess) columnDefinition.getColumnAccess()) :
                    (PrivateColumnAccess) ((WrapperColumnAccess) columnDefinition.getColumnAccess()).getExistingColumnAccess();
            if (!columnDefinition.getTableDefinition().classElementLookUpMap.containsKey(privateColumnAccess.getGetterNameElement(columnDefinition.elementName))) {
                processorManager.logError(ColumnValidator.class, "Could not find getter for private element: " +
                                "\"%1s\" from table class: %1s. Consider adding a getter with name %1s or making it more accessible.",
                        columnDefinition.elementName, columnDefinition.getTableDefinition().elementName, privateColumnAccess.getGetterNameElement(columnDefinition.elementName));
                success = false;
            }
            if (!columnDefinition.getTableDefinition().classElementLookUpMap.containsKey(privateColumnAccess.getSetterNameElement(columnDefinition.elementName))) {
                processorManager.logError(ColumnValidator.class, "Could not find setter for private element: " +
                                "\"%1s\" from table class: %1s. Consider adding a setter with name %1s or making it more accessible.",
                        columnDefinition.elementName, columnDefinition.getTableDefinition().elementName, privateColumnAccess.getSetterNameElement(columnDefinition.elementName));
                success = false;
            }
        }

        if (!StringUtils.isNullOrEmpty(columnDefinition.getDefaultValue())) {
            if (columnDefinition instanceof ForeignKeyColumnDefinition &&
                    ((ForeignKeyColumnDefinition) columnDefinition).getIsModel()) {
                processorManager.logError(ColumnValidator.class, "Default values cannot be specified for model fields");
            } else if (columnDefinition.elementTypeName.isPrimitive()) {
                processorManager.logWarning(ColumnValidator.class, "Primitive column types will not respect default values");
            }
        }

        if (columnDefinition.getColumnName() == null || columnDefinition.getColumnName().isEmpty()) {
            success = false;
            processorManager.logError("Field %1s cannot have a null column name for column: %1s and type: %1s",
                    columnDefinition.elementName, columnDefinition.getColumnName(),
                    columnDefinition.elementTypeName);
        }

        if (columnDefinition.getColumnAccess() instanceof EnumColumnAccess) {
            if (columnDefinition.getIsPrimaryKey()) {
                success = false;
                processorManager.logError("Enums cannot be primary keys. Column: %1s and type: %1s", columnDefinition.getColumnName(),
                        columnDefinition.elementTypeName);
            } else if (columnDefinition instanceof ForeignKeyColumnDefinition) {
                success = false;
                processorManager.logError("Enums cannot be foreign keys. Column: %1s and type: %1s", columnDefinition.getColumnName(),
                        columnDefinition.elementTypeName);
            }
        }

        if (columnDefinition instanceof ForeignKeyColumnDefinition) {
            if (columnDefinition.getColumn() != null && columnDefinition.getColumn().name()
                    .length() > 0) {
                success = false;
                processorManager.logError("Foreign Key %1s cannot specify the column() field. " +
                                "Use a @ForeignKeyReference(columnName = {NAME} instead. Column: %1s and type: %1s",
                        ((ForeignKeyColumnDefinition) columnDefinition).elementName, columnDefinition.getColumnName(),
                        columnDefinition.elementTypeName);
            }

        } else {
            if (autoIncrementingPrimaryKey != null && columnDefinition.getIsPrimaryKey()) {
                processorManager.logError("You cannot mix and match autoincrementing and composite primary keys.");
                success = false;
            }

            if (columnDefinition.isPrimaryKeyAutoIncrement() || columnDefinition.getIsRowId()) {
                if (autoIncrementingPrimaryKey == null) {
                    autoIncrementingPrimaryKey = columnDefinition;
                } else if (!autoIncrementingPrimaryKey.equals(columnDefinition)) {
                    processorManager.logError(
                            "Only one autoincrementing primary key is allowed on a table. Found Column: %1s and type: %1s",
                            columnDefinition.getColumnName(), columnDefinition.elementTypeName);
                    success = false;
                }
            }
        }

        return success;
    }
}
