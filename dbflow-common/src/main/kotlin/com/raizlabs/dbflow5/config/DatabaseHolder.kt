package com.raizlabs.dbflow5.config

import kotlin.reflect.KClass
import com.raizlabs.dbflow5.converter.TypeConverter
import kotlin.jvm.JvmField

/**
 * Description: The base interface for interacting with all of the database and top-level data that's shared
 * between them.
 */
abstract class DatabaseHolder {

    val databaseDefinitionMap: MutableMap<KClass<*>, DBFlowDatabase> = hashMapOf()
    val databaseNameMap: MutableMap<String, DBFlowDatabase> = hashMapOf()
    val databaseClassLookupMap: MutableMap<KClass<*>, DBFlowDatabase> = hashMapOf()

    @JvmField
    val typeConverters: MutableMap<KClass<*>, TypeConverter<*, *>> = hashMapOf()

    val databaseDefinitions: List<DBFlowDatabase>
        get() = databaseNameMap.values.toList()

    /**
     * @param clazz The model value class to get a [TypeConverter]
     * @return Type converter for the specified model value.
     */
    fun getTypeConverterForClass(clazz: KClass<*>): TypeConverter<*, *>? = typeConverters[clazz]

    /**
     * @param table The model class
     * @return The database that the table belongs in
     */
    fun getDatabaseForTable(table: KClass<*>): DBFlowDatabase? = databaseDefinitionMap[table]

    fun getDatabase(databaseClass: KClass<*>): DBFlowDatabase? =
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
    fun putDatabaseForTable(table: KClass<*>, databaseDefinition: DBFlowDatabase) {
        databaseDefinitionMap[table] = databaseDefinition
        databaseNameMap[databaseDefinition.databaseName] = databaseDefinition
        databaseClassLookupMap[databaseDefinition.associatedDatabaseKClassFile] = databaseDefinition
    }

    fun reset() {
        databaseDefinitionMap.clear()
        databaseNameMap.clear()
        databaseClassLookupMap.clear()
        typeConverters.clear()
    }
}
