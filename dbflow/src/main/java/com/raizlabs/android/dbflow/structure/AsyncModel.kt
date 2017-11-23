package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.BaseAsyncObject
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction

import java.lang.ref.WeakReference

/**
 * Description: Called from a [BaseModel], this places the current [Model] interaction on the background.
 */
@Deprecated("use Transactions instead")
class AsyncModel<TModel : Any>(private val model: TModel) : BaseAsyncObject<AsyncModel<TModel>>(model.javaClass), Model {

    @Transient
    private var onModelChangedListener: WeakReference<OnModelChangedListener<TModel>>? = null

    private val modelAdapter: ModelAdapter<TModel> by lazy { FlowManager.getModelAdapter(model.javaClass) }

    /**
     * Listens for when this [Model] modification completes.
     */
    interface OnModelChangedListener<in T> {

        /**
         * Called when the change finishes on the [DefaultTransactionQueue]. This method is called on the UI thread.
         */
        fun onModelChanged(model: T)
    }

    /**
     * Call before [.save], [.delete], [.update], or [.insert].
     *
     * @param onModelChangedListener The listener to use for a corresponding call to a method.
     */
    fun withListener(onModelChangedListener: OnModelChangedListener<TModel>?): AsyncModel<TModel> {
        this.onModelChangedListener = WeakReference<OnModelChangedListener<TModel>>(onModelChangedListener)
        return this
    }

    override fun save(wrapper: DatabaseWrapper): Boolean = save()

    override fun save(): Boolean {
        executeTransaction(ProcessModelTransaction.Builder(
                object : ProcessModelTransaction.ProcessModel<TModel> {
                    override fun processModel(model: TModel, wrapper: DatabaseWrapper) {
                        modelAdapter.save(model, wrapper)
                    }
                }).add(model).build())
        return false
    }

    override fun delete(wrapper: DatabaseWrapper): Boolean = delete()

    override fun delete(): Boolean {
        executeTransaction(ProcessModelTransaction.Builder(
                object : ProcessModelTransaction.ProcessModel<TModel> {
                    override fun processModel(model: TModel, wrapper: DatabaseWrapper) {
                        modelAdapter.delete(model, wrapper)
                    }
                }).add(model).build())
        return false
    }

    override fun update(wrapper: DatabaseWrapper): Boolean = update()

    override fun update(): Boolean {
        executeTransaction(ProcessModelTransaction.Builder(
                object : ProcessModelTransaction.ProcessModel<TModel> {
                    override fun processModel(model: TModel, wrapper: DatabaseWrapper) {
                        modelAdapter.update(model, wrapper)
                    }
                }).add(model).build())
        return false
    }

    override fun insert(wrapper: DatabaseWrapper): Long = insert()

    override fun insert(): Long {
        executeTransaction(ProcessModelTransaction.Builder(
                object : ProcessModelTransaction.ProcessModel<TModel> {
                    override fun processModel(model: TModel, wrapper: DatabaseWrapper) {
                        modelAdapter.insert(model, wrapper)
                    }
                }).add(model).build())
        return Model.INVALID_ROW_ID
    }

    override fun load(wrapper: DatabaseWrapper) {
        load()
    }

    override fun load() {
        executeTransaction(ProcessModelTransaction.Builder(
                object : ProcessModelTransaction.ProcessModel<TModel> {
                    override fun processModel(model: TModel, wrapper: DatabaseWrapper) {
                        modelAdapter.load(model, wrapper)
                    }
                }).add(model).build())
    }

    override fun exists(wrapper: DatabaseWrapper): Boolean = exists()

    override fun exists(): Boolean = modelAdapter.exists(model)

    /**
     * @return Itself since it's already async.
     */
    @Suppress("UNCHECKED_CAST")
    override fun async(): AsyncModel<out Model> = this as AsyncModel<out Model>

    override fun onSuccess(transaction: Transaction) {
        onModelChangedListener?.get()?.onModelChanged(model)
    }
}
