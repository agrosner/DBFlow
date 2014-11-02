package com.grosner.dbflow.config;

import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface FlowManagerHolder {

    public TypeConverter getTypeConverterForClass(Class<?> clazz);

    public BaseFlowManager getFlowManagerForTable(Class<?> clazz);

    void putFlowManagerForTable(Class<? extends Model> table, BaseFlowManager baseFlowManager);

}
