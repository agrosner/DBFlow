package com.raizlabs.android.dbflow.config;

import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface DatabaseHolder {

    public TypeConverter getTypeConverterForClass(Class<?> clazz);

    public BaseDatabaseDefinition getDatabaseForTable(Class<?> clazz);

    public BaseDatabaseDefinition getDatabase(String databaseName);

    void putDatabaseForTable(Class<? extends Model> table, BaseDatabaseDefinition baseDatabaseDefinition);

}
