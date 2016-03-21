package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;

/**
 * Description: The base implementation of {@link com.raizlabs.android.dbflow.structure.Model} using the
 * shared {@link com.raizlabs.android.dbflow.config.FlowManager}. It implements the methods so you don't have to.
 * If you wish not to extend from this class you will need to implement {@link com.raizlabs.android.dbflow.structure.Model}
 * instead.
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
    public void save() {
        getModelAdapter().save(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void delete() {
        getModelAdapter().delete(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        getModelAdapter().update(this);
    }

    /**
     * Directly tries to insert this item into the DB without updating.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void insert() {
        getModelAdapter().insert(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean exists() {
        return getModelAdapter().exists(this);
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
