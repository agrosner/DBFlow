package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.JvmStatic
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.adapter.queriable.ListModelLoader
import com.raizlabs.dbflow5.adapter.queriable.SingleModelLoader
import com.raizlabs.dbflow5.adapter.saveable.ModelSaver
import kotlin.reflect.KClass

expect class TableConfig<T : Any> : InternalTableConfig<T> {

    constructor(builder: Builder<T>)

    constructor(tableClass: KClass<T>,
                modelSaver: ModelSaver<T>?,
                singleModelLoader: SingleModelLoader<T>?,
                listModelLoader: ListModelLoader<T>?)

    class Builder<T : Any> : InternalBuilder<T> {
        constructor(tableClass: KClass<T>)
    }
}

/**
 * Description: Represents certain table configuration options. This allows you to easily specify
 * certain configuration options for a table.
 */
abstract class InternalTableConfig<T : Any>
internal constructor(val tableClass: KClass<T>,
                     val modelSaver: ModelSaver<T>? = null,
                     val singleModelLoader: SingleModelLoader<T>? = null,
                     val listModelLoader: ListModelLoader<T>? = null) {

    internal constructor(builder: InternalBuilder<T>) : this(
        tableClass = builder.tableClass,
        modelSaver = builder.modelAdapterModelSaver,
        singleModelLoader = builder.singleModelLoader,
        listModelLoader = builder.listModelLoader
    )

    /**
     * Table builder for java consumers. use [TableConfig] directly if calling from Kotlin.
     */
    abstract class InternalBuilder<T : Any> internal constructor(internal val tableClass: KClass<T>) {
        internal var modelAdapterModelSaver: ModelSaver<T>? = null
        internal var singleModelLoader: SingleModelLoader<T>? = null
        internal var listModelLoader: ListModelLoader<T>? = null

        /**
         * Define how the [ModelAdapter] saves data into the DB from its associated [T]. This
         * will override the default.
         */
        fun modelAdapterModelSaver(modelSaver: ModelSaver<T>) = applyThis {
            this.modelAdapterModelSaver = modelSaver
        }

        /**
         * Define how the table loads single models. This will override the default.
         */
        fun singleModelLoader(singleModelLoader: SingleModelLoader<T>) = applyThis {
            this.singleModelLoader = singleModelLoader
        }

        /**
         * Define how the table loads a [List] of items. This will override the default.
         */
        fun listModelLoader(listModelLoader: ListModelLoader<T>) = applyThis {
            this.listModelLoader = listModelLoader
        }

        /**
         * @return A new [TableConfig]. Subsequent calls to this method produce a new instance
         * of [TableConfig].
         */
        fun build(): TableConfig<*> = TableConfig(this as TableConfig.Builder<T>)

        private inline fun applyThis(fn: InternalBuilder<T>.() -> Unit) = apply(fn) as TableConfig.Builder<T>
    }

    companion object {

        @JvmStatic
        fun <T : Any> builder(tableClass: KClass<T>): TableConfig.Builder<T> =
            TableConfig.Builder(tableClass)
    }
}
