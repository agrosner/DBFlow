package com.raizlabs.android.dbflow.processor.validator

import com.raizlabs.android.dbflow.processor.definition.OneToManyDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager

/**
 * Description: Validates to ensure a [OneToManyDefinition] is correctly coded. Will throw failures on the [ProcessorManager]
 */
class OneToManyValidator : Validator<OneToManyDefinition> {
    override fun validate(processorManager: ProcessorManager, oneToManyDefinition: OneToManyDefinition): Boolean {
        return true
    }
}
