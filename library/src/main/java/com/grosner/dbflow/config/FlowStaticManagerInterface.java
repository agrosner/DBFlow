package com.grosner.dbflow.config;

import com.grosner.dbflow.converter.TypeConverter;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface FlowStaticManagerInterface {

    public TypeConverter getTypeConverterForClass(Class<?> clazz);

}
