package com.raizlabs.android.dbflow.processor.validator

import com.raizlabs.android.dbflow.processor.model.ProcessorManager

/**
 * Description: the base interface for validating annotations.
 */
interface Validator<in ValidatorDefinition> {

    /**
     * @param processorManager    The manager
     * *
     * @param validatorDefinition The validator to use
     * *
     * @return true if validation passed, false if there was an error.
     */
    fun validate(processorManager: ProcessorManager, validatorDefinition: ValidatorDefinition): Boolean
}
