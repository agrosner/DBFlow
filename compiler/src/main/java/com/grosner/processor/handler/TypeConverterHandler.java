package com.grosner.processor.handler;

import com.grosner.dbflow.annotation.TypeConverter;
import com.grosner.dbflow.converter.CalendarConverter;
import com.grosner.dbflow.converter.DateConverter;
import com.grosner.dbflow.converter.SqlDateConverter;
import com.grosner.processor.definition.TypeConverterDefinition;
import com.grosner.processor.model.ProcessorManager;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class TypeConverterHandler extends BaseContainerHandler<TypeConverter> {

    private static final Class[] DEFAULT_TYPE_CONVERTERS = new Class[]{
            CalendarConverter.class,
            DateConverter.class,
            SqlDateConverter.class,
    };

    @Override
    protected Class<TypeConverter> getAnnotationClass() {
        return TypeConverter.class;
    }

    @Override
    public void processElements(ProcessorManager processorManager, Set<Element> annotatedElements) {
        for (Class clazz : DEFAULT_TYPE_CONVERTERS) {
            annotatedElements.add(processorManager.getElements().getTypeElement(clazz.getName()));
        }
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        processorManager.addTypeConverterDefinition(new TypeConverterDefinition((TypeElement) element, processorManager));
    }
}
