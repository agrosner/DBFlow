package com.grosner.processor.validator;

import com.grosner.processor.Classes;
import com.grosner.processor.ProcessorUtils;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.model.ProcessorManager;

import javax.lang.model.element.TypeElement;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class TableValidator implements Validator<TableDefinition> {
    @Override
    public boolean validate(ProcessorManager processorManager, TableDefinition tableDefinition) {
        boolean success = true;

        if(tableDefinition.getColumnDefinitions() == null || tableDefinition.getColumnDefinitions().isEmpty()) {
            processorManager.logError("Table %1s needs to define at least one column", tableDefinition.tableName);
            success = false;
        }

        boolean hasTwoKinds = (tableDefinition.autoIncrementDefinition != null && !tableDefinition.primaryColumnDefinitions.isEmpty());

        if(hasTwoKinds) {
            processorManager.logError("Table %1s cannot mix and match autoincrement and composite primary keys", tableDefinition.tableName);
            success = false;
        }

        boolean hasPrimary = (tableDefinition.autoIncrementDefinition != null && tableDefinition.primaryColumnDefinitions.isEmpty()
                || tableDefinition.autoIncrementDefinition == null && !tableDefinition.primaryColumnDefinitions.isEmpty());

        if(!hasPrimary) {
            processorManager.logError("Table %1s needs to define at least one primary key", tableDefinition.tableName);
            success = false;
        }

        if(!ProcessorUtils.implementsClass(processorManager.getProcessingEnvironment(), Classes.MODEL, (TypeElement) tableDefinition.element)) {
            processorManager.logError("The @Table annotation can only apply to a class that implements Model");
            success = false;
        }

        return success;
    }
}
