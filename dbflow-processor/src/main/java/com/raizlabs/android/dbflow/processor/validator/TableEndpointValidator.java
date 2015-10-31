package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Description:
 */
public class TableEndpointValidator implements Validator<TableEndpointDefinition> {

    @Override
    public boolean validate(ProcessorManager processorManager, TableEndpointDefinition tableEndpointDefinition) {
        boolean success = true;

        if (tableEndpointDefinition.contentUriDefinitions.isEmpty()) {
            processorManager.logError("A table endpoint %1s must supply at least one @ContentUri", tableEndpointDefinition.elementClassName);
            success = false;
        }

        if (tableEndpointDefinition.isTopLevel && (tableEndpointDefinition.contentProviderName == null
                || tableEndpointDefinition.contentProviderName.isEmpty())) {
            processorManager.logError("A top-level @TableEndpoint %1s must specify the @ContentProvider it belongs to", tableEndpointDefinition.elementClassName);
            success = false;
        }

        return success;
    }
}
