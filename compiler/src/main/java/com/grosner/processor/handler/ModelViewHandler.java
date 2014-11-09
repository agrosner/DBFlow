package com.grosner.processor.handler;

import com.grosner.dbflow.annotation.ModelView;
import com.grosner.processor.definition.ModelViewDefinition;
import com.grosner.processor.model.ProcessorManager;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelViewHandler extends BaseContainerHandler<ModelView> {

    @Override
    protected void onProcessElement(ProcessorManager processorManager, String packageName, Element element) {
        ModelViewDefinition modelViewDefinition = new ModelViewDefinition(processorManager, packageName, element);

        try {
            JavaWriter javaWriter = new JavaWriter(processorManager.getProcessingEnvironment()
                    .getFiler().createSourceFile(modelViewDefinition.getFQCN()).openWriter());
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
