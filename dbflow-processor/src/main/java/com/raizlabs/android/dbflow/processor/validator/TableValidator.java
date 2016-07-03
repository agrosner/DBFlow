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

    private ModelContainerValidator modelContainerValidator = new ModelContainerValidator();

    @Override
    public boolean validate(ProcessorManager processorManager, TableDefinition tableDefinition) {
        boolean success = true;

        if (tableDefinition.getColumnDefinitions() == null || tableDefinition.getColumnDefinitions().isEmpty()) {
            processorManager.logError(TableValidator.class, "Table %1s of %1s, %1s needs to define at least one column", tableDefinition.tableName,
                tableDefinition.elementClassName, tableDefinition.element.getClass());
            success = false;
        }

        boolean hasTwoKinds = ((tableDefinition.hasAutoIncrement || tableDefinition.hasRowID) && !tableDefinition.primaryColumnDefinitions.isEmpty());

        if (hasTwoKinds) {
            processorManager.logError(TableValidator.class, "Table %1s cannot mix and match autoincrement and composite primary keys", tableDefinition.tableName);
            success = false;
        }

        boolean hasPrimary = ((tableDefinition.hasAutoIncrement || tableDefinition.hasRowID) && tableDefinition.primaryColumnDefinitions.isEmpty()
            || !tableDefinition.hasAutoIncrement && !tableDefinition.hasRowID && !tableDefinition.primaryColumnDefinitions.isEmpty());

        if (!hasPrimary) {
            processorManager.logError(TableValidator.class, "Table %1s needs to define at least one primary key", tableDefinition.tableName);
            success = false;
        }

        if (!ProcessorUtils.implementsClass(processorManager.getProcessingEnvironment(), ClassNames.MODEL.toString(), (TypeElement) tableDefinition.element)) {
            //processorManager.logError(TableValidator.class, "The @Table annotation can only apply to a class that implements Model. Found: " + tableDefinition.element);
            //success = false;
        }

        if (tableDefinition.modelContainerDefinition != null) {
            if (!modelContainerValidator.validate(processorManager, tableDefinition.modelContainerDefinition)) {
                tableDefinition.modelContainerDefinition = null;
            }
        }

        return success;
    }
}
