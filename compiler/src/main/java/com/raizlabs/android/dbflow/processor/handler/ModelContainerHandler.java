package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Description: Creates {@link com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition},
 * adds them to the {@link com.raizlabs.android.dbflow.processor.model.ProcessorManager}, and writes the source
 * files for them.
 */
public class ModelContainerHandler extends BaseContainerHandler<ModelContainer> {

    @Override
    protected Class<ModelContainer> getAnnotationClass() {
        return ModelContainer.class;
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        ModelContainerDefinition modelContainerDefinition = new ModelContainerDefinition((TypeElement) element, processorManager);
        processorManager.addModelContainerDefinition(modelContainerDefinition);
        WriterUtils.writeBaseDefinition(modelContainerDefinition, processorManager);
    }
}
