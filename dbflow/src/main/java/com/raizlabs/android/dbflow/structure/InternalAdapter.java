package com.raizlabs.android.dbflow.structure;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.IntRange;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.container.ModelContainerAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Used for our internal Adapter classes such as generated {@link ModelAdapter}
 * or {@link ModelContainerAdapter}
 */
public interface InternalAdapter<TableClass extends Model, ModelClass extends Model> {

    /**
     * @return The table name of this adapter.
     */
    String getTableName();

    /**
     * Saves the specified model to the DB.
     *
     * @param model The model to save/insert/update
     */
    void save(ModelClass model);

    /**
     * Saves the specified model to the DB.
     *
     * @param model           The model to save/insert/update
     * @param databaseWrapper The manually specified wrapper.
     */
    void save(ModelClass model, DatabaseWrapper databaseWrapper);

    /**
     * Inserts the specified model into the DB.
     *
     * @param model The model to insert.
     */
    void insert(ModelClass model);

    /**
     * Inserts the specified model into the DB.
     *
     * @param model           The model to insert.
     * @param databaseWrapper The manually specified wrapper.
     */
    void insert(ModelClass model, DatabaseWrapper databaseWrapper);

    /**
     * Updates the specified model into the DB.
     *
     * @param model The model to update.
     */
    void update(ModelClass model);

    /**
     * Updates the specified model into the DB.
     *
     * @param model           The model to update.
     * @param databaseWrapper The manually specified wrapper.
     */
    void update(ModelClass model, DatabaseWrapper databaseWrapper);

    /**
     * Deletes the model from the DB
     *
     * @param model The model to delete
     */
    void delete(ModelClass model);

    /**
     * Deletes the model from the DB
     *
     * @param model           The model to delete
     * @param databaseWrapper The manually specified wrapper.
     */
    void delete(ModelClass model, DatabaseWrapper databaseWrapper);

    /**
     * Binds a {@link ModelClass} to the specified db statement
     *
     * @param sqLiteStatement The statement to fill
     */
    void bindToStatement(DatabaseStatement sqLiteStatement, ModelClass model);

    /**
     * Provides common logic and a starting value for insert statements. So we can property compile
     * and bind statements without generating duplicate code.
     *
     * @param sqLiteStatement The statement to bind.
     * @param model           The model to retrieve data from.
     * @param start           The starting index for this bind.
     */
    void bindToInsertStatement(DatabaseStatement sqLiteStatement, ModelClass model,
                               @IntRange(from = 0, to = 1) int start);

    /**
     * Binds to a {@link SQLiteStatement}. It leaves out an autoincrementing primary key (if specified)
     * to keep the true nature of AI keys.
     *
     * @param sqLiteStatement The statement to bind to.
     * @param model           The model to read from.
     */
    void bindToInsertStatement(DatabaseStatement sqLiteStatement, ModelClass model);

    /**
     * Binds a {@link ModelClass} to the specified db statement
     *
     * @param contentValues The content values to fill.
     * @param model         The model values to put on the contentvalues
     */
    void bindToContentValues(ContentValues contentValues, ModelClass model);

    /**
     * Binds a {@link ModelClass} to the specified db statement, leaving out the {@link PrimaryKey#autoincrement()}
     * column.
     *
     * @param contentValues The content values to fill.
     * @param model         The model values to put on the content values.
     */
    void bindToInsertValues(ContentValues contentValues, ModelClass model);

    /**
     * If a {@link com.raizlabs.android.dbflow.structure.Model} has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    void updateAutoIncrement(ModelClass model, Number id);

    /**
     * @return The value for the {@link com.raizlabs.android.dbflow.annotation.Column#PRIMARY_KEY_AUTO_INCREMENT}
     * if it has the field. This method is overridden when its specified for the {@link ModelClass}
     */
    Number getAutoIncrementingId(ModelClass model);

    /**
     * @return true if the {@link InternalAdapter} can be cached.
     */
    boolean cachingEnabled();
}
