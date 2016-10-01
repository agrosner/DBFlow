package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Description: Validates proper usage of the {@link com.raizlabs.android.dbflow.annotation.Table}
 */
public class TableValidator implements Validator<TableDefinition> {

    @Override
    public boolean validate(ProcessorManager processorManager, TableDefinition tableDefinition) {
        boolean success = true;

        if (tableDefinition.getColumnDefinitions() == null || tableDefinition.getColumnDefinitions().isEmpty()) {
            processorManager.logError(TableValidator.class, "Table %1s of %1s, %1s needs to define at least one column", tableDefinition.getTableName(),
                    tableDefinition.elementClassName, tableDefinition.element.getClass());
            success = false;
        }

        boolean hasTwoKinds = ((tableDefinition.getHasAutoIncrement() || tableDefinition.getHasRowID()) && !tableDefinition.get_primaryColumnDefinitions().isEmpty());

        if (hasTwoKinds) {
            processorManager.logError(TableValidator.class, "Table %1s cannot mix and match autoincrement and composite primary keys", tableDefinition.getTableName());
            success = false;
        }

        boolean hasPrimary = ((tableDefinition.getHasAutoIncrement() || tableDefinition.getHasRowID()) && tableDefinition.get_primaryColumnDefinitions().isEmpty()
                || !tableDefinition.getHasAutoIncrement() && !tableDefinition.getHasRowID() && !tableDefinition.get_primaryColumnDefinitions().isEmpty());

        if (!hasPrimary) {
            processorManager.logError(TableValidator.class, "Table %1s needs to define at least one primary key", tableDefinition.getTableName());
            success = false;
        }

        return success;
    }
}
