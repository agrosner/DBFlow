package com.raizlabs.dbflow5.transaction

import com.raizlabs.dbflow5.SafeVarargs
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.executeTransaction
import com.raizlabs.dbflow5.structure.Model

typealias ProcessFunction<T> = (T, DatabaseWrapper) -> Unit

/**
 * Description: Allows you to process a single or [List] of models in a transaction. You
 * can operate on a set of [Model] to [Model.save], [Model.update], etc.
 */
class ProcessModelTransaction<TModel>(
    private val models: List<TModel> = arrayListOf(),
    private val processListener: OnModelProcessListener<TModel>? = null,
    private val processModel: ProcessModel<TModel>,
    private val runProcessListenerOnSameThread: Boolean) : ITransaction<Unit> {


    /**
     * Description: Simple interface for acting on a model in a Transaction or list of [Model]
     */
    interface ProcessModel<in TModel> {

        /**
         * Called when processing models
         *
         * @param model   The model to process
         * @param wrapper
         */
        fun processModel(model: TModel, wrapper: DatabaseWrapper)
    }

    /**
     * Listener for providing callbacks as models are processed in this [ITransaction].
     *
     * @param <TModel> The model class.
    </TModel> */
    interface OnModelProcessListener<in TModel> {

        /**
         * Called when model has been operated on.
         *
         * @param current       The current index of items processed.
         * @param total         The total number of items to process.
         * @param modifiedModel The model previously modified.
         */
        fun onModelProcessed(current: Long, total: Long, modifiedModel: TModel)
    }

    internal constructor(builder: Builder<TModel>) : this(
        processListener = builder.processListener,
        models = builder.models,
        processModel = builder.processModel,
        runProcessListenerOnSameThread = builder.runProcessListenerOnSameThread
    )

    override fun execute(databaseWrapper: DatabaseWrapper) {
        val size = models.size
        for (i in 0 until size) {
            val model = models[i]
            processModel.processModel(model, databaseWrapper)

            if (processListener != null) {
                if (runProcessListenerOnSameThread) {
                    processListener.onModelProcessed(i.toLong(), size.toLong(), model)
                } else {
                    Transaction.transactionHandler.post {
                        processListener.onModelProcessed(i.toLong(), size.toLong(), model)
                    }
                }
            }
        }
    }

    /**
     * Makes it easy to build a [ProcessModelTransaction].
     *
     * @param <TModel>
     */
    class Builder<TModel> {

        internal val processModel: ProcessModel<TModel>
        internal var processListener: OnModelProcessListener<TModel>? = null
        internal var models: MutableList<TModel> = arrayListOf()
        internal var runProcessListenerOnSameThread: Boolean = false


        constructor(processModel: ProcessModel<TModel>) {
            this.processModel = processModel
        }

        /**
         * @param models       The models to process. This constructor creates a new [ArrayList]
         * from the [Collection] passed.
         * @param processModel The method call interface.
         */
        constructor(models: Collection<TModel>, processModel: ProcessModel<TModel>) {
            this.processModel = processModel
            this.models = models.toMutableList()
        }

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
         * @param processListener Allows you to listen for when models are processed to update UI,
         * this is called on the UI thread.
         */
        fun processListener(processListener: OnModelProcessListener<TModel>) = apply {
            this.processListener = processListener
        }

        /**
         * @param runProcessListenerOnSameThread Default is false. If true we return callback
         * on same calling thread, if false we push the callback
         * to the UI thread.
         */
        fun runProcessListenerOnSameThread(runProcessListenerOnSameThread: Boolean) = apply {
            this.runProcessListenerOnSameThread = runProcessListenerOnSameThread
        }

        /**
         * @return A new [ProcessModelTransaction]. Subsequent calls to this method produce
         * new instances.
         */
        fun build(): ProcessModelTransaction<TModel> = ProcessModelTransaction(this)
    }
}


/**
 * Enables a collection of T objects to easily operate on them within a synchronous database transaction.
 */
@Deprecated(message = "Use the coroutines awaitSave, awaitInsert, awaitDelete, awaitUpdate")
inline fun <reified T : Any> Collection<T>.processInTransaction(
    crossinline processFunction: ProcessFunction<T>) {
    databaseForTable<T>().executeTransaction { db -> forEach { processFunction(it, db) } }
}

inline fun <T> processModel(crossinline function: (T, DatabaseWrapper) -> Unit) = object : ProcessModelTransaction.ProcessModel<T> {
    override fun processModel(model: T, wrapper: DatabaseWrapper) = function(model, wrapper)
}

/**
 * Places the [Collection] of items on the [ITransactionQueue]. Use the [processFunction] to perform
 * an action on each individual [Model]. This happens on a non-UI thread.
 */
inline fun <reified T : Any> Collection<T>.processInTransactionAsync(
    crossinline processFunction: ProcessFunction<T>,
    noinline success: Success<Unit>? = null,
    noinline error: Error<Unit>? = null,
    processListener: ProcessModelTransaction.OnModelProcessListener<T>? = null) {
    val builder = this.processTransaction(processFunction)
    processListener?.let { builder.processListener(processListener) }
    databaseForTable<T>().beginTransactionAsync(builder.build())
        .success(success)
        .error(error)
        .execute()
}

inline fun <reified T : Any> Collection<T>.processTransaction(
    crossinline processFunction: ProcessFunction<T>): ProcessModelTransaction.Builder<T> =
    ProcessModelTransaction.Builder<T>(processModel { model, wrapper -> processFunction(model, wrapper) })
        .addAll(this)
