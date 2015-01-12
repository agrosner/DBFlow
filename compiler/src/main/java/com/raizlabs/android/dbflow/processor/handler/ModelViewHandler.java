package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Element;
import java.io.IOException;

/**
 * Description: Handles {@link com.raizlabs.android.dbflow.annotation.ModelView} annotations, writing
 * ModelViewAdapters, and adding them to the {@link com.raizlabs.android.dbflow.processor.model.ProcessorManager}
 */
public class ModelViewHandler extends BaseContainerHandler<ModelView> {

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        ModelViewDefinition modelViewDefinition = new ModelViewDefinition(processorManager, element);

        try {
            JavaWriter javaWriter = new JavaWriter(processorManager.getProcessingEnvironment()
                    .getFiler().createSourceFile(modelViewDefinition.getSourceFileName()).openWriter());
            modelViewDefinition.write(javaWriter);

            processorManager.addModelViewDefinition(modelViewDefinition);

            javaWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<ModelView> getAnnotationClass() {
        return ModelView.class;
    }
}
