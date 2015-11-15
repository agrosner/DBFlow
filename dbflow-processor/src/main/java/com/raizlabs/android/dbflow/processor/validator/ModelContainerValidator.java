package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Description:
 */
public class ModelContainerValidator implements Validator<ModelContainerDefinition> {
    @Override
    public boolean validate(ProcessorManager processorManager, ModelContainerDefinition modelContainerDefinition) {
        if (modelContainerDefinition.tableDefinition == null) {
            return false;
        }

        return true;
    }
}
