package com.grosner.processor.handler;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.TypeConverter;
import com.grosner.dbflow.converter.CalendarConverter;
import com.grosner.dbflow.converter.DateConverter;
import com.grosner.dbflow.converter.SqlDateConverter;
import com.grosner.processor.definition.TypeConverterDefinition;
import com.grosner.processor.model.ProcessorManager;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Iterator;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class TypeConverterHandler {

    private static final Class[] DEFAULT_TYPE_CONVERTERS = new Class[] {
            CalendarConverter.class,
            DateConverter.class,
            SqlDateConverter.class,
    };

    public TypeConverterHandler(RoundEnvironment roundEnvironment, ProcessorManager processorManager) {

        final Set<Element> annotatedElements = Sets.newHashSet(roundEnvironment.getElementsAnnotatedWith(TypeConverter.class));

        for(Class clazz: DEFAULT_TYPE_CONVERTERS) {
            annotatedElements.add(processorManager.getElements().getTypeElement(clazz.getName()));
        }

        if(annotatedElements.size() > 0) {
            Iterator<? extends Element> iterator = annotatedElements.iterator();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                processorManager.addTypeConverterDefinition(new TypeConverterDefinition((TypeElement) element, processorManager));
            }
        }
    }
}
