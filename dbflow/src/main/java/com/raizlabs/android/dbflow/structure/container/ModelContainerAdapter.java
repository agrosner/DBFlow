package com.raizlabs.android.dbflow.structure.container;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.config.TableConfig;
import com.raizlabs.android.dbflow.sql.queriable.ModelContainerLoader;
import com.raizlabs.android.dbflow.sql.saveable.ListModelSaver;
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver;
import com.raizlabs.android.dbflow.structure.InternalAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.RetrievalAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: The base class that generated {@link ModelContainerAdapter} implement
 * to provide the necessary interactions.
 */
public abstract class ModelContainerAdapter<TModel extends Model>
        extends RetrievalAdapter<ModelContainer<TModel, ?>, TModel>
        implements InternalAdapter<ModelContainer<TModel, ?>> {

    private ModelContainerLoader<TModel> modelContainerLoader;
    private ModelSaver<TModel, ModelContainer<TModel, ?>, ModelContainerAdapter<TModel>> modelSaver;
    private ListModelSaver<TModel, ModelContainer<TModel, ?>, ModelContainerAdapter<TModel>> listModelSaver;
    private ModelAdapter<TModel> modelAdapter;

    protected final Map<String, Class> columnMap = new HashMap<>();

    public ModelContainerAdapter(DatabaseDefinition databaseDefinition) {
        super(databaseDefinition);

        TableConfig<TModel> tableConfig = getTableConfig();
        if (tableConfig != null) {
            if (tableConfig.modelContainerLoader() != null) {
                modelContainerLoader = tableConfig.modelContainerLoader();
            }
            if (tableConfig.modelContainerModelSaver() != null) {
                modelSaver = tableConfig.modelContainerModelSaver();
                modelSaver.setAdapter(this);
                modelSaver.setModelAdapter(getModelAdapter());
            }
        }
    }

    /**
     * Saves the container to the DB.
     *
     * @param modelContainer The container to read data from into {@link android.content.ContentValues}
     */
    @Override
    public void save(ModelContainer<TModel, ?> modelContainer) {
        getModelSaver().save(modelContainer);
    }

    @Override
    public void save(ModelContainer<TModel, ?> model, DatabaseWrapper databaseWrapper) {
        getModelSaver().save(model, databaseWrapper);
    }

    @Override
    public void saveAll(Collection<ModelContainer<TModel, ?>> modelContainers) {
        getListModelSaver().saveAll(modelContainers);
    }

    @Override
    public void saveAll(Collection<ModelContainer<TModel, ?>> modelContainers, DatabaseWrapper databaseWrapper) {
        getListModelSaver().saveAll(modelContainers, databaseWrapper);
    }

    @Override
    public void insert(ModelContainer<TModel, ?> modelContainer) {
        getModelSaver().insert(modelContainer);
    }

    @Override
    public void insert(ModelContainer<TModel, ?> model, DatabaseWrapper databaseWrapper) {
        getModelSaver().insert(model, databaseWrapper);
    }

    @Override
    public void insertAll(Collection<ModelContainer<TModel, ?>> modelContainers) {
        getListModelSaver().insertAll(modelContainers);
    }

    @Override
    public void insertAll(Collection<ModelContainer<TModel, ?>> modelContainers, DatabaseWrapper databaseWrapper) {
        getListModelSaver().insertAll(modelContainers, databaseWrapper);
    }

    @Override
    public void update(ModelContainer<TModel, ?> modelContainer) {
        getModelSaver().update(modelContainer);
    }

    @Override
    public void update(ModelContainer<TModel, ?> model, DatabaseWrapper databaseWrapper) {
        getModelSaver().update(model, databaseWrapper);
    }

    @Override
    public void updateAll(Collection<ModelContainer<TModel, ?>> modelContainers) {
        getListModelSaver().updateAll(modelContainers);
    }

    @Override
    public void updateAll(Collection<ModelContainer<TModel, ?>> modelContainers, DatabaseWrapper databaseWrapper) {
        getListModelSaver().updateAll(modelContainers, databaseWrapper);
    }

    @Override
    public void delete(ModelContainer<TModel, ?> modelContainer) {
        getModelSaver().delete(modelContainer);
    }

    @Override
    public void delete(ModelContainer<TModel, ?> model, DatabaseWrapper databaseWrapper) {
        getModelSaver().delete(model, databaseWrapper);
    }

    public ModelSaver<TModel, ModelContainer<TModel, ?>, ModelContainerAdapter<TModel>> getModelSaver() {
        if (modelSaver == null) {
            modelSaver = new ModelSaver<>();
            modelSaver.setModelAdapter(getModelAdapter());
            modelSaver.setAdapter(this);
        }
        return modelSaver;
    }

    public ListModelSaver<TModel, ModelContainer<TModel, ?>, ModelContainerAdapter<TModel>> getListModelSaver() {
        if (listModelSaver == null) {
            listModelSaver = new ListModelSaver<>(getModelSaver());
        }
        return listModelSaver;
    }

    public ModelAdapter<TModel> getModelAdapter() {
        if (modelAdapter == null) {
            modelAdapter = FlowManager.getModelAdapter(getModelClass());
        }
        return modelAdapter;
    }

    /**
     * Sets how this {@link ModelContainerAdapter} saves its objects.
     *
     * @param modelSaver The saver to use.
     */
    public void setModelSaver(ModelSaver<TModel, ModelContainer<TModel, ?>, ModelContainerAdapter<TModel>> modelSaver) {
        this.modelSaver = modelSaver;
    }

    /**
     * Converts the container into a {@link TModel}
     *
     * @param modelContainer The container to read data from into a {@link TModel}
     * @return a new model instance.
     */
    public abstract TModel toModel(ModelContainer<TModel, ?> modelContainer);

    /**
     * Converts the {@link TModel} into a {@link ForeignKeyContainer} by appending only its primary
     * keys to the container. This is mostly for convenience.
     *
     * @param model the model to convert.
     * @return A new {@link ForeignKeyContainer} from the {@link TModel}.
     */
    public abstract ForeignKeyContainer<TModel> toForeignKeyContainer(TModel model);

    /**
     * If a {@link com.raizlabs.android.dbflow.structure.Model} has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param modelContainer The model container object to store the key
     * @param id             The key to store
     */
    @Override
    public void updateAutoIncrement(ModelContainer<TModel, ?> modelContainer, Number id) {

    }

    /**
     * @param modelContainer The model container object to read primary key autoincrement from
     * @return The value of the {@link PrimaryKey#autoincrement()} if there is one.
     */
    @Override
    public Number getAutoIncrementingId(ModelContainer<TModel, ?> modelContainer) {
        return 0;
    }

    @Override
    public boolean cachingEnabled() {
        return false;
    }

    @Override
    public void bindToInsertStatement(DatabaseStatement sqLiteStatement, ModelContainer<TModel, ?> model) {
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

    public ModelContainerLoader<TModel> getModelContainerLoader() {
        if (modelContainerLoader == null) {
            modelContainerLoader = createModelContainerLoader();
        }
        return modelContainerLoader;
    }

    protected ModelContainerLoader<TModel> createModelContainerLoader() {
        return new ModelContainerLoader<>(getModelClass());
    }

    /**
     * Override the {@link ModelContainerLoader} with your own implementation (if necessary).
     *
     * @param modelContainerLoader The loader used to load {@link Cursor} data into a {@link ModelContainer}.
     */
    public void setModelContainerLoader(ModelContainerLoader<TModel> modelContainerLoader) {
        this.modelContainerLoader = modelContainerLoader;
    }
}
