package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.config.TableConfig;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.FlowCursor;

/**
 * Description: Provides a base retrieval class for all {@link Model} backed
 * adapters.
 */
@SuppressWarnings("NullableProblems")
public abstract class RetrievalAdapter<TModel> {

    private SingleModelLoader<TModel> singleModelLoader;
    private ListModelLoader<TModel> listModelLoader;

    private TableConfig<TModel> tableConfig;

    public RetrievalAdapter(@NonNull DatabaseDefinition databaseDefinition) {
        DatabaseConfig databaseConfig = FlowManager.getConfig()
                .getConfigForDatabase(databaseDefinition.getAssociatedDatabaseClassFile());
        if (databaseConfig != null) {
            tableConfig = databaseConfig.getTableConfigForTable(getModelClass());
            if (tableConfig != null) {
                if (tableConfig.getSingleModelLoader() != null) {
                    singleModelLoader = tableConfig.getSingleModelLoader();
                }

                if (tableConfig.getListModelLoader() != null) {
                    listModelLoader = tableConfig.getListModelLoader();
                }
            }
        }
    }

    /**
     * Force loads the model from the DB. Even if caching is enabled it will requery the object.
     */
    public void load(@NonNull TModel model) {
        load(model, FlowManager.getDatabaseForTable(getModelClass()).getWritableDatabase());
    }

    /**
     * Force loads the model from the DB. Even if caching is enabled it will requery the object.
     */
    public void load(@NonNull TModel model, DatabaseWrapper databaseWrapper) {
        getNonCacheableSingleModelLoader().load(databaseWrapper,
                SQLite.INSTANCE.select()
                        .from(getModelClass())
                        .where(getPrimaryConditionClause(model)).getQuery(),
                model);
    }

    /**
     * Assigns the {@link Cursor} data into the specified {@link TModel}
     *
     * @param model  The model to assign cursor data to
     * @param cursor The cursor to load into the model
     */
    public abstract void loadFromCursor(@NonNull FlowCursor cursor, @NonNull TModel model);

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    public boolean exists(@NonNull TModel model) {
        return exists(model, FlowManager.getDatabaseForTable(getModelClass()).getWritableDatabase());
    }

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    public abstract boolean exists(@NonNull TModel model,
                                   @NonNull DatabaseWrapper databaseWrapper);

    /**
     * @param model The primary condition clause.
     * @return The clause that contains necessary primary conditions for this table.
     */
    public abstract OperatorGroup getPrimaryConditionClause(@NonNull TModel model);

    /**
     * @return the model class this adapter corresponds to
     */
    @NonNull
    public abstract Class<TModel> getModelClass();

    @Nullable
    protected TableConfig<TModel> getTableConfig() {
        return tableConfig;
    }

    /**
     * @return A new {@link ListModelLoader}, caching will override this loader instance.
     */
    @NonNull
    public ListModelLoader<TModel> getListModelLoader() {
        if (listModelLoader == null) {
            listModelLoader = createListModelLoader();
        }
        return listModelLoader;
    }

    /**
     * @return A new {@link ListModelLoader}, caching will override this loader instance.
     */
    @NonNull
    protected ListModelLoader<TModel> createListModelLoader() {
        return new ListModelLoader<>(getModelClass());
    }

    /**
     * @return A new {@link SingleModelLoader}, caching will override this loader instance.
     */
    @NonNull
    protected SingleModelLoader<TModel> createSingleModelLoader() {
        return new SingleModelLoader<>(getModelClass());
    }

    @NonNull
    public SingleModelLoader<TModel> getSingleModelLoader() {
        if (singleModelLoader == null) {
            singleModelLoader = createSingleModelLoader();
        }
        return singleModelLoader;
    }

    /**
     * @return A new instance of a {@link SingleModelLoader}. Subsequent calls do not cache
     * this object so it's recommended only calling this in bulk if possible.
     */
    @NonNull
    public SingleModelLoader<TModel> getNonCacheableSingleModelLoader() {
        return new SingleModelLoader<>(getModelClass());
    }

    /**
     * @return A new instance of a {@link ListModelLoader}. Subsequent calls do not cache
     * this object so it's recommended only calling this in bulk if possible.
     */
    @NonNull
    public ListModelLoader<TModel> getNonCacheableListModelLoader() {
        return new ListModelLoader<>(getModelClass());
    }

    /**
     * Overrides the default implementation and allows you to provide your own implementation. Defines
     * how a single {@link TModel} is loaded.
     *
     * @param singleModelLoader The loader to use.
     */
    public void setSingleModelLoader(@NonNull SingleModelLoader<TModel> singleModelLoader) {
        this.singleModelLoader = singleModelLoader;
    }

    /**
     * Overrides the default implementation and allows you to provide your own implementation. Defines
     * how a list of {@link TModel} are loaded.
     *
     * @param listModelLoader The loader to use.
     */
    public void setListModelLoader(@NonNull ListModelLoader<TModel> listModelLoader) {
        this.listModelLoader = listModelLoader;
    }

}
