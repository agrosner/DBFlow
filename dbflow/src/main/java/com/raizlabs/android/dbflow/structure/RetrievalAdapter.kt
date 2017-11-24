package com.raizlabs.android.dbflow.structure

import android.database.Cursor
import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.config.TableConfig
import com.raizlabs.android.dbflow.sql.language.OperatorGroup
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.sql.language.where
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Provides a base retrieval class for all [Model] backed
 * adapters.
 */
abstract class RetrievalAdapter<T : Any>(databaseDefinition: DatabaseDefinition) {

    /**
     * Overrides the default implementation and allows you to provide your own implementation. Defines
     * how a single [T] is loaded.
     *
     * @param singleModelLoader The loader to use.
     */
    private var _singleModelLoader: SingleModelLoader<T>? = null

    var singleModelLoader: SingleModelLoader<T>
        get() {
            if (_singleModelLoader == null) {
                _singleModelLoader = createSingleModelLoader()
            }
            return _singleModelLoader!!
        }
        set(value) {
            this._singleModelLoader = value
        }
    /**
     * @return A new [ListModelLoader], caching will override this loader instance.
     */
    /**
     * Overrides the default implementation and allows you to provide your own implementation. Defines
     * how a list of [T] are loaded.
     *
     * @param listModelLoader The loader to use.
     */
    private var _listModelLoader: ListModelLoader<T>? = null

    var listModelLoader: ListModelLoader<T>
        get() {
            if (_listModelLoader == null) {
                _listModelLoader = createListModelLoader()
            }
            return _listModelLoader!!
        }
        set(value) {
            this._listModelLoader = value
        }

    protected var tableConfig: TableConfig<T>? = null
        private set

    /**
     * @return the model class this adapter corresponds to
     */
    abstract val modelClass: Class<T>

    /**
     * @return A new instance of a [SingleModelLoader]. Subsequent calls do not cache
     * this object so it's recommended only calling this in bulk if possible.
     */
    val nonCacheableSingleModelLoader: SingleModelLoader<T>
        get() = SingleModelLoader(modelClass)

    /**
     * @return A new instance of a [ListModelLoader]. Subsequent calls do not cache
     * this object so it's recommended only calling this in bulk if possible.
     */
    val nonCacheableListModelLoader: ListModelLoader<T>
        get() = ListModelLoader(modelClass)

    init {
        val databaseConfig = FlowManager.getConfig()
                .getConfigForDatabase(databaseDefinition.associatedDatabaseClassFile)
        if (databaseConfig != null) {
            tableConfig = databaseConfig.getTableConfigForTable(modelClass)
            if (tableConfig != null) {
                tableConfig?.singleModelLoader?.let { _singleModelLoader = it }
                tableConfig?.listModelLoader?.let { _listModelLoader = it }
            }
        }
    }

    /**
     * Force loads the model from the DB. Even if caching is enabled it will requery the object.
     */
    open fun load(model: T, databaseWrapper: DatabaseWrapper) {
        nonCacheableSingleModelLoader.load(databaseWrapper,
                (databaseWrapper.select
                        from modelClass
                        where getPrimaryConditionClause(model)).query,
                model)
    }

    /**
     * Assigns the [Cursor] data into the specified [T]
     *
     * @param model  The model to assign cursor data to
     * @param cursor The cursor to load into the model
     */
    abstract fun loadFromCursor(cursor: FlowCursor, model: T, wrapper: DatabaseWrapper)

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    open fun exists(model: T): Boolean =
            exists(model, FlowManager.getDatabaseForTable(modelClass).writableDatabase)

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    abstract fun exists(model: T, databaseWrapper: DatabaseWrapper): Boolean

    /**
     * @param model The primary condition clause.
     * @return The clause that contains necessary primary conditions for this table.
     */
    abstract fun getPrimaryConditionClause(model: T): OperatorGroup

    /**
     * @return A new [ListModelLoader], caching will override this loader instance.
     */
    protected open fun createListModelLoader(): ListModelLoader<T> = ListModelLoader(modelClass)

    /**
     * @return A new [SingleModelLoader], caching will override this loader instance.
     */
    protected open fun createSingleModelLoader(): SingleModelLoader<T> = SingleModelLoader(modelClass)

}
/**
 * Force loads the model from the DB. Even if caching is enabled it will requery the object.
 */
