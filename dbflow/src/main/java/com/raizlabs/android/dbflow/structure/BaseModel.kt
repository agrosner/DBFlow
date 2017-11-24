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

    override fun DatabaseWrapper.load() {
        modelAdapter.load(this@BaseModel, this)
    }

    override fun DatabaseWrapper.save(): Boolean = modelAdapter.save(this@BaseModel, this)

    override fun DatabaseWrapper.delete(): Boolean = modelAdapter.delete(this@BaseModel, this)

    override fun DatabaseWrapper.update(): Boolean = modelAdapter.update(this@BaseModel, this)

    override fun DatabaseWrapper.insert(): Long = modelAdapter.insert(this@BaseModel, this)

    override fun DatabaseWrapper.exists(): Boolean = modelAdapter.exists(this@BaseModel, this)

}
