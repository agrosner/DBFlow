package com.dbflow5.config

import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.OpenHelper
import com.dbflow5.isNotNullOrEmpty
import com.dbflow5.runtime.ModelNotifier
import com.dbflow5.transaction.TransactionDispatcher
import java.util.regex.Pattern
import kotlin.reflect.KClass

fun interface OpenHelperCreator {
    fun createHelper(db: DBFlowDatabase, callback: DatabaseCallback?): OpenHelper
}

fun interface TransactionDispatcherFactory {
    fun create(): TransactionDispatcher
}


/**
 *
 * Checks if databaseName is valid. It will check if databaseName matches regex pattern
 * [A-Za-z_$]+[a-zA-Z0-9_$]
 * Examples:
 * database - valid
 * DbFlow1 - valid
 * database.db - invalid (contains a dot)
 * 1database - invalid (starts with a number)
 * @param databaseName database name to validate.
 * @return `true` if parameter is a valid database name, `false` otherwise.
 */
private fun isValidDatabaseName(databaseName: String?): Boolean {
    val javaClassNamePattern = Pattern.compile("[A-Za-z_$]+[a-zA-Z0-9_$]*")
    return javaClassNamePattern.matcher(databaseName).matches()
}

/**
 * Description:
 */
class DatabaseConfig(
    val databaseClass: KClass<*>,
    val openHelperCreator: OpenHelperCreator? = null,
    val transactionDispatcherFactory: TransactionDispatcherFactory? = null,
    val callback: DatabaseCallback? = null,
    val tableConfigMap: Map<KClass<*>, TableConfig<*>> = mapOf(),
    val modelNotifier: ((DBFlowDatabase) -> ModelNotifier)? = null,
    val isInMemory: Boolean = false,
    val databaseName: String? = null,
    val databaseExtensionName: String? = null,
    val journalMode: DBFlowDatabase.JournalMode = DBFlowDatabase.JournalMode.Automatic,
    val throwExceptionsOnCreate: Boolean = true,
) {

    internal constructor(builder: Builder) : this(
        // convert java interface to kotlin function.
        openHelperCreator = builder.openHelperCreator,
        databaseClass = builder.databaseClass,
        transactionDispatcherFactory = builder.transactionDispatcherFactory,
        callback = builder.callback,
        tableConfigMap = builder.tableConfigMap,
        modelNotifier = builder.modelNotifier,
        isInMemory = builder.inMemory,
        databaseName = builder.databaseName ?: builder.databaseClass.simpleName,
        databaseExtensionName = when {
            builder.databaseExtensionName == null -> ".db"
            builder.databaseExtensionName.isNotNullOrEmpty() -> ".${builder.databaseExtensionName}"
            else -> ""
        },
        journalMode = builder.journalMode,
        throwExceptionsOnCreate = builder.throwExceptionsOnCreate,
    ) {
        if (!isValidDatabaseName(databaseName)) {
            throw IllegalArgumentException(
                "Invalid database name $databaseName found. Names must follow " +
                    "the \"[A-Za-z_\$]+[a-zA-Z0-9_\$]*\" pattern."
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getTableConfigForTable(modelClass: KClass<T>): TableConfig<T>? =
        tableConfigMap[modelClass] as TableConfig<T>?

    /**
     * Build compatibility class for Java. Use the [DatabaseConfig] class directly if Kotlin consumer.
     */
    class Builder(
        internal val databaseClass: KClass<*>,
        internal val openHelperCreator: OpenHelperCreator? = null
    ) {

        internal var transactionDispatcherFactory: TransactionDispatcherFactory? = null
        internal var callback: DatabaseCallback? = null
        internal val tableConfigMap: MutableMap<KClass<*>, TableConfig<*>> = hashMapOf()
        internal var modelNotifier: ((DBFlowDatabase) -> ModelNotifier)? = null
        internal var inMemory = false
        internal var databaseName: String? = null
        internal var databaseExtensionName: String? = null
        internal var journalMode: DBFlowDatabase.JournalMode = DBFlowDatabase.JournalMode.Automatic
        internal var throwExceptionsOnCreate: Boolean = true

        fun transactionDispatcherFactory(creator: TransactionDispatcherFactory) = apply {
            this.transactionDispatcherFactory = creator
        }

        fun helperListener(callback: DatabaseCallback) = apply {
            this.callback = callback
        }

        fun addTableConfig(tableConfig: TableConfig<*>) = apply {
            tableConfigMap[tableConfig.tableClass] = tableConfig
        }

        inline fun <reified T : Any> table(fn: TableConfig.Builder<T>.() -> Unit) =
            addTableConfig(TableConfig.builder(T::class).apply(fn).build())

        fun modelNotifier(modelNotifier: (DBFlowDatabase) -> ModelNotifier) = apply {
            this.modelNotifier = modelNotifier
        }

        fun inMemory() = apply {
            inMemory = true
        }

        fun journalMode(journalMode: DBFlowDatabase.JournalMode) = apply {
            this.journalMode = journalMode
        }

        /**
         * @return Pass in dynamic database name here. Otherwise it defaults to class name.
         */
        fun databaseName(name: String) = apply {
            databaseName = name
        }

        /**
         * @return Pass in the extension for the DB here.
         * Otherwise defaults to ".db". If empty string passed, no extension is used.
         */
        fun extensionName(name: String) = apply {
            databaseExtensionName = name
        }

        fun throwExceptionsOnCreate(throwExceptionsOnCreate: Boolean) = apply {
            this.throwExceptionsOnCreate = throwExceptionsOnCreate
        }

        fun build() = DatabaseConfig(this)
    }

    companion object {

        fun builder(database: KClass<*>, openHelperCreator: OpenHelperCreator? = null): Builder =
            Builder(database, openHelperCreator)

        fun inMemoryBuilder(
            database: KClass<*>,
            openHelperCreator: OpenHelperCreator? = null
        ): Builder =
            Builder(database, openHelperCreator).inMemory()
    }
}
