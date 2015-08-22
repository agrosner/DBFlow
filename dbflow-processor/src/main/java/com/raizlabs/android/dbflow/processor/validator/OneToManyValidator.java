package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.definition.OneToManyDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Description: Validates to ensure a {@link OneToManyDefinition} is correctly coded. Will throw failures on the {@link ProcessorManager}
 */
public class OneToManyValidator implements Validator<OneToManyDefinition> {
    @Override
    public boolean validate(ProcessorManager processorManager, OneToManyDefinition oneToManyDefinition) {
        boolean success = true;

        return success;
    }
}
