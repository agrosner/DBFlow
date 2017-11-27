package com.raizlabs.android.dbflow.adapter

import android.content.ContentValues
import android.database.sqlite.SQLiteStatement
import android.support.annotation.IntRange

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.database.DatabaseStatement
import com.raizlabs.android.dbflow.database.DatabaseWrapper

/**
 * Description: Used for our internal Adapter classes such as generated [ModelAdapter].
 */
interface InternalAdapter<TModel> {

    /**
     * @return The table name of this adapter.
     */
    val tableName: String

    /**
     * Saves the specified model to the DB.
     *
     * @param model           The model to save/insert/update
     * @param databaseWrapper The manually specified wrapper.
     */
    fun save(model: TModel, databaseWrapper: DatabaseWrapper): Boolean

    /**
     * Saves a [Collection] of models to the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseWrapper The manually specified wrapper
     * @return the count of models saved or updated.
     */
    fun saveAll(models: Collection<TModel>, databaseWrapper: DatabaseWrapper): Long

    /**
     * Inserts the specified model into the DB.
     *
     * @param model           The model to insert.
     * @param databaseWrapper The manually specified wrapper.
     */
    fun insert(model: TModel, databaseWrapper: DatabaseWrapper): Long

    /**
     * Inserts a [Collection] of models into the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseWrapper The manually specified wrapper
     * @return the count inserted
     */
    fun insertAll(models: Collection<TModel>, databaseWrapper: DatabaseWrapper): Long

    /**
     * Updates the specified model into the DB.
     *
     * @param model           The model to update.
     * @param databaseWrapper The manually specified wrapper.
     */
    fun update(model: TModel, databaseWrapper: DatabaseWrapper): Boolean

    /**
     * Updates a [Collection] of models in the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseWrapper The manually specified wrapper
     * @return count of successful updates.
     */
    fun updateAll(models: Collection<TModel>, databaseWrapper: DatabaseWrapper): Long

    /**
     * Deletes the model from the DB
     *
     * @param model           The model to delete
     * @param databaseWrapper The manually specified wrapper.
     */
    fun delete(model: TModel, databaseWrapper: DatabaseWrapper): Boolean

    /**
     * Updates a [Collection] of models in the DB.
     *
     * @param models          The [Collection] of models to save.
     * @param databaseWrapper The manually specified wrapper
     * @return count of successful deletions.
     */
    fun deleteAll(models: Collection<TModel>, databaseWrapper: DatabaseWrapper): Long

    /**
     * Binds a [TModel] to the specified db statement
     *
     * @param sqLiteStatement The statement to fill
     */
    fun bindToStatement(sqLiteStatement: DatabaseStatement, model: TModel)

    /**
     * Provides common logic and a starting value for insert statements. So we can property compile
     * and bind statements without generating duplicate code.
     *
     * @param sqLiteStatement The statement to bind.
     * @param model           The model to retrieve data from.
     * @param start           The starting index for this bind.
     */
    fun bindToInsertStatement(sqLiteStatement: DatabaseStatement, model: TModel,
                              @IntRange(from = 0, to = 1) start: Int)

    /**
     * Binds to a [SQLiteStatement]. It leaves out an autoincrementing primary key (if specified)
     * to keep the true nature of AI keys.
     *
     * @param sqLiteStatement The statement to bind to.
     * @param model           The model to read from.
     */
    fun bindToInsertStatement(sqLiteStatement: DatabaseStatement, model: TModel)

    /**
     * Binds a [TModel] to the specified db statement
     *
     * @param contentValues The content values to fill.
     * @param model         The model values to put on the contentvalues
     */
    fun bindToContentValues(contentValues: ContentValues, model: TModel)

    /**
     * Binds a [TModel] to the specified db statement, leaving out the [PrimaryKey.autoincrement]
     * column.
     *
     * @param contentValues The content values to fill.
     * @param model         The model values to put on the content values.
     */
    fun bindToInsertValues(contentValues: ContentValues, model: TModel)

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
    fun updateAutoIncrement(model: TModel, id: Number)

    /**
     * @return The value for the [PrimaryKey.autoincrement]
     * if it has the field. This method is overridden when its specified for the [TModel]
     */
    fun getAutoIncrementingId(model: TModel): Number?

    /**
     * @return true if the [InternalAdapter] can be cached.
     */
    fun cachingEnabled(): Boolean
}
