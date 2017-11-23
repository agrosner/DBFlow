package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.annotation.ColumnIgnore
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: The base implementation of [Model]. It is recommended to use this class as
 * the base for your [Model], but it is not required.
 */
@Deprecated("No subclass needed. Use extension methods instead")
open class BaseModel : Model {

    /**
     * @return The associated [ModelAdapter]. The [FlowManager]
     * may throw a [InvalidDBConfiguration] for this call if this class
     * is not associated with a table, so be careful when using this method.
     */
    @delegate:ColumnIgnore
    @delegate:Transient
    val modelAdapter: ModelAdapter<BaseModel> by lazy { FlowManager.getModelAdapter(javaClass) }

    /**
     * Specifies the Action that was taken when data changes
     */
    enum class Action {

        /**
         * The model called [Model.save]
         */
        SAVE,

        /**
         * The model called [Model.insert]
         */
        INSERT,

        /**
         * The model called [Model.update]
         */
        UPDATE,

        /**
         * The model called [Model.delete]
         */
        DELETE,

        /**
         * The model was changed. used in prior to [android.os.Build.VERSION_CODES.JELLY_BEAN_MR1]
         */
        CHANGE
    }

    override fun load() {
        modelAdapter.load(this)
    }

    override fun load(wrapper: DatabaseWrapper) {
        modelAdapter.load(this, wrapper)
    }

    override fun save(): Boolean = modelAdapter.save(this)


    override fun save(wrapper: DatabaseWrapper): Boolean =
            modelAdapter.save(this, wrapper)

    override fun delete(): Boolean = modelAdapter.delete(this)

    override fun delete(wrapper: DatabaseWrapper): Boolean =
            modelAdapter.delete(this, wrapper)

    override fun update(): Boolean = modelAdapter.update(this)

    override fun update(wrapper: DatabaseWrapper): Boolean =
            modelAdapter.update(this, wrapper)

    override fun insert(): Long = modelAdapter.insert(this)

    override fun insert(wrapper: DatabaseWrapper): Long =
            modelAdapter.insert(this, wrapper)

    override fun exists(): Boolean = modelAdapter.exists(this)

    override fun exists(wrapper: DatabaseWrapper): Boolean =
            modelAdapter.exists(this, wrapper)

    override fun async(): AsyncModel<out Model> = AsyncModel(this)


}
