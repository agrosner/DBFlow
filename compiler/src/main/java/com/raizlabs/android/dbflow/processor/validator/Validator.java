package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface Validator<ValidatorDefinition> {

    public boolean validate(ProcessorManager processorManager, ValidatorDefinition validatorDefinition);
}
