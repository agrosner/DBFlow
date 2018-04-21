package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.database.DatabaseCallback
import com.raizlabs.dbflow5.runtime.ModelNotifier
import kotlin.reflect.KClass


actual class DatabaseConfig : InternalDatabaseConfig {

    constructor(databaseClass: KClass<*>,
                databaseName: String,
                openHelperCreator: OpenHelperCreator? = null,
                transactionManagerCreator: TransactionManagerCreator? = null,
                callback: DatabaseCallback? = null,
                tableConfigMap: Map<KClass<*>, TableConfig<*>> = mapOf(),
                modelNotifier: ModelNotifier? = null,
                isInMemory: Boolean = false,
                databaseExtensionName: String? = null) :
        super(
            databaseClass,
            databaseName,
            openHelperCreator,
            transactionManagerCreator,
            callback,
            tableConfigMap,
            modelNotifier,
            isInMemory,
            databaseExtensionName
        )

    constructor(builder: Builder) : super(builder)

    actual class Builder actual constructor(databaseClass: KClass<*>,
                                            databaseName: String,
                                            openHelperCreator: OpenHelperCreator?)
        : InternalBuilder(databaseClass, databaseName, openHelperCreator) {
        override fun build(): DatabaseConfig = DatabaseConfig(this)
    }

    actual companion object {

        @JvmStatic
        fun builder(database: Class<*>, databaseName: String, openHelperCreator: OpenHelperCreator): Builder =
            Builder(database.kotlin, databaseName, openHelperCreator)

        @JvmStatic
        actual fun builder(database: KClass<*>, databaseName: String, openHelperCreator: OpenHelperCreator): DatabaseConfig.Builder =
            InternalDatabaseConfig.builder(database, databaseName, openHelperCreator)

        @JvmStatic
        actual fun inMemoryBuilder(database: KClass<*>, databaseName: String, openHelperCreator: OpenHelperCreator): DatabaseConfig.Builder =
            InternalDatabaseConfig.builder(database, databaseName, openHelperCreator)

        @JvmStatic
        fun inMemoryBuilder(database: Class<*>, databaseName: String, openHelperCreator: OpenHelperCreator): Builder =
            Builder(database.kotlin, databaseName, openHelperCreator).inMemory()
    }
}