package com.dbflow5.adapter.saveable

import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper

open class ListModelSaver<T : Any>(val modelSaver: ModelSaver<T>) {

    val modelAdapter = modelSaver.modelAdapter

    @Synchronized
    open fun saveAll(
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

    @Synchronized
    open fun insertAll(
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

    @Synchronized
    open fun updateAll(
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

    @Synchronized
    open fun deleteAll(
        tableCollection: Collection<T>,
        wrapper: DatabaseWrapper
    ): Result<Collection<T>> {
        return applyAndCount(
            tableCollection,
            modelAdapter.getDeleteStatement(wrapper)
        ) { model, statement ->
            modelSaver.delete(wrapper, model, statement)
        }
    }

    private inline fun applyAndCount(
        tableCollection: Collection<T>,
        databaseStatement: DatabaseStatement,
        crossinline fn: (T, DatabaseStatement) -> Result<T>
    ): Result<Collection<T>> {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return Result.success(listOf())
        }

        val list = mutableListOf<T>()
        databaseStatement.use { statement ->
            list.addAll(tableCollection
                .asSequence()
                .mapNotNull { fn(it, statement).getOrNull() })
        }
        return list.takeIf { it.isNotEmpty() }
            ?.let { Result.success(it) }
            ?: Result.failure(Throwable("All operations failed."))
    }
}
