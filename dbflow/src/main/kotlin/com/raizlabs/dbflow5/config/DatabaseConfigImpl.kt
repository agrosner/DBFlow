package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.database.DatabaseCallback
import com.raizlabs.dbflow5.runtime.ModelNotifier
import kotlin.reflect.KClass


actual class DatabaseConfig : InternalDatabaseConfig {

    constructor(databaseClass: KClass<*>,
                openHelperCreator: OpenHelperCreator? = null,
                transactionManagerCreator: TransactionManagerCreator? = null,
                callback: DatabaseCallback? = null,
                tableConfigMap: Map<KClass<*>, TableConfig<*>> = mapOf(),
                modelNotifier: ModelNotifier? = null,
                isInMemory: Boolean = false,
                databaseName: String? = null,
                databaseExtensionName: String? = null) :
        super(
            databaseClass,
            openHelperCreator,
            transactionManagerCreator,
            callback,
            tableConfigMap,
            modelNotifier,
            isInMemory,
            databaseName,
            databaseExtensionName
        )

    constructor(builder: Builder) : super(builder)

    actual class Builder actual constructor(databaseClass: KClass<*>,
                                            openHelperCreator: OpenHelperCreator?)
        : InternalBuilder(databaseClass, openHelperCreator) {
        override fun build(): DatabaseConfig = DatabaseConfig(this)
    }

    actual companion object {

        @JvmStatic
        fun builder(database: Class<*>, openHelperCreator: OpenHelperCreator): Builder =
            Builder(database.kotlin, openHelperCreator)

        @JvmStatic
        actual fun builder(database: KClass<*>, openHelperCreator: OpenHelperCreator): DatabaseConfig.Builder =
            InternalDatabaseConfig.builder(database, openHelperCreator)

        @JvmStatic
        actual fun inMemoryBuilder(database: KClass<*>, openHelperCreator: OpenHelperCreator): DatabaseConfig.Builder =
            InternalDatabaseConfig.builder(database, openHelperCreator)

        @JvmStatic
        fun inMemoryBuilder(database: Class<*>, openHelperCreator: OpenHelperCreator): Builder =
            Builder(database.kotlin, openHelperCreator).inMemory()
    }
}