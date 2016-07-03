package com.raizlabs.android.dbflow.config;

import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: The base interface for interacting with all of the database and top-level data that's shared
 * between them.
 */
public abstract class DatabaseHolder {
    protected final Map<Class<? extends Model>, DatabaseDefinition> databaseDefinitionMap = new HashMap<>();
    protected final Map<String, DatabaseDefinition> databaseNameMap = new HashMap<>();
    protected final Map<Class<?>, DatabaseDefinition> databaseClassLookupMap = new HashMap<>();
    protected final Map<Class<?>, TypeConverter> typeConverters = new HashMap<>();

    /**
     * @param clazz The model value class to get a {@link com.raizlabs.android.dbflow.converter.TypeConverter}
     * @return Type converter for the specified model value.
     */
    public TypeConverter getTypeConverterForClass(Class<?> clazz) {
        return typeConverters.get(clazz);
    }

    /**
     * @param table The model class
     * @return The database that the table belongs in
     */
    public DatabaseDefinition getDatabaseForTable(Class<? extends Model> table) {
        return databaseDefinitionMap.get(table);
    }

    public DatabaseDefinition getDatabase(Class<?> databaseClass) {
        return databaseClassLookupMap.get(databaseClass);
    }

    /**
     * @param databaseName The name of the database to retrieve
     * @return The database that has the specified name
     */
    public DatabaseDefinition getDatabase(String databaseName) {
        return databaseNameMap.get(databaseName);
    }

    /**
     * Helper method used to store a database for the specified table.
     *
     * @param table              The model table
     * @param databaseDefinition The database definition
     */
    public void putDatabaseForTable(Class<? extends Model> table, DatabaseDefinition databaseDefinition) {
        databaseDefinitionMap.put(table, databaseDefinition);
        databaseNameMap.put(databaseDefinition.getDatabaseName(), databaseDefinition);
        databaseClassLookupMap.put(databaseDefinition.getAssociatedDatabaseClassFile(), databaseDefinition);
    }

    public void reset() {
        databaseDefinitionMap.clear();
        databaseNameMap.clear();
        databaseClassLookupMap.clear();
        typeConverters.clear();
    }

    public List<DatabaseDefinition> getDatabaseDefinitions() {
        return new ArrayList<>(databaseNameMap.values());
    }
}
