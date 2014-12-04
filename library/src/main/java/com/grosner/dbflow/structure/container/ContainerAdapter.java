package com.grosner.dbflow.structure.container;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.structure.InternalAdapter;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelAdapter;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class ContainerAdapter<ModelClass extends Model> implements InternalAdapter<ModelClass> {

    /**
     * Loads the specified {@link android.database.Cursor} into the
     * {@link com.grosner.dbflow.structure.container.ModelContainer} .
     *
     * @param cursor         The cursor to read from
     * @param modelContainer The container to store the cursor data
     */
    public abstract void loadFromCursor(Cursor cursor, ModelContainer<ModelClass, ?> modelContainer);

    /**
     * Saves the container to the DB.
     *
     * @param async          Whether it is immediate or on {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     * @param modelContainer The container to read data from into {@link android.content.ContentValues}
     * @param saveMode       The {@link com.grosner.dbflow.sql.SqlUtils.SaveMode}
     */
    public void save(boolean async, ModelContainer<ModelClass, ?> modelContainer, int saveMode) {
        ModelContainerUtils.sync(async, modelContainer, this, saveMode);
    }

    /**
     *
     * @param sqLiteStatement
     * @param modelContainer
     */
    public abstract void bindToStatement(SQLiteStatement sqLiteStatement, ModelContainer<ModelClass, ?> modelContainer);

    public abstract void bindToContentValues(ContentValues contentValues, ModelContainer<ModelClass, ?> modelContainer);

    /**
     * @param modelContainer The container to check if exists by combining the primary keys into a query.
     * @return whether the specified container exists in the DB
     */
    public abstract boolean exists(ModelContainer<ModelClass, ?> modelContainer);

    /**
     * Deletes the specified container using the primary key values contained in it.
     *
     * @param async          Whether it is immediate or on {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     * @param modelContainer The container to delete.
     */
    public abstract void delete(boolean async, ModelContainer<ModelClass, ?> modelContainer);

    @Override
    public abstract Class<ModelClass> getModelClass();

    @Override
    public abstract String getTableName();

    /**
     * Converts the container into a {@link ModelClass}
     *
     * @param modelContainer The container to read data from into a {@link ModelClass}
     * @return a new model instance.
     */
    public abstract ModelClass toModel(ModelContainer<ModelClass, ?> modelContainer);

    /**
     * @param modelContainer
     * @return a {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder} of the primary keys
     * of the model object.
     */
    public abstract ConditionQueryBuilder<ModelClass> getPrimaryModelWhere(ModelContainer<ModelClass, ?> modelContainer);

    /**
     * Returns the type of the column for this model container. It's useful for when we do not know the exact class of the column
     * when in a {@link com.grosner.dbflow.structure.container.ModelContainer}
     *
     * @param columnName The name of the column to look up
     * @return The class that corresponds to the specified columnName
     */
    public abstract Class<?> getClassForColumn(String columnName);

}
