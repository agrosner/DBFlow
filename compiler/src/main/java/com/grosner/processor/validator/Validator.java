package com.grosner.processor.validator;

import com.grosner.processor.model.ProcessorManager;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface Validator<ValidatorDefinition> {

    public boolean validate(ProcessorManager processorManager, ValidatorDefinition validatorDefinition);
}
