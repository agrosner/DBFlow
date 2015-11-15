package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Description:
 */
public class DatabaseValidator implements Validator<DatabaseDefinition> {
    @Override
    public boolean validate(ProcessorManager processorManager, DatabaseDefinition databaseDefinition) {
        if (databaseDefinition.outputClassName == null) {
            return false;
        }

        return true;
    }
}
