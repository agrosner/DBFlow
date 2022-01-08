package com.dbflow5.config

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.queriable.ListModelLoader
import com.dbflow5.adapter.queriable.SingleModelLoader
import com.dbflow5.adapter.saveable.ModelSaver
import kotlin.reflect.KClass

/**
 * Description: Represents certain table configuration options. This allows you to easily specify
 * certain configuration options for a table.
 */
class TableConfig<T : Any>(
    val tableClass: KClass<T>,
    val modelSaver: ModelSaver<T>? = null,
    val singleModelLoader: SingleModelLoader<T>? = null,
    val listModelLoader: ListModelLoader<T>? = null
) {

    internal constructor(builder: Builder<T>) : this(
        tableClass = builder.tableClass,
        modelSaver = builder.modelAdapterModelSaver,
        singleModelLoader = builder.singleModelLoader,
        listModelLoader = builder.listModelLoader
    )

    /**
     * Table builder for java consumers. use [TableConfig] directly if calling from Kotlin.
     */
    class Builder<T : Any>(internal val tableClass: KClass<T>) {
        internal var modelAdapterModelSaver: ModelSaver<T>? = null
        internal var singleModelLoader: SingleModelLoader<T>? = null
        internal var listModelLoader: ListModelLoader<T>? = null

        /**
         * Define how the [ModelAdapter] saves data into the DB from its associated [T]. This
         * will override the default.
         */
        fun modelAdapterModelSaver(modelSaver: ModelSaver<T>) = apply {
            this.modelAdapterModelSaver = modelSaver
        }

        /**
         * Define how the table loads single models. This will override the default.
         */
        fun singleModelLoader(singleModelLoader: SingleModelLoader<T>) = apply {
            this.singleModelLoader = singleModelLoader
        }

        /**
         * Define how the table loads a [List] of items. This will override the default.
         */
        fun listModelLoader(listModelLoader: ListModelLoader<T>) = apply {
            this.listModelLoader = listModelLoader
        }

        /**
         * @return A new [TableConfig]. Subsequent calls to this method produce a new instance
         * of [TableConfig].
         */
        fun build(): TableConfig<*> = TableConfig(this)
    }

    companion object {

        fun <T : Any> builder(tableClass: KClass<T>): Builder<T> =
            Builder(tableClass)
    }
}
