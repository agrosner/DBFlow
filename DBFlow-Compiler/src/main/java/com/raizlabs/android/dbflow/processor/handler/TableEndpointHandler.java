package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.validator.TableEndpointValidator;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;

/**
 * Description:
 */
public class TableEndpointHandler extends BaseContainerHandler<TableEndpoint> {

    private final TableEndpointValidator validator;

    public TableEndpointHandler() {
        validator = new TableEndpointValidator();
    }

    @Override
    protected Class<TableEndpoint> getAnnotationClass() {
        return TableEndpoint.class;
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {

        // top-level only
        if(element.getEnclosingElement() instanceof PackageElement) {
            TableEndpointDefinition tableEndpointDefinition = new TableEndpointDefinition(element, processorManager);
            if(validator.validate(processorManager, tableEndpointDefinition)) {
                processorManager.putTableEndpointForProvider(tableEndpointDefinition);
            }
        }
    }
}
