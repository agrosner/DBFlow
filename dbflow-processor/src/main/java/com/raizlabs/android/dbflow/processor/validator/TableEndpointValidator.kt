package com.raizlabs.android.dbflow.processor.validator

import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager

/**
 * Description:
 */
class TableEndpointValidator : Validator<TableEndpointDefinition> {

    override fun validate(processorManager: ProcessorManager, validatorDefinition: TableEndpointDefinition): Boolean {
        var success = true

        if (validatorDefinition.contentUriDefinitions.isEmpty()) {
            processorManager.logError("A table endpoint %1s must supply at least one @ContentUri", validatorDefinition.elementClassName)
            success = false
        }

        return success
    }
}
