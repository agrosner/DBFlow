package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.adapter.queriable.ListModelLoader
import com.raizlabs.dbflow5.adapter.queriable.SingleModelLoader
import com.raizlabs.dbflow5.adapter.saveable.ModelSaver
import kotlin.reflect.KClass

/**
 * Description:
 */
actual class TableConfig<T : Any> : InternalTableConfig<T> {

    actual constructor(tableClass: KClass<T>,
                       modelSaver: ModelSaver<T>?,
                       singleModelLoader: SingleModelLoader<T>?,
                       listModelLoader: ListModelLoader<T>?) :
        super(tableClass, modelSaver, singleModelLoader, listModelLoader)

    actual constructor(builder: Builder<T>) : super(builder)

    actual class Builder<T : Any> : InternalBuilder<T> {

        actual constructor(tableClass: KClass<T>) : super(tableClass)

        constructor(tableClass: Class<T>) : super(tableClass.kotlin)
    }

    companion object {

        @JvmStatic
        fun <T : Any> builder(tableClass: KClass<T>): TableConfig.Builder<T> =
            TableConfig.Builder(tableClass)

        @JvmStatic
        fun <T : Any> builder(tableClass: Class<T>): TableConfig.Builder<T> =
            TableConfig.Builder(tableClass)
    }
}