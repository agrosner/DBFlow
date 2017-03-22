package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

fun <T : Any> T.save(databaseWrapper: DatabaseWrapper = FlowManager.getWritableDatabaseForTable(javaClass)) = FlowManager.getModelAdapter(javaClass).save(this, databaseWrapper)

fun <T : Any> T.insert(databaseWrapper: DatabaseWrapper = FlowManager.getWritableDatabaseForTable(javaClass)) = FlowManager.getModelAdapter(javaClass).insert(this, databaseWrapper)

fun <T : Any> T.update(databaseWrapper: DatabaseWrapper = FlowManager.getWritableDatabaseForTable(javaClass)) = FlowManager.getModelAdapter(javaClass).update(this, databaseWrapper)

fun <T : Any> T.delete(databaseWrapper: DatabaseWrapper = FlowManager.getWritableDatabaseForTable(javaClass)) = FlowManager.getModelAdapter(javaClass).delete(this, databaseWrapper)

fun <T : Any> T.exists(databaseWrapper: DatabaseWrapper = FlowManager.getWritableDatabaseForTable(javaClass)) = FlowManager.getModelAdapter(javaClass).exists(this, databaseWrapper)