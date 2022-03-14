package com.dbflow5.adapter

import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseConnection

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
     * @param databaseConnection The manually specified wrapper.
     * @return model if save was successful, else null.
     */
    fun save(model: TModel, databaseConnection: DatabaseConnection): Result<TModel>

    fun jvmSave(model: TModel, databaseConnection: DatabaseConnection): TModel? {
        val result = save(model, databaseConnection)
        return result.getOrNull()
    }

    /**
     * Saves a [Collection] of models to the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseConnection The manually specified wrapper
     * @return the models saved or updated, null if none successful.
     */
    fun saveAll(
        models: Collection<TModel>,
        databaseConnection: DatabaseConnection
    ): Result<Collection<TModel>>

    /**
     * Inserts the specified model into the DB.
     *
     * @param model           The model to insert.
     * @param databaseConnection The manually specified wrapper.
     */
    fun insert(model: TModel, databaseConnection: DatabaseConnection): Result<TModel>

    /**
     * Inserts a [Collection] of models into the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseConnection The manually specified wrapper
     * @return the inserted models.
     */
    fun insertAll(
        models: Collection<TModel>,
        databaseConnection: DatabaseConnection
    ): Result<Collection<TModel>>

    /**
     * Updates the specified model into the DB.
     *
     * @param model           The model to update.
     * @param databaseConnection The manually specified wrapper.
     * @return the updated model, if successful, otherwise null.
     */
    fun update(model: TModel, databaseConnection: DatabaseConnection): Result<TModel>

    /**
     * Updates a [Collection] of models in the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseConnection The manually specified wrapper
     * @return successful updates, if null none were successful.
     */
    fun updateAll(
        models: Collection<TModel>,
        databaseConnection: DatabaseConnection
    ): Result<Collection<TModel>>

    /**
     * Deletes the model from the DB
     *
     * @param model           The model to delete
     * @param databaseConnection The manually specified wrapper.
     */
    fun delete(model: TModel, databaseConnection: DatabaseConnection): Result<TModel>

    /**
     * Updates a [Collection] of models in the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseConnection The manually specified wrapper
     * @return count of successful deletions.
     */
    fun deleteAll(
        models: Collection<TModel>,
        databaseConnection: DatabaseConnection
    ): Result<Collection<TModel>>

    /**
     * Binds to a [DatabaseStatement] for insert.
     *
     * @param statement The statement to bind to.
     * @param model     The model to read from.
     */
    fun bindToInsertStatement(statement: DatabaseStatement, model: TModel)

    /**
     * Binds values of the model to an update [DatabaseStatement]. It repeats each primary
     * key binding twice to ensure proper update statements.
     */
    fun bindToUpdateStatement(statement: DatabaseStatement, model: TModel)

    /**
     * Binds values of the model to a delete [DatabaseStatement].
     */
    fun bindToDeleteStatement(statement: DatabaseStatement, model: TModel)

    /**
     * If a model has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    fun updateAutoIncrement(model: TModel, id: Number): TModel

}
