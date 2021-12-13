package com.dbflow5.config

import com.dbflow5.converter.TypeConverter
import kotlin.reflect.KClass

interface MutableHolder {
    fun put(databaseDefinition: DBFlowDatabase, table: KClass<*>)
}

/**
 * Description: The base interface for interacting with all of the database and top-level data that's shared
 * between them.
 */
abstract class DatabaseHolder : MutableHolder {

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
    fun getTypeConverterForClass(clazz: Class<*>): TypeConverter<*, *>? =
        typeConverters[clazz.kotlin]

    /**
     * @param table The model class
     * @return The database that the table belongs in
     */
    fun getDatabaseForTable(table: Class<*>): DBFlowDatabase? = databaseDefinitionMap[table.kotlin]

    fun getDatabase(databaseClass: Class<*>): DBFlowDatabase? =
        databaseClassLookupMap[databaseClass.kotlin]

    /**
     * @param databaseName The name of the database to retrieve
     * @return The database that has the specified name
     */
    fun getDatabase(databaseName: String): DBFlowDatabase? = databaseNameMap[databaseName]

    override fun put(databaseDefinition: DBFlowDatabase, table: KClass<*>) {
        databaseDefinitionMap[table] = databaseDefinition
        databaseNameMap[databaseDefinition.databaseName] = databaseDefinition
        databaseClassLookupMap[databaseDefinition.associatedDatabaseClassFile.kotlin] =
            databaseDefinition
    }

    /**
     * Helper method used to store a database for the specified table.
     *
     * @param table              The model table
     * @param databaseDefinition The database definition
     */
    fun putDatabaseForTable(table: Class<*>, databaseDefinition: DBFlowDatabase) {
        put(databaseDefinition, table.kotlin)
    }

    fun putTypeConverter(type: Class<*>, typeConverter: TypeConverter<*, *>) {
        typeConverters[type.kotlin] = typeConverter
    }

    fun reset() {
        databaseDefinitionMap.clear()
        databaseNameMap.clear()
        databaseClassLookupMap.clear()
        typeConverters.clear()
    }
}
