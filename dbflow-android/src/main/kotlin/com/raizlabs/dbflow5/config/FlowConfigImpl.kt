package com.raizlabs.dbflow5.config

import android.content.Context
import kotlin.reflect.KClass

/**
 * Description:
 */
actual class FlowConfig
    : InternalFlowConfig {

    val context: Context

    constructor(
        context: Context,
        databaseHolders: Set<KClass<out DatabaseHolder>> = setOf(),
        databaseConfigMap: Map<KClass<*>, DatabaseConfig> = mapOf(),
        openDatabasesOnInit: Boolean = false) : super(databaseHolders, databaseConfigMap, openDatabasesOnInit) {
        this.context = context.applicationContext
    }

    constructor(builder: Builder) : super(builder) {
        context = builder.context.applicationContext
    }

    override fun merge(flowConfig: FlowConfig): FlowConfig = FlowConfig(
        context = flowConfig.context,
        databaseConfigMap = databaseConfigMap.entries
            .map { (key, value) ->
                key to (flowConfig.databaseConfigMap[key] ?: value)
            }.toMap(),
        databaseHolders = databaseHolders.plus(flowConfig.databaseHolders),
        openDatabasesOnInit = flowConfig.openDatabasesOnInit)

    actual class Builder(internal val context: Context) : InternalFlowConfig.InternalBuilder() {

        fun addDatabaseHolder(databaseHolderClass: Class<out DatabaseHolder>): Builder =
            addDatabaseHolder(databaseHolderClass.kotlin) as Builder

        override fun build(): FlowConfig = FlowConfig(this)
    }

    companion object {

        fun builder(context: Context): FlowConfig.Builder = FlowConfig.Builder(context)
    }
}