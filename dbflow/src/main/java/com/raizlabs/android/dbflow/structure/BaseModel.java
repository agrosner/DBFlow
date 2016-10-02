package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;

/**
 * Description: The base implementation of {@link Model}. It is recommended to use this class as
 * the base for your {@link Model}, but it is not required.
 */
public class BaseModel implements Model {

    /**
     * Specifies the Action that was taken when data changes
     */
    public enum Action {

        /**
         * The model called {@link Model#save()}
         */
        SAVE,

        /**
         * The model called {@link Model#insert()}
         */
        INSERT,

        /**
         * The model called {@link Model#update()}
         */
        UPDATE,

        /**
         * The model called {@link Model#delete()}
         */
        DELETE,

        /**
         * The model was changed. used in prior to {@link android.os.Build.VERSION_CODES#JELLY_BEAN_MR1}
         */
        CHANGE
    }

    @ColumnIgnore
    private transient ModelAdapter modelAdapter;

    @SuppressWarnings("unchecked")
    @Override
    public void load() {
        getModelAdapter().load(this);
    }

    @SuppressWarnings("unchecked")
    public void load(DatabaseWrapper databaseWrapper) {
        getModelAdapter().load(this, databaseWrapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void save() {
        getModelAdapter().save(this);
    }

    @SuppressWarnings("unchecked")
    public void save(DatabaseWrapper databaseWrapper) {
        getModelAdapter().save(this, databaseWrapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void delete() {
        getModelAdapter().delete(this);
    }

    @SuppressWarnings("unchecked")
    public void delete(DatabaseWrapper databaseWrapper) {
        getModelAdapter().delete(this, databaseWrapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        getModelAdapter().update(this);
    }

    @SuppressWarnings("unchecked")
    public void update(DatabaseWrapper databaseWrapper) {
        getModelAdapter().update(this, databaseWrapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public long insert() {
        return getModelAdapter().insert(this);
    }

    @SuppressWarnings("unchecked")
    public void insert(DatabaseWrapper databaseWrapper) {
        getModelAdapter().insert(this, databaseWrapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean exists() {
        return getModelAdapter().exists(this);
    }

    @SuppressWarnings("unchecked")
    public boolean exists(DatabaseWrapper databaseWrapper) {
        return getModelAdapter().exists(this, databaseWrapper);
    }

    /**
     * @return An async instance of this model where all transactions are on the {@link DefaultTransactionQueue}
     */
    public AsyncModel<BaseModel> async() {
        return new AsyncModel<>(this);
    }

    /**
     * @return The associated {@link ModelAdapter}. The {@link FlowManager}
     * may throw a {@link InvalidDBConfiguration} for this call if this class
     * is not associated with a table, so be careful when using this method.
     */
    public ModelAdapter getModelAdapter() {
        if (modelAdapter == null) {
            modelAdapter = FlowManager.getModelAdapter(getClass());
        }
        return modelAdapter;
    }
}
