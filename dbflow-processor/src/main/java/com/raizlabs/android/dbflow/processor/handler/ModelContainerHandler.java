package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.validator.ModelContainerValidator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Description: Creates {@link com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition},
 * adds them to the {@link com.raizlabs.android.dbflow.processor.model.ProcessorManager}, and writes the source
 * files for them.
 */
public class ModelContainerHandler extends BaseContainerHandler<ModelContainer> {

    private final ModelContainerValidator validator = new ModelContainerValidator();

    @Override
    protected Class<ModelContainer> getAnnotationClass() {
        return ModelContainer.class;
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        if (element instanceof TypeElement) {
            ModelContainerDefinition modelContainerDefinition = new ModelContainerDefinition((TypeElement) element, processorManager);
            if (validator.validate(processorManager, modelContainerDefinition)) {
                processorManager.addModelContainerDefinition(modelContainerDefinition);
            }
        }
    }
}
