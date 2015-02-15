package com.raizlabs.android.dbflow.config;

import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: The base interface for interacting with all of the database and top-level data that's shared
 * between them.
 */
public abstract class DatabaseHolder {

    static final Map<Class<? extends Model>, BaseDatabaseDefinition> mManagerMap = new HashMap<>();

    static final Map<String, BaseDatabaseDefinition> mManagerNameMap = new HashMap<>();

    protected static final Map<Class<?>, TypeConverter> mTypeConverters = new HashMap<>();

    /**
     * @param clazz The model value class to get a {@link com.raizlabs.android.dbflow.converter.TypeConverter}
     * @return Type converter for the specified model value.
     */
    public TypeConverter getTypeConverterForClass(Class<?> clazz) {
        return mTypeConverters.get(clazz);
    }

    /**
     * @param table The model class
     * @return The database that the table belongs in
     */
    public BaseDatabaseDefinition getDatabaseForTable(Class<? extends Model> table) {
        return mManagerMap.get(table);
    }

    /**
     * @param databaseName The name of the database to retrieve
     * @return The database that has the specified name
     */
    public BaseDatabaseDefinition getDatabase(String databaseName) {
        return mManagerNameMap.get(databaseName);
    }

    /**
     * Helper method used to store a database for the specified table.
     *
     * @param table                  The model table
     * @param baseDatabaseDefinition The database definition
     */
    void putDatabaseForTable(Class<? extends Model> table, BaseDatabaseDefinition baseDatabaseDefinition) {
        mManagerMap.put(table, baseDatabaseDefinition);
        mManagerNameMap.put(baseDatabaseDefinition.getDatabaseName(), baseDatabaseDefinition);
    }

}
