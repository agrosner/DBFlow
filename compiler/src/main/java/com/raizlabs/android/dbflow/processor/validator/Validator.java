package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Description: the base interface for validating annotations.
 */
public interface Validator<ValidatorDefinition> {

    /**
     * @param processorManager    The manager
     * @param validatorDefinition The validator to use
     * @return true if validation passed, false if there was an error.
     */
    public boolean validate(ProcessorManager processorManager, ValidatorDefinition validatorDefinition);
}
