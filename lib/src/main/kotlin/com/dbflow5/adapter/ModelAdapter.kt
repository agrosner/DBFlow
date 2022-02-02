package com.dbflow5.adapter

import com.dbflow5.adapter.saveable.ListModelSaver
import com.dbflow5.adapter.saveable.ModelSaver
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.Table
import com.dbflow5.config.FlowLog
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.Property

/**
 * Description: Used for generated classes from the combination of [Table] and [Model].
 */
abstract class ModelAdapter<T : Any> :
    RetrievalAdapter<T>(), InternalAdapter<T>, CreationAdapter {

    private var _modelSaver: ModelSaver<T>? = null

    val listModelSaver: ListModelSaver<T> by lazy { createListModelSaver() }

    /**
     * @return An array of column properties, in order of declaration.
     */
    abstract val allColumnProperties: Array<IProperty<*>>

    /**
     * @return The query used to insert a model using a [DatabaseStatement]
     */
    protected abstract val insertStatementQuery: String

    protected abstract val updateStatementQuery: String

    protected abstract val deleteStatementQuery: String

    protected abstract val saveStatementQuery: String

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

    /**
     * @param databaseWrapper The database used to do an insert statement.
     * @return a new compiled [DatabaseStatement] representing insert. Not cached, always generated.
     * To bind values use [bindToInsertStatement].
     */
    fun getInsertStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement =
        databaseWrapper.compileStatement(insertStatementQuery)

    /**
     * @param databaseWrapper The database used to do an update statement.
     * @return a new compiled [DatabaseStatement] representing update. Not cached, always generated.
     * To bind values use [bindToUpdateStatement].
     */
    fun getUpdateStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement =
        databaseWrapper.compileStatement(updateStatementQuery)

    /**
     * @param databaseWrapper The database used to do a delete statement.
     * @return a new compiled [DatabaseStatement] representing delete. Not cached, always generated.
     * To bind values use [bindToDeleteStatement].
     */
    fun getDeleteStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement =
        databaseWrapper.compileStatement(deleteStatementQuery)

    /**
     * @param databaseWrapper The database used to do a save (insert or replace) statement.
     * @return a new compiled [DatabaseStatement] representing insert or replace. Not cached, always generated.
     * To bind values use [bindToInsertStatement].
     */
    fun getSaveStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement =
        databaseWrapper.compileStatement(saveStatementQuery)

    override fun save(model: T, databaseWrapper: DatabaseWrapper): Result<T> {
        checkInTransaction(databaseWrapper)
        return modelSaver.save(model, databaseWrapper)
    }

    override fun saveAll(
        models: Collection<T>,
        databaseWrapper: DatabaseWrapper
    ): Result<Collection<T>> {
        checkInTransaction(databaseWrapper)
        return listModelSaver.saveAll(models, databaseWrapper)
    }

    override fun insert(model: T, databaseWrapper: DatabaseWrapper): Result<T> {
        checkInTransaction(databaseWrapper)
        return modelSaver.insert(model, databaseWrapper)
    }

    override fun insertAll(
        models: Collection<T>,
        databaseWrapper: DatabaseWrapper
    ): Result<Collection<T>> {
        checkInTransaction(databaseWrapper)
        return listModelSaver.insertAll(models, databaseWrapper)
    }

    override fun update(model: T, databaseWrapper: DatabaseWrapper): Result<T> {
        checkInTransaction(databaseWrapper)
        return modelSaver.update(model, databaseWrapper)
    }

    override fun updateAll(
        models: Collection<T>,
        databaseWrapper: DatabaseWrapper
    ): Result<Collection<T>> {
        checkInTransaction(databaseWrapper)
        return listModelSaver.updateAll(models, databaseWrapper)
    }

    override fun delete(model: T, databaseWrapper: DatabaseWrapper): Result<T> {
        checkInTransaction(databaseWrapper)
        return modelSaver.delete(model, databaseWrapper)
    }

    override fun deleteAll(
        models: Collection<T>,
        databaseWrapper: DatabaseWrapper
    ): Result<Collection<T>> {
        checkInTransaction(databaseWrapper)
        return listModelSaver.deleteAll(models, databaseWrapper)
    }

    private fun checkInTransaction(databaseWrapper: DatabaseWrapper) {
        if (!databaseWrapper.isInTransaction) {
            FlowLog.log(
                FlowLog.Level.W,
                "Database Not Running in a Transaction. Performance may be impacted, observability " +
                    "will need manual updates via db.tableObserver.checkForTableUpdates()"
            )
        }
    }

    /**
     * If a [Model] has an auto-incrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    override fun updateAutoIncrement(model: T, id: Number): T {
        return model
    }

    /**
     * Called when we want to save our [ForeignKey] objects. usually during insert + update.
     * This method is overridden when [ForeignKey] specified
     */
    open fun saveForeignKeys(model: T, wrapper: DatabaseWrapper): T {
        return model
    }

    var modelSaver: ModelSaver<T>
        get() = tableConfig?.modelSaver?.let {
            it.modelAdapter = this
            it
        }
            ?: createSingleModelSaver()
                .apply { modelAdapter = this@ModelAdapter }
                .also { _modelSaver = it }
        set(value) {
            this._modelSaver = value
            value.modelAdapter = this
        }

    protected open fun createSingleModelSaver(): ModelSaver<T> = ModelSaver()

    protected open fun createListModelSaver(): ListModelSaver<T> = ListModelSaver(modelSaver)

    /**
     * Retrieves a property by name from the table via the corresponding generated "_Table" class. Useful
     * when you want to dynamically get a property from an [ModelAdapter] and do an operation on it.
     *
     * @param columnName The column name of the property.
     * @return The property from the corresponding Table class.
     */
    abstract fun getProperty(columnName: String): Property<*>


}
