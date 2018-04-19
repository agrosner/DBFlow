package com.raizlabs.dbflow5.runtime

import com.raizlabs.dbflow5.Runnable
import com.raizlabs.dbflow5.Thread
import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.structure.Model
import com.raizlabs.dbflow5.structure.save
import com.raizlabs.dbflow5.threading.ThreadConfigurator
import com.raizlabs.dbflow5.transaction.*

/**
 * Description: This queue will bulk save items added to it when it gets access to the DB. It should only exist as one entity.
 * It will save the [.MODEL_SAVE_SIZE] at a time or more only when the limit is reached. It will not
 */
class DBBatchSaveQueue
/**
 * Creates a new instance of this class to batch save DB object classes.
 */
internal constructor(private val databaseDefinition: DBFlowDatabase) : Thread("DBBatchSaveQueue") {

    private val threadConfigurator = ThreadConfigurator()

    /**
     * Tells how many items to save at a time. This can be set using [.setModelSaveSize]
     */
    private var modelSaveSize = MODEL_SAVE_SIZE

    /**
     * Sets the time we check periodically for leftover DB objects in our queue to save.
     */
    private var modelSaveCheckTime = MODEL_SAVE_CHECK_TIME.toLong()

    /**
     * The list of DB objects that we will save here
     */
    private val models = arrayListOf<Any>()

    /**
     * If true, this queue will quit.
     */
    private var isQuitting = false

    private var errorListener: Error<Unit>? = null
    private var successListener: Success<Unit>? = null
    private var emptyTransactionListener: Runnable? = null

    private val modelSaver = processModel<Any?> { model, _ ->
        (model as? Model)?.save(databaseDefinition) ?: model?.save(databaseDefinition)
    }

    private val successCallback: Success<Unit> = { transaction: Transaction<Unit>, result: Unit ->
        successListener?.invoke(transaction, result)
    }
    private val errorCallback: Error<Unit> = { transaction: Transaction<Unit>, error: Throwable ->
        errorListener?.invoke(transaction, error)
    }

    /**
     * Sets how many models to save at a time in this queue.
     * Increase it for larger batches, but slower recovery time.
     * Smaller the batch, the more time it takes to save overall.
     */
    fun setModelSaveSize(mModelSaveSize: Int) {
        this.modelSaveSize = mModelSaveSize
    }

    /**
     * Sets how long, in millis that this queue will check for leftover DB objects that have not been saved yet.
     * The default is [.MODEL_SAVE_CHECK_TIME]
     *
     * @param time The time, in millis that queue automatically checks for leftover DB objects in this queue.
     */
    fun setModelSaveCheckTime(time: Long) {
        this.modelSaveCheckTime = time
    }

    /**
     * Listener for errors in each batch [Transaction]. Called from the DBBatchSaveQueue thread.
     *
     * @param errorListener The listener to use.
     */
    fun setErrorListener(errorListener: Error<Unit>?) {
        this.errorListener = errorListener
    }

    /**
     * Listener for batch updates. Called from the DBBatchSaveQueue thread.
     *
     * @param successListener The listener to get notified when changes are successful.
     */
    fun setSuccessListener(successListener: Success<Unit>?) {
        this.successListener = successListener
    }

    /**
     * Listener for when there is no work done. Called from the DBBatchSaveQueue thread.
     *
     * @param emptyTransactionListener The listener to get notified when the save queue thread ran but was empty.
     */
    fun setEmptyTransactionListener(emptyTransactionListener: Runnable?) {
        this.emptyTransactionListener = emptyTransactionListener
    }

    override fun run() {
        super.run()
        threadConfigurator.configureForBackground()
        while (true) {
            var tmpModels = listOf<Any>()
            synchronized(models) {
                tmpModels = arrayListOf(models)
                models.clear()
            }
            if (tmpModels.isNotEmpty()) {
                val builder = ProcessModelTransaction.Builder(modelSaver)
                tmpModels.forEach { builder.add(it) }
                databaseDefinition.beginTransactionAsync(builder.build())
                    .success(successCallback)
                    .error(errorCallback)
                    .build()
                    .execute()
            } else {
                emptyTransactionListener?.run()
            }

            threadConfigurator.sleep(modelSaveCheckTime)

            if (isQuitting) {
                return
            }
        }
    }

    /**
     * Will cause the queue to wake from sleep and handle it's current list of items.
     */
    fun purgeQueue() {
        interrupt()
    }

    /**
     * Adds an object to this queue.
     */
    fun add(inModel: Any) {
        synchronized(models) {
            models.add(inModel)

            if (models.size > modelSaveSize) {
                interrupt()
            }
        }
    }

    /**
     * Adds a [Collection] of DB objects to this queue
     */
    fun addAll(list: MutableCollection<Any>) {
        synchronized(models) {
            models.addAll(list)

            if (models.size > modelSaveSize) {
                interrupt()
            }
        }
    }

    /**
     * Adds a [Collection] of class that extend Object to this queue
     */
    fun addAll2(list: Collection<Any>) {
        synchronized(models) {
            models.addAll(list)

            if (models.size > modelSaveSize) {
                interrupt()
            }
        }
    }

    /**
     * Removes a DB object from this queue before it is processed.
     */
    fun remove(outModel: Any) {
        synchronized(models) {
            models.remove(outModel)
        }
    }

    /**
     * Removes a [Collection] of DB object from this queue
     * before it is processed.
     */
    fun removeAll(outCollection: Collection<Any>) {
        synchronized(models) {
            models.removeAll(outCollection)
        }
    }

    /**
     * Removes a [Collection] of DB objects from this queue
     * before it is processed.
     */
    fun removeAll2(outCollection: Collection<*>) {
        synchronized(models) {
            models.removeAll(outCollection)
        }
    }

    /**
     * Quits this queue after it sleeps for the [.modelSaveCheckTime]
     */
    fun quit() {
        isQuitting = true
    }

    companion object {

        /**
         * Once the queue size reaches 50 or larger, the thread will be interrupted and we will batch save the models.
         */
        private const val MODEL_SAVE_SIZE = 50

        /**
         * The default time that it will awake the save queue thread to check if any models are still waiting to be saved
         */
        private const val MODEL_SAVE_CHECK_TIME = 30000
    }

}

