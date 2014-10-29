package com.grosner.processor.handler;

import com.grosner.dbflow.annotation.TypeConverter;
import com.grosner.processor.definition.TypeConverterDefinition;
import com.grosner.processor.model.ProcessorManager;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Iterator;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class TypeConverterHandler {

    public TypeConverterHandler(RoundEnvironment roundEnvironment, ProcessorManager processorManager) {

        final Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(TypeConverter.class);

        if(annotatedElements.size() > 0) {

            Iterator<? extends Element> iterator = annotatedElements.iterator();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                processorManager.addTypeConverterDefinition(new TypeConverterDefinition((TypeElement) element, processorManager));
            }
        }
    }
}
