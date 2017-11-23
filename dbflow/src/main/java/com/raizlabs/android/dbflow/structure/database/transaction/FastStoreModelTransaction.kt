package com.raizlabs.android.dbflow.structure.database.transaction

import com.raizlabs.android.dbflow.config.modelAdapter
import com.raizlabs.android.dbflow.structure.InternalAdapter
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import java.util.*

/**
 * Description: Similiar to [ProcessModelTransaction] in that it allows you to store a [List] of
 * [Model], except that it performs it as efficiently as possible. Also due to way the class operates,
 * only one kind of [TModel] is allowed.
 */
class FastStoreModelTransaction<TModel> internal constructor(builder: Builder<TModel>) : ITransaction {

    internal val models: List<TModel>?
    internal val processModelList: ProcessModelList<TModel>
    internal val internalAdapter: InternalAdapter<TModel>

    /**
     * Description: Simple interface for acting on a model in a Transaction or list of [Model]
     */
    internal interface ProcessModelList<TModel> {

        /**
         * Called when processing models
         *
         * @param modelList The model list to process
         */
        fun processModel(modelList: List<TModel>, adapter: InternalAdapter<TModel>,
                         wrapper: DatabaseWrapper)
    }

    init {
        models = builder.models
        processModelList = builder.processModelList
        internalAdapter = builder.internalAdapter
    }

    override fun execute(databaseWrapper: DatabaseWrapper) {
        if (models != null) {
            processModelList.processModel(models, internalAdapter, databaseWrapper)
        }
    }

    /**
     * Makes it easy to build a [ProcessModelTransaction].
     *
     * @param <TModel>
    </TModel> */
    class Builder<TModel> internal constructor(internal val processModelList: ProcessModelList<TModel>,
                                               internal val internalAdapter: InternalAdapter<TModel>) {
        internal var models: MutableList<TModel> = ArrayList()

        fun add(model: TModel) = apply {
            models.add(model)
        }

        /**
         * Adds all specified models to the [ArrayList].
         */
        @SafeVarargs
        fun addAll(vararg models: TModel) = apply {
            this.models.addAll(models.toList())
        }

        /**
         * Adds a [Collection] of [Model] to the existing [ArrayList].
         */
        fun addAll(models: Collection<TModel>?) = apply {
            if (models != null) {
                this.models.addAll(models)
            }
        }

        /**
         * @return A new [ProcessModelTransaction]. Subsequent calls to this method produce
         * new instances.
         */
        fun build(): FastStoreModelTransaction<TModel> = FastStoreModelTransaction(this)
    }

    companion object {

        @JvmStatic
        fun <TModel> saveBuilder(internalAdapter: InternalAdapter<TModel>): Builder<TModel> {
            return Builder(object : ProcessModelList<TModel> {
                override fun processModel(tModels: List<TModel>, adapter: InternalAdapter<TModel>, wrapper: DatabaseWrapper) {
                    adapter.saveAll(tModels, wrapper)
                }
            }, internalAdapter)
        }

        @JvmStatic
        fun <TModel> insertBuilder(internalAdapter: InternalAdapter<TModel>): Builder<TModel> {
            return Builder(object : ProcessModelList<TModel> {
                override fun processModel(tModels: List<TModel>, adapter: InternalAdapter<TModel>, wrapper: DatabaseWrapper) {
                    adapter.insertAll(tModels, wrapper)
                }
            }, internalAdapter)
        }

        @JvmStatic
        fun <TModel> updateBuilder(internalAdapter: InternalAdapter<TModel>): Builder<TModel> {
            return Builder(object : ProcessModelList<TModel> {
                override fun processModel(tModels: List<TModel>, adapter: InternalAdapter<TModel>, wrapper: DatabaseWrapper) {
                    adapter.updateAll(tModels, wrapper)
                }
            }, internalAdapter)
        }

        @JvmStatic
        fun <TModel> deleteBuilder(internalAdapter: InternalAdapter<TModel>): Builder<TModel> {
            return Builder(object : ProcessModelList<TModel> {
                override fun processModel(tModels: List<TModel>, adapter: InternalAdapter<TModel>, wrapper: DatabaseWrapper) {
                    adapter.deleteAll(tModels, wrapper)
                }
            }, internalAdapter)
        }
    }
}

inline fun <reified T : Any> Collection<T>.fastSave() = FastStoreModelTransaction.saveBuilder(modelAdapter<T>()).addAll(this)

inline fun <reified T : Any> Collection<T>.fastInsert() = FastStoreModelTransaction.insertBuilder(modelAdapter<T>()).addAll(this)

inline fun <reified T : Any> Collection<T>.fastUpdate() = FastStoreModelTransaction.updateBuilder(modelAdapter<T>()).addAll(this)