package com.raizlabs.android.dbflow.processor.validator

import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager

/**
 * Description:
 */
class ContentProviderValidator : Validator<ContentProviderDefinition> {
    override fun validate(processorManager: ProcessorManager,
                          validatorDefinition: ContentProviderDefinition): Boolean {
        var success = true

        if (validatorDefinition.endpointDefinitions.isEmpty()) {
            processorManager.logError("The content provider %1s must have at least 1 @TableEndpoint associated with it",
                    validatorDefinition.element.simpleName)
            success = false
        }

        return success
    }
}
