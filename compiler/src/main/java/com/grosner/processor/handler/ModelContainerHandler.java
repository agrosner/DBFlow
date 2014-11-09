package com.grosner.processor.handler;

import com.grosner.dbflow.annotation.ContainerAdapter;
import com.grosner.processor.definition.ModelContainerDefinition;
import com.grosner.processor.model.ProcessorManager;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelContainerHandler extends BaseContainerHandler<ContainerAdapter> {

    @Override
    protected Class<ContainerAdapter> getAnnotationClass() {
        return ContainerAdapter.class;
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, String packageName, Element element) {
        ModelContainerDefinition modelContainerDefinition = new ModelContainerDefinition(packageName, (TypeElement) element, processorManager);
        processorManager.addModelContainerDefinition(modelContainerDefinition);
        try {
            JavaWriter javaWriter = new JavaWriter(processorManager.getProcessingEnvironment().getFiler()
                    .createSourceFile(modelContainerDefinition.getFQCN()).openWriter());
            modelContainerDefinition.write(javaWriter);

            javaWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
