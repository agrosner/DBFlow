package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.structure.AsyncModel
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

inline fun <reified T : Any> T.save(databaseWrapper: DatabaseWrapper = writableDatabaseForTable<T>()) = modelAdapter<T>().save(this, databaseWrapper)

inline fun <reified T : Any> T.insert(databaseWrapper: DatabaseWrapper = writableDatabaseForTable<T>()) = modelAdapter<T>().insert(this, databaseWrapper)

inline fun <reified T : Any> T.update(databaseWrapper: DatabaseWrapper = writableDatabaseForTable<T>()) = modelAdapter<T>().update(this, databaseWrapper)

inline fun <reified T : Any> T.delete(databaseWrapper: DatabaseWrapper = writableDatabaseForTable<T>()) = modelAdapter<T>().delete(this, databaseWrapper)

inline fun <reified T : Any> T.exists(databaseWrapper: DatabaseWrapper = writableDatabaseForTable<T>()) = modelAdapter<T>().exists(this, databaseWrapper)

inline fun <reified T : Any> T.async(databaseWrapper: DatabaseWrapper = writableDatabaseForTable<T>()) = AsyncModel(this)