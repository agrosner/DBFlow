package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Description:
 */
public class ContentProviderValidator implements Validator<ContentProviderDefinition> {
    @Override
    public boolean validate(ProcessorManager processorManager, ContentProviderDefinition contentProviderDefinition) {
        boolean success = true;

        if (contentProviderDefinition.endpointDefinitions.isEmpty()) {
            processorManager.logError("The content provider %1s must have at least 1 @TableEndpoint associated with it",
                    contentProviderDefinition.element.getSimpleName());
            success = false;
        }

        return success;
    }
}
