package com.raizlabs.android.dbflow.structure.container;

import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.queriable.ModelContainerLoader;
import com.raizlabs.android.dbflow.structure.InternalAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.RetrievalAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: The base class that generated {@link ModelContainerAdapter} implement
 * to provide the necessary interactions.
 */
public abstract class ModelContainerAdapter<ModelClass extends Model> extends RetrievalAdapter<ModelContainer<ModelClass, ?>, ModelClass> implements InternalAdapter<ModelClass, ModelContainer<ModelClass, ?>> {

    private ModelContainerLoader<ModelClass> modelContainerLoader;

    protected final Map<String, Class> columnMap = new HashMap<>();

    /**
     * Saves the container to the DB.
     *
     * @param modelContainer The container to read data from into {@link android.content.ContentValues}
     */
    @Override
    public void save(ModelContainer<ModelClass, ?> modelContainer) {
        SqlUtils.save(modelContainer, this, modelContainer.getModelAdapter());
    }

    /**
     * Inserts the specified model into the DB.
     *
     * @param modelContainer The model container to insert.
     */
    public void insert(ModelContainer<ModelClass, ?> modelContainer) {
        SqlUtils.insert(modelContainer, this, modelContainer.getModelAdapter());
    }

    /**
     * Updates the specified model into the DB.
     *
     * @param modelContainer The model to update.
     */
    public void update(ModelContainer<ModelClass, ?> modelContainer) {
        SqlUtils.update(modelContainer, this, modelContainer.getModelAdapter());
    }

    /**
     * Deletes the specified container using the primary key values contained in it.
     *
     * @param modelContainer The container to delete.
     */
    @Override
    public void delete(ModelContainer<ModelClass, ?> modelContainer) {
        SqlUtils.delete(modelContainer, this, modelContainer.getModelAdapter());
    }

    /**
     * Converts the container into a {@link ModelClass}
     *
     * @param modelContainer The container to read data from into a {@link ModelClass}
     * @return a new model instance.
     */
    public abstract ModelClass toModel(ModelContainer<ModelClass, ?> modelContainer);

    /**
     * Converts the {@link ModelClass} into a {@link ForeignKeyContainer} by appending only its primary
     * keys to the container. This is mostly for convenience.
     *
     * @param model the model to convert.
     * @return A new {@link ForeignKeyContainer} from the {@link ModelClass}.
     */
    public abstract ForeignKeyContainer<ModelClass> toForeignKeyContainer(ModelClass model);

    /**
     * If a {@link com.raizlabs.android.dbflow.structure.Model} has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param modelContainer The model container object to store the key
     * @param id             The key to store
     */
    @Override
    public void updateAutoIncrement(ModelContainer<ModelClass, ?> modelContainer, Number id) {

    }

    /**
     * @param modelContainer The model container object to read primary key autoincrement from
     * @return The value of the {@link PrimaryKey#autoincrement()} if there is one.
     */
    @Override
    public Number getAutoIncrementingId(ModelContainer<ModelClass, ?> modelContainer) {
        return 0;
    }

    @Override
    public boolean cachingEnabled() {
        return false;
    }

    @Override
    public void bindToInsertStatement(SQLiteStatement sqLiteStatement, ModelContainer<ModelClass, ?> model) {
        bindToInsertStatement(sqLiteStatement, model, 0);
    }

    @NonNull
    public Map<String, Class> getColumnMap() {
        return columnMap;
    }

    /**
     * Returns the type of the column for this model container. It's useful for when we do not know the exact class of the column
     * when in a {@link com.raizlabs.android.dbflow.structure.container.ModelContainer}
     *
     * @param columnName The name of the column to look up
     * @return The class that corresponds to the specified columnName
     */
    public Class<?> getClassForColumn(String columnName) {
        return columnMap.get(columnName);
    }

    public ModelContainerLoader<ModelClass> getModelContainerLoader() {
        if (modelContainerLoader == null) {
            modelContainerLoader = createModelContainerLoader();
        }
        return modelContainerLoader;
    }

    protected ModelContainerLoader<ModelClass> createModelContainerLoader() {
        return new ModelContainerLoader<>(getModelClass());
    }
}
