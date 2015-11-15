package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Description:
 */
public class ModelViewValidator implements Validator<ModelViewDefinition> {
    @Override
    public boolean validate(ProcessorManager processorManager, ModelViewDefinition modelViewDefinition) {
        if (modelViewDefinition.viewTableName == null) {
            return false;
        }

        return true;
    }
}
