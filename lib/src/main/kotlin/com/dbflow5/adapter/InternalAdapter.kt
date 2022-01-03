package com.dbflow5.adapter

import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper

/**
 * Description: Used for our internal Adapter classes such as generated [ModelAdapter].
 */
interface InternalAdapter<TModel> {

    /**
     * @return The table name of this adapter.
     */
    val name: String

    /**
     * Saves the specified model to the DB.
     *
     * @param model           The model to save/insert/update
     * @param databaseWrapper The manually specified wrapper.
     * @return model if save was successful, else null.
     */
    suspend fun save(model: TModel, databaseWrapper: DatabaseWrapper): Result<TModel>

    /**
     * Saves a [Collection] of models to the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseWrapper The manually specified wrapper
     * @return the models saved or updated, null if none successful.
     */
    suspend fun saveAll(models: Collection<TModel>, databaseWrapper: DatabaseWrapper): Result<Collection<TModel>>

    /**
     * Inserts the specified model into the DB.
     *
     * @param model           The model to insert.
     * @param databaseWrapper The manually specified wrapper.
     */
    suspend fun insert(model: TModel, databaseWrapper: DatabaseWrapper): Result<TModel>

    /**
     * Inserts a [Collection] of models into the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseWrapper The manually specified wrapper
     * @return the inserted models.
     */
    suspend fun insertAll(models: Collection<TModel>, databaseWrapper: DatabaseWrapper): Result<Collection<TModel>>

    /**
     * Updates the specified model into the DB.
     *
     * @param model           The model to update.
     * @param databaseWrapper The manually specified wrapper.
     * @return the updated model, if successful, otherwise null.
     */
    suspend fun update(model: TModel, databaseWrapper: DatabaseWrapper): Result<TModel>

    /**
     * Updates a [Collection] of models in the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseWrapper The manually specified wrapper
     * @return successful updates, if null none were successful.
     */
    suspend fun updateAll(models: Collection<TModel>, databaseWrapper: DatabaseWrapper): Result<Collection<TModel>>

    /**
     * Deletes the model from the DB
     *
     * @param model           The model to delete
     * @param databaseWrapper The manually specified wrapper.
     */
    suspend fun delete(model: TModel, databaseWrapper: DatabaseWrapper): Result<TModel>

    /**
     * Updates a [Collection] of models in the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseWrapper The manually specified wrapper
     * @return count of successful deletions.
     */
    suspend fun deleteAll(models: Collection<TModel>, databaseWrapper: DatabaseWrapper): Result<Collection<TModel>>

    /**
     * Binds to a [DatabaseStatement] for insert.
     *
     * @param sqLiteStatement The statement to bind to.
     * @param model           The model to read from.
     */
    fun bindToInsertStatement(sqLiteStatement: DatabaseStatement, model: TModel)

    /**
     * Binds values of the model to an update [DatabaseStatement]. It repeats each primary
     * key binding twice to ensure proper update statements.
     */
    fun bindToUpdateStatement(updateStatement: DatabaseStatement, model: TModel)

    /**
     * Binds values of the model to a delete [DatabaseStatement].
     */
    fun bindToDeleteStatement(deleteStatement: DatabaseStatement, model: TModel)

    /**
     * If a model has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    fun updateAutoIncrement(model: TModel, id: Number): TModel

}
