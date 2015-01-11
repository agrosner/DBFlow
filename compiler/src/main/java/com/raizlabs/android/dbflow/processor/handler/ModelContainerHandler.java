package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.ContainerAdapter;
import com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Description: Creates {@link com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition},
 * adds them to the {@link com.raizlabs.android.dbflow.processor.model.ProcessorManager}, and writes the source
 * files for them.
 */
public class ModelContainerHandler extends BaseContainerHandler<ContainerAdapter> {

    @Override
    protected Class<ContainerAdapter> getAnnotationClass() {
        return ContainerAdapter.class;
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        ModelContainerDefinition modelContainerDefinition = new ModelContainerDefinition((TypeElement) element, processorManager);
        processorManager.addModelContainerDefinition(modelContainerDefinition);
        try {
            JavaWriter javaWriter = new JavaWriter(processorManager.getProcessingEnvironment().getFiler()
                    .createSourceFile(modelContainerDefinition.getSourceFileName()).openWriter());
            modelContainerDefinition.write(javaWriter);

            javaWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
