package com.grosner.dbflow.config;

import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface DatabaseHolder {

    public TypeConverter getTypeConverterForClass(Class<?> clazz);

    public BaseDatabaseDefinition getFlowManagerForTable(Class<?> clazz);

    public BaseDatabaseDefinition getFlowManager(String databaseName);

    void putFlowManagerForTable(Class<? extends Model> table, BaseDatabaseDefinition baseDatabaseDefinition);

}
