package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;

import java.io.IOException;

import javax.lang.model.element.Element;

/**
 * Description: Handles {@link com.raizlabs.android.dbflow.annotation.ModelView} annotations, writing
 * ModelViewAdapters, and adding them to the {@link com.raizlabs.android.dbflow.processor.model.ProcessorManager}
 */
public class ModelViewHandler extends BaseContainerHandler<ModelView> {

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        ModelViewDefinition modelViewDefinition = new ModelViewDefinition(processorManager, element);
        processorManager.addModelViewDefinition(modelViewDefinition);

        WriterUtils.writeBaseDefinition(modelViewDefinition, processorManager);
        try {
            modelViewDefinition.writeViewTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<ModelView> getAnnotationClass() {
        return ModelView.class;
    }
}
