package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.converter.TypeConverter

/**
 * Description: The base interface for interacting with all of the database and top-level data that's shared
 * between them.
 */
abstract class DatabaseHolder {

    val databaseDefinitionMap: MutableMap<Class<*>, DBFlowDatabase> = hashMapOf()
    val databaseNameMap: MutableMap<String, DBFlowDatabase> = hashMapOf()
    val databaseClassLookupMap: MutableMap<Class<*>, DBFlowDatabase> = hashMapOf()

    @JvmField
    val typeConverters: MutableMap<Class<*>, TypeConverter<*, *>> = hashMapOf()

    val databaseDefinitions: List<DBFlowDatabase>
        get() = databaseNameMap.values.toList()

    /**
     * @param clazz The model value class to get a [TypeConverter]
     * @return Type converter for the specified model value.
     */
    fun getTypeConverterForClass(clazz: Class<*>): TypeConverter<*, *>? = typeConverters[clazz]

    /**
     * @param table The model class
     * @return The database that the table belongs in
     */
    fun getDatabaseForTable(table: Class<*>): DBFlowDatabase? = databaseDefinitionMap[table]

    fun getDatabase(databaseClass: Class<*>): DBFlowDatabase? =
        databaseClassLookupMap[databaseClass]

    /**
     * @param databaseName The name of the database to retrieve
     * @return The database that has the specified name
     */
    fun getDatabase(databaseName: String): DBFlowDatabase? = databaseNameMap[databaseName]

    /**
     * Helper method used to store a database for the specified table.
     *
     * @param table              The model table
     * @param databaseDefinition The database definition
     */
    fun putDatabaseForTable(table: Class<*>, databaseDefinition: DBFlowDatabase) {
        databaseDefinitionMap.put(table, databaseDefinition)
        databaseNameMap.put(databaseDefinition.databaseName, databaseDefinition)
        databaseClassLookupMap.put(databaseDefinition.associatedDatabaseClassFile, databaseDefinition)
    }

    fun reset() {
        databaseDefinitionMap.clear()
        databaseNameMap.clear()
        databaseClassLookupMap.clear()
        typeConverters.clear()
    }
}
