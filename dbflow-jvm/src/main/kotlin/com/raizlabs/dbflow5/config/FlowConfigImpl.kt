package com.raizlabs.dbflow5.config

import kotlin.reflect.KClass

/**
 * Description:
 */
actual class FlowConfig
    : InternalFlowConfig {

    constructor(
        databaseHolders: Set<KClass<out DatabaseHolder>> = setOf(),
        databaseConfigMap: Map<KClass<*>, DatabaseConfig> = mapOf(),
        openDatabasesOnInit: Boolean = false) : super(databaseHolders, databaseConfigMap, openDatabasesOnInit)

    constructor(builder: Builder) : super(builder)

    override fun merge(flowConfig: FlowConfig): FlowConfig = FlowConfig(
        databaseConfigMap = databaseConfigMap.entries
            .map { (key, value) ->
                key to (flowConfig.databaseConfigMap[key] ?: value)
            }.toMap(),
        databaseHolders = databaseHolders.plus(flowConfig.databaseHolders),
        openDatabasesOnInit = flowConfig.openDatabasesOnInit)

    actual class Builder : InternalFlowConfig.InternalBuilder() {

        fun addDatabaseHolder(databaseHolderClass: Class<out DatabaseHolder>): Builder =
            addDatabaseHolder(databaseHolderClass.kotlin) as Builder

        override fun build(): FlowConfig = FlowConfig(this)
    }

    companion object {

        fun builder(): FlowConfig.Builder = FlowConfig.Builder()
    }
}