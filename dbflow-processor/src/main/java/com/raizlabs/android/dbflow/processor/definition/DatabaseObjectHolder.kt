package com.raizlabs.android.dbflow.processor.definition

import com.google.common.collect.Maps
import com.raizlabs.android.dbflow.processor.definition.*
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
    var providerMap: MutableMap<TypeName, ContentProviderDefinition> = Maps.newHashMap<TypeName, ContentProviderDefinition>()
}
