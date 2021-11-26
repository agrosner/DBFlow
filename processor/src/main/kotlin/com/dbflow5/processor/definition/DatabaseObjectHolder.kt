package com.dbflow5.processor.definition

import com.squareup.javapoet.TypeName

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

    var tableDefinitionMap: MutableMap<TypeName, TableDefinition> = hashMapOf()
    val tableNameMap: MutableMap<String, TableDefinition> = hashMapOf()

    val queryModelDefinitionMap: MutableMap<TypeName, QueryModelDefinition> = hashMapOf()
    var modelViewDefinitionMap: MutableMap<TypeName, ModelViewDefinition> = hashMapOf()
    val manyToManyDefinitionMap: MutableMap<TypeName, MutableList<ManyToManyDefinition>> =
        hashMapOf()

    /**
     * Retrieve what database class they're trying to reference.
     */
    fun getMissingDBRefs(): List<String> {
        if (databaseDefinition == null) {
            val list = mutableListOf<String>()
            tableDefinitionMap.values.forEach {
                list += "Database ${it.associationalBehavior.databaseTypeName} not found for Table ${it.associationalBehavior.name}"
            }
            queryModelDefinitionMap.values.forEach {
                list += "Database ${it.associationalBehavior.databaseTypeName} not found for QueryModel ${it.elementName}"
            }
            modelViewDefinitionMap.values.forEach {
                list += "Database ${it.associationalBehavior.databaseTypeName} not found for ModelView ${it.elementName}"
            }
            return list

        } else {
            return listOf()
        }
    }
}
