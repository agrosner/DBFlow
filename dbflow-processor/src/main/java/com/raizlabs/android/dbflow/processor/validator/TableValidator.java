package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import javax.lang.model.element.TypeElement;

/**
 * Description: Validates proper usage of the {@link com.raizlabs.android.dbflow.annotation.Table}
 */
public class TableValidator implements Validator<TableDefinition> {
    @Override
    public boolean validate(ProcessorManager processorManager, TableDefinition tableDefinition) {
        boolean success = true;

        if(tableDefinition.getColumnDefinitions() == null || tableDefinition.getColumnDefinitions().isEmpty()) {
            processorManager.logError("Table %1s needs to define at least one column", tableDefinition.tableName);
            success = false;
        }

        boolean hasTwoKinds = (tableDefinition.hasAutoIncrement && !tableDefinition.primaryColumnDefinitions.isEmpty());

        if(hasTwoKinds) {
            processorManager.logError("Table %1s cannot mix and match autoincrement and composite primary keys", tableDefinition.tableName);
            success = false;
        }

        boolean hasPrimary = (tableDefinition.hasAutoIncrement && tableDefinition.primaryColumnDefinitions.isEmpty()
                || !tableDefinition.hasAutoIncrement && !tableDefinition.primaryColumnDefinitions.isEmpty());

        if(!hasPrimary) {
            processorManager.logError("Table %1s needs to define at least one primary key", tableDefinition.tableName);
            success = false;
        }

        if(!ProcessorUtils.implementsClass(processorManager.getProcessingEnvironment(), ClassNames.MODEL.toString(), (TypeElement) tableDefinition.element)) {
            processorManager.logError("The @Table annotation can only apply to a class that implements Model");
            success = false;
        }

        return success;
    }
}
