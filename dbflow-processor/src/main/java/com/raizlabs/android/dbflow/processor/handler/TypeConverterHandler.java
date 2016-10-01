package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.TypeConverter;
import com.raizlabs.android.dbflow.converter.BooleanConverter;
import com.raizlabs.android.dbflow.converter.CalendarConverter;
import com.raizlabs.android.dbflow.converter.DateConverter;
import com.raizlabs.android.dbflow.converter.SqlDateConverter;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.validator.TypeConverterValidator;
import com.squareup.javapoet.ClassName;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Description: Handles {@link com.raizlabs.android.dbflow.annotation.TypeConverter} annotations,
 * adding default methods and adding them to the {@link com.raizlabs.android.dbflow.processor.model.ProcessorManager}
 */
public class TypeConverterHandler extends BaseContainerHandler<TypeConverter> {

    private static final TypeConverterValidator VALIDATOR = new TypeConverterValidator();

    private static final Class[] DEFAULT_TYPE_CONVERTERS = new Class[]{
            CalendarConverter.class,
            DateConverter.class,
            SqlDateConverter.class,
            BooleanConverter.class,
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
        if (element instanceof TypeElement) {
            ClassName className = ProcessorUtils.INSTANCE.fromTypeMirror(element.asType());
            TypeConverterDefinition converterDefinition = new TypeConverterDefinition(className,
                    element.asType(), processorManager);
            if (VALIDATOR.validate(processorManager, converterDefinition)) {
                processorManager.addTypeConverterDefinition(converterDefinition);
            }
        }
    }
}
