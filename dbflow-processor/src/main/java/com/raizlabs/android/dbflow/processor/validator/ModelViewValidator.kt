package com.raizlabs.android.dbflow.processor.validator

import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager

/**
 * Description:
 */
class ModelViewValidator : Validator<ModelViewDefinition> {

    override fun validate(processorManager: ProcessorManager, validatorDefinition: ModelViewDefinition): Boolean {
        return true
    }
}
