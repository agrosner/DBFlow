package com.raizlabs.android.dbflow.config

import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver
import com.raizlabs.android.dbflow.structure.ModelAdapter

/**
 * Description: Represents certain table configuration options. This allows you to easily specify
 * certain configuration options for a table.
 */
class TableConfig<TModel>(val tableClass: Class<TModel>? = null,
                          val modelSaver: ModelSaver<TModel>? = null,
                          val singleModelLoader: SingleModelLoader<TModel>? = null,
                          val listModelLoader: ListModelLoader<TModel>? = null) {

    internal constructor(builder: Builder<TModel>) : this(
            tableClass = builder.tableClass,
            modelSaver = builder.modelAdapterModelSaver,
            singleModelLoader = builder.singleModelLoader,
            listModelLoader = builder.listModelLoader
    )

    /**
     * Table builder for java consumers. use [TableConfig] directly if calling from Kotlin.
     */
    class Builder<TModel>(internal val tableClass: Class<TModel>) {
        internal var modelAdapterModelSaver: ModelSaver<TModel>? = null
        internal var singleModelLoader: SingleModelLoader<TModel>? = null
        internal var listModelLoader: ListModelLoader<TModel>? = null

        /**
         * Define how the [ModelAdapter] saves data into the DB from its associated [TModel]. This
         * will override the default.
         */
        fun modelAdapterModelSaver(modelSaver: ModelSaver<TModel>) = apply {
            this.modelAdapterModelSaver = modelSaver
        }

        /**
         * Define how the table loads single models. This will override the default.
         */
        fun singleModelLoader(singleModelLoader: SingleModelLoader<TModel>) = apply {
            this.singleModelLoader = singleModelLoader
        }

        /**
         * Define how the table loads a [List] of items. This will override the default.
         */
        fun listModelLoader(listModelLoader: ListModelLoader<TModel>) = apply {
            this.listModelLoader = listModelLoader
        }

        /**
         * @return A new [TableConfig]. Subsequent calls to this method produce a new instance
         * of [TableConfig].
         */
        fun build(): TableConfig<*> = TableConfig(this)
    }

    companion object {

        fun <TModel> builder(tableClass: Class<TModel>): TableConfig.Builder<TModel> =
                TableConfig.Builder(tableClass)
    }
}
