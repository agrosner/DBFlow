package com.raizlabs.android.dbflow.processor.definition

import com.squareup.javapoet.TypeName
import java.util.*

/**
 * Description: Provides overarching holder for [DatabaseDefinition], [TableDefinition],
 * and more. So we can safely use.
 */
class DatabaseObjectHolder {

    var databaseDefinition: DatabaseDefinition? = null
        set(databaseDefinition) {
            field = databaseDefinition
            field?.objectHolder = this
        }

    var tableDefinitionMap: MutableMap<TypeName, TableDefinition> = HashMap()
    var tableNameMap: MutableMap<String, TableDefinition> = HashMap()

    var queryModelDefinitionMap: MutableMap<TypeName, QueryModelDefinition> = HashMap()
    var modelViewDefinitionMap: MutableMap<TypeName, ModelViewDefinition> = HashMap()
    var manyToManyDefinitionMap: MutableMap<TypeName, MutableList<ManyToManyDefinition>> = HashMap()
    var providerMap = hashMapOf<TypeName, ContentProviderDefinition>()

    /**
     * Retrieve what database class they're trying to reference.
     */
    fun getMissingDBRefs(): List<String> {
        if (databaseDefinition == null) {
            val list = mutableListOf<String>()
            tableDefinitionMap.values.forEach {
                list += "Database ${it.databaseTypeName} not found for Table ${it.tableName}"
            }
            queryModelDefinitionMap.values.forEach {
                list += "Database ${it.databaseTypeName} not found for QueryModel ${it.elementName}"
            }
            modelViewDefinitionMap.values.forEach {
                list += "Database ${it.databaseTypeName} not found for ModelView ${it.elementName}"
            }
            providerMap.values.forEach {
                list += "Database ${it.databaseTypeName} not found for ContentProvider ${it.elementName}"
            }
            return list

        } else {
            return listOf()
        }
    }
}
