package com.dbflow5.adapter.saveable

import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper

open class ListModelSaver<T : Any>(val modelSaver: ModelSaver<T>) {

    val modelAdapter = modelSaver.modelAdapter

    open suspend fun saveAll(
        tableCollection: Collection<T>,
        wrapper: DatabaseWrapper
    ): Result<Collection<T>> {
        return applyAndCount(
            tableCollection,
            modelAdapter.getSaveStatement(wrapper)
        ) { model, statement ->
            modelSaver.save(model, statement, wrapper)
        }
    }

    open suspend fun insertAll(
        tableCollection: Collection<T>,
        wrapper: DatabaseWrapper
    ): Result<Collection<T>> {
        return applyAndCount(
            tableCollection,
            modelAdapter.getInsertStatement(wrapper)
        ) { model, statement ->
            modelSaver.insert(model, statement, wrapper)
        }
    }

    open suspend fun updateAll(
        tableCollection: Collection<T>,
        wrapper: DatabaseWrapper
    ): Result<Collection<T>> {
        return applyAndCount(
            tableCollection,
            modelAdapter.getUpdateStatement(wrapper)
        ) { model, statement ->
            modelSaver.update(model, statement, wrapper)
        }
    }

    open suspend fun deleteAll(
        tableCollection: Collection<T>,
        wrapper: DatabaseWrapper
    ): Result<Collection<T>> {
        return applyAndCount(
            tableCollection,
            modelAdapter.getDeleteStatement(wrapper)
        ) { model, statement ->
            modelSaver.delete(model, statement, wrapper)
        }
    }

    private suspend inline fun applyAndCount(
        tableCollection: Collection<T>,
        databaseStatement: DatabaseStatement,
        crossinline fn: suspend (T, DatabaseStatement) -> Result<T>
    ): Result<Collection<T>> {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return Result.success(listOf())
        }

        val list = mutableListOf<T>()
        databaseStatement.use { statement ->
            tableCollection
                .mapNotNullTo(list) { fn(it, statement).getOrNull() }
        }
        return list.takeIf { it.isNotEmpty() }
            ?.let { Result.success(it) }
            ?: Result.failure(Throwable("All operations failed."))
    }
}
