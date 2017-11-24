package com.raizlabs.android.dbflow.structure

import android.content.ContentValues
import android.database.sqlite.SQLiteStatement
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.DEFAULT_CACHE_SIZE
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.sql.saveable.ListModelSaver
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver
import com.raizlabs.android.dbflow.structure.cache.IMultiKeyCacheConverter
import com.raizlabs.android.dbflow.structure.cache.ModelCache
import com.raizlabs.android.dbflow.structure.cache.SimpleMapCache
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Used for generated classes from the combination of [Table] and [Model].
 */
abstract class ModelAdapter<T : Any>(databaseDefinition: DatabaseDefinition)
    : InstanceAdapter<T>(databaseDefinition), InternalAdapter<T> {

    private var insertStatement: DatabaseStatement? = null
    private var compiledStatement: DatabaseStatement? = null
    private var updateStatement: DatabaseStatement? = null
    private var deleteStatement: DatabaseStatement? = null

    val cachingColumns: Array<String> by lazy { createCachingColumns() }
    val modelCache: ModelCache<T, *> by lazy { createModelCache() }
    private var _modelSaver: ModelSaver<T>? = null

    val listModelSaver: ListModelSaver<T> by lazy { createListModelSaver() }

    /**
     * @return The autoincrement column name for the [PrimaryKey.autoincrement]
     * if it has the field. This method is overridden when its specified for the [T]
     */
    open val autoIncrementingColumnName: String
        get() = throw InvalidDBConfiguration(
                String.format("This method may have been called in error. The model class %1s must contain " + "an autoincrementing or single int/long primary key (if used in a ModelCache, this method may be called)",
                        modelClass))

    open val cacheSize: Int
        get() = DEFAULT_CACHE_SIZE

    open val cacheConverter: IMultiKeyCacheConverter<*>
        get() = throw InvalidDBConfiguration("For multiple primary keys, a public static IMultiKeyCacheConverter field must" +
                "be  marked with @MultiCacheField in the corresponding model class. The resulting key" +
                "must be a unique combination of the multiple keys, otherwise inconsistencies may occur.")

    /**
     * @return The query used to create this table.
     */
    abstract val creationQuery: String

    /**
     * @return An array of column properties, in order of declaration.
     */
    abstract val allColumnProperties: Array<IProperty<*>>

    /**
     * @return The query used to insert a model using a [SQLiteStatement]
     */
    protected open val insertStatementQuery: String
        get() = compiledStatementQuery

    /**
     * @return The normal query used in saving a model if we use a [SQLiteStatement].
     */
    protected abstract val compiledStatementQuery: String

    protected abstract val updateStatementQuery: String

    protected abstract val deleteStatementQuery: String

    /**
     * @return The conflict algorithm to use when updating a row in this table.
     */
    open val updateOnConflictAction: ConflictAction
        get() = ConflictAction.ABORT

    /**
     * @return The conflict algorithm to use when inserting a row in this table.
     */
    open val insertOnConflictAction: ConflictAction
        get() = ConflictAction.ABORT

    init {
        if (tableConfig != null && tableConfig!!.modelSaver != null) {
            _modelSaver = tableConfig!!.modelSaver
            _modelSaver!!.modelAdapter = this
        }
    }

    /**
     * @return The pre-compiled insert statement for this table model adapter. This is reused and cached.
     */
    fun getInsertStatement(): DatabaseStatement {
        if (insertStatement == null) {
            insertStatement = getInsertStatement(FlowManager.getWritableDatabaseForTable(modelClass))
        }

        return insertStatement!!
    }

    /**
     * @return The pre-compiled update statement for this table model adapter. This is reused and cached.
     */
    fun getUpdateStatement(): DatabaseStatement {
        if (updateStatement == null) {
            updateStatement = getUpdateStatement(FlowManager.getWritableDatabaseForTable(modelClass))
        }

        return updateStatement!!
    }

    /**
     * @return The pre-compiled delete statement for this table model adapter. This is reused and cached.
     */
    fun getDeleteStatement(): DatabaseStatement {
        if (deleteStatement == null) {
            deleteStatement = getDeleteStatement(FlowManager.getWritableDatabaseForTable(modelClass))
        }

        return deleteStatement!!
    }

    fun closeInsertStatement() {
        insertStatement?.close()
        insertStatement = null
    }

    fun closeUpdateStatement() {
        updateStatement?.close()
        updateStatement = null
    }

    fun closeDeleteStatement() {
        deleteStatement?.close()
        deleteStatement = null
    }

    /**
     * @param databaseWrapper The database used to do an insert statement.
     * @return a new compiled [DatabaseStatement] representing insert. Not cached, always generated.
     * To bind values use [.bindToInsertStatement].
     */
    fun getInsertStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement =
            databaseWrapper.compileStatement(insertStatementQuery)

    /**
     * @param databaseWrapper The database used to do an update statement.
     * @return a new compiled [DatabaseStatement] representing update. Not cached, always generated.
     * To bind values use [.bindToUpdateStatement].
     */
    fun getUpdateStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement =
            databaseWrapper.compileStatement(updateStatementQuery)

    /**
     * @param databaseWrapper The database used to do a delete statement.
     * @return a new compiled [DatabaseStatement] representing delete. Not cached, always generated.
     * To bind values use [.bindToDeleteStatement].
     */
    fun getDeleteStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement =
            databaseWrapper.compileStatement(deleteStatementQuery)

    /**
     * @return The precompiled full statement for this table model adapter
     */
    fun getCompiledStatement(): DatabaseStatement {
        if (compiledStatement == null) {
            compiledStatement = getCompiledStatement(FlowManager.getWritableDatabaseForTable(modelClass))
        }

        return compiledStatement!!
    }

    fun closeCompiledStatement() {
        compiledStatement?.close()
        compiledStatement = null
    }

    /**
     * @param databaseWrapper The database used to do an insert statement.
     * @return a new compiled [DatabaseStatement] representing insert.
     * To bind values use [.bindToInsertStatement].
     */
    fun getCompiledStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement =
            databaseWrapper.compileStatement(compiledStatementQuery)

    /**
     * Creates a new [T] and Loads the cursor into a the object.
     *
     * @param cursor The cursor to load
     * @return A new [T]
     */
    fun loadFromCursor(cursor: FlowCursor, databaseWrapper: DatabaseWrapper): T {
        val model = newInstance()
        loadFromCursor(cursor, model, databaseWrapper)
        return model
    }

    override fun save(model: T): Boolean = modelSaver.save(model)

    override fun save(model: T, databaseWrapper: DatabaseWrapper): Boolean =
            modelSaver.save(model, databaseWrapper)

    override fun saveAll(models: Collection<T>) {
        listModelSaver.saveAll(models)
    }

    override fun saveAll(models: Collection<T>, databaseWrapper: DatabaseWrapper) {
        listModelSaver.saveAll(models, databaseWrapper)
    }

    override fun insert(model: T): Long = modelSaver.insert(model)

    override fun insert(model: T, databaseWrapper: DatabaseWrapper): Long =
            modelSaver.insert(model, databaseWrapper)

    override fun insertAll(models: Collection<T>) {
        listModelSaver.insertAll(models)
    }

    override fun insertAll(models: Collection<T>, databaseWrapper: DatabaseWrapper) {
        listModelSaver.insertAll(models, databaseWrapper)
    }

    override fun update(model: T): Boolean = modelSaver.update(model)

    override fun update(model: T, databaseWrapper: DatabaseWrapper): Boolean =
            modelSaver.update(model, databaseWrapper)

    override fun updateAll(models: Collection<T>) {
        listModelSaver.updateAll(models)
    }

    override fun updateAll(models: Collection<T>, databaseWrapper: DatabaseWrapper) {
        listModelSaver.updateAll(models, databaseWrapper)
    }

    override fun delete(model: T): Boolean = modelSaver.delete(model)

    override fun delete(model: T, databaseWrapper: DatabaseWrapper): Boolean =
            modelSaver.delete(model, databaseWrapper)

    override fun deleteAll(models: Collection<T>, databaseWrapper: DatabaseWrapper) {
        listModelSaver.deleteAll(models, databaseWrapper)
    }

    override fun deleteAll(models: Collection<T>) {
        listModelSaver.deleteAll(models)
    }

    override fun bindToInsertStatement(sqLiteStatement: DatabaseStatement, model: T) {
        bindToInsertStatement(sqLiteStatement, model, 0)
    }

    override fun bindToContentValues(contentValues: ContentValues, model: T) {
        bindToInsertValues(contentValues, model)
    }

    override fun bindToStatement(sqLiteStatement: DatabaseStatement, model: T) {
        bindToInsertStatement(sqLiteStatement, model, 0)
    }

    /**
     * If a [Model] has an auto-incrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    override fun updateAutoIncrement(model: T, id: Number) {

    }

    /**
     * @return The value for the [PrimaryKey.autoincrement]
     * if it has the field. This method is overridden when its specified for the [T]
     */
    override fun getAutoIncrementingId(model: T): Number? {
        throw InvalidDBConfiguration(
                String.format("This method may have been called in error. The model class %1s must contain" + "a single primary key (if used in a ModelCache, this method may be called)",
                        modelClass))
    }

    fun hasAutoIncrement(model: T): Boolean {
        val id = getAutoIncrementingId(model) ?: throw IllegalStateException("An autoincrementing column field cannot be null.")

        return id.toLong() > 0
    }

    /**
     * Called when we want to save our [ForeignKey] objects. usually during insert + update.
     * This method is overridden when [ForeignKey] specified
     */
    open fun saveForeignKeys(model: T, wrapper: DatabaseWrapper) {

    }

    /**
     * Called when we want to delete our [ForeignKey] objects. During deletion [.delete]
     * This method is overridden when [ForeignKey] specified
     */
    open fun deleteForeignKeys(model: T, wrapper: DatabaseWrapper) {

    }

    /**
     * @return A set of columns that represent the caching columns.
     */
    open fun createCachingColumns(): Array<String> = arrayOf(autoIncrementingColumnName)

    /**
     * Loads all primary keys from the [FlowCursor] into the inValues. The size of the array must
     * match all primary keys. This method gets generated when caching is enabled.
     *
     * @param inValues The reusable array of values to populate.
     * @param cursor   The cursor to load from.
     * @return The populated set of values to load from cache.
     */
    open fun getCachingColumnValuesFromCursor(inValues: Array<Any?>,
                                              cursor: FlowCursor): Array<Any>? {
        throwCachingError()
        return null
    }

    /**
     * @param cursor The cursor to load caching id from.
     * @return The single cache column from cursor (if single).
     */
    open fun getCachingColumnValueFromCursor(cursor: FlowCursor): Any? {
        throwSingleCachingError()
        return null
    }

    /**
     * Loads all primary keys from the [TModel] into the inValues. The size of the array must
     * match all primary keys. This method gets generated when caching is enabled. It converts the primary fields
     * of the [TModel] into the array of values the caching mechanism uses.
     *
     * @param inValues The reusable array of values to populate.
     * @param TModel   The model to load from.
     * @return The populated set of values to load from cache.
     */
    open fun getCachingColumnValuesFromModel(inValues: Array<Any?>, TModel: T): Array<Any>? {
        throwCachingError()
        return null
    }

    /**
     * @param model The model to load cache column data from.
     * @return The single cache column from model (if single).
     */
    open fun getCachingColumnValueFromModel(model: T): Any? {
        throwSingleCachingError()
        return null
    }

    fun storeModelInCache(model: T) {
        modelCache.addModel(getCachingId(model), model)
    }

    fun removeModelFromCache(model: T) {
        getCachingId(model)?.let { modelCache.removeModel(it) }
    }

    fun getCachingId(inValues: Array<Any>?): Any? = when {
        inValues?.size == 1 -> // if it exists in cache no matter the query we will use that one
            inValues.getOrNull(0)
        inValues != null -> cacheConverter.getCachingKey(inValues)
        else -> null
    }

    open fun getCachingId(model: T): Any? =
            getCachingId(getCachingColumnValuesFromModel(arrayOfNulls(cachingColumns.size), model))

    var modelSaver: ModelSaver<T>
        get() {
            if (_modelSaver == null) {
                _modelSaver = createSingleModelSaver().apply { modelAdapter = this@ModelAdapter }
            }
            return _modelSaver!!
        }
        set(value) {
            this._modelSaver = value
            value.modelAdapter = this
        }

    protected open fun createSingleModelSaver(): ModelSaver<T> = ModelSaver()

    protected open fun createListModelSaver(): ListModelSaver<T> = ListModelSaver(modelSaver)

    /**
     * Reloads relationships when loading from [FlowCursor] in a model that's cacheable. By having
     * relationships with cached models, the retrieval will be very fast.
     *
     * @param cursor The cursor to reload from.
     */
    open fun reloadRelationships(model: T, cursor: FlowCursor, databaseWrapper: DatabaseWrapper) {
        if (!cachingEnabled()) {
            throwCachingError()
        }
    }

    override fun cachingEnabled(): Boolean = false

    open fun createModelCache(): ModelCache<T, *> = SimpleMapCache(cacheSize)

    /**
     * Retrieves a property by name from the table via the corresponding generated "_Table" class. Useful
     * when you want to dynamically get a property from an [ModelAdapter] and do an operation on it.
     *
     * @param columnName The column name of the property.
     * @return The property from the corresponding Table class.
     */
    abstract fun getProperty(columnName: String): Property<*>

    /**
     * @return When false, this table gets generated and associated with database, however it will not immediately
     * get created upon startup. This is useful for keeping around legacy tables for migrations.
     */
    open fun createWithDatabase(): Boolean = true

    private fun throwCachingError() {
        throw InvalidDBConfiguration(
                String.format("This method may have been called in error. The model class %1s must contain" + "an auto-incrementing or at least one primary key (if used in a ModelCache, this method may be called)",
                        modelClass))
    }

    private fun throwSingleCachingError() {
        throw InvalidDBConfiguration(
                String.format("This method may have been called in error. The model class %1s must contain" + "an auto-incrementing or one primary key (if used in a ModelCache, this method may be called)",
                        modelClass))
    }

}
