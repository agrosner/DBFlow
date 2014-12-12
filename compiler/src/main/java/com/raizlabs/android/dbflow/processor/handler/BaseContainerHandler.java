package com.raizlabs.android.dbflow.processor.handler;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class BaseContainerHandler<AnnotationClass extends Annotation> implements Handler {

    @Override
    public void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment) {
        final Set<Element> annotatedElements = Sets.newHashSet(roundEnvironment.getElementsAnnotatedWith(getAnnotationClass()));
        processElements(processorManager, (Set<Element>) annotatedElements);
        if (annotatedElements.size() > 0) {
            Iterator<? extends Element> iterator = annotatedElements.iterator();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                onProcessElement(processorManager, element);
            }
        }
    }

    protected abstract Class<AnnotationClass> getAnnotationClass();

    public void processElements(ProcessorManager processorManager, Set<Element> annotatedElements) {

    }

    protected abstract void onProcessElement(ProcessorManager processorManager,  Element element);
}
