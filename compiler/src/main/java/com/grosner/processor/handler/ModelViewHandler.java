package com.grosner.processor.handler;

import com.grosner.dbflow.annotation.ModelView;
import com.grosner.processor.model.ProcessorManager;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelViewHandler extends BaseContainerHandler<ModelView> {

    public ModelViewHandler(RoundEnvironment roundEnvironment, ProcessorManager processorManager) {
        super(ModelView.class, roundEnvironment, processorManager);
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, String packageName, Element element) {

    }
}
