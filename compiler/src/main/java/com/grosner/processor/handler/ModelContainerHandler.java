package com.grosner.processor.handler;

import com.grosner.dbflow.annotation.ModelContainer;
import com.grosner.processor.definition.ModelContainerDefinition;
import com.grosner.processor.model.ProcessorManager;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelContainerHandler {

    public ModelContainerHandler(RoundEnvironment roundEnvironment, ProcessorManager processorManager) {

        final Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(ModelContainer.class);

        if(annotatedElements.size() > 0) {

            Iterator<? extends Element> iterator = annotatedElements.iterator();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                final String packageName = processorManager.getElements()
                        .getPackageOf(element).toString();
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
    }
}
