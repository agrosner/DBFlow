package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;

/**
 * Description: The base implementation of {@link com.raizlabs.android.dbflow.structure.Model} using the
 * shared {@link com.raizlabs.android.dbflow.config.FlowManager}. It implements the methods so you don't have to.
 * If you wish not to extend from this class you will need to implement {@link com.raizlabs.android.dbflow.structure.Model}
 * instead.
 */
public abstract class BaseModel implements Model {

    /**
     * Specifies the Action that was taken when data changes
     */
    public enum Action {

        /**
         * The model called {@link #save(boolean)}
         */
        SAVE,

        /**
         * The model called {@link #insert(boolean)}
         */
        INSERT,

        /**
         * The model called {@link #update(boolean)}
         */
        UPDATE,

        /**
         * The model called {@link #delete(boolean)}
         */
        DELETE,

        /**
         * The model was changed. used in prior to {@link android.os.Build.VERSION_CODES#JELLY_BEAN_MR1}
         */
        CHANGE
    }

    private ModelAdapter mModelAdapter;

    public BaseModel() {
        mModelAdapter = FlowManager.getModelAdapter(getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void save(boolean async) {
        mModelAdapter.save(async, this, SqlUtils.SAVE_MODE_DEFAULT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void delete(boolean async) {
        mModelAdapter.delete(async, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(boolean async) {
        mModelAdapter.update(async, this);
    }

    /**
     * Directly tries to insert this item into the DB without updating.
     *
     * @param async If we want this to happen on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void insert(boolean async) {
        mModelAdapter.insert(async, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean exists() {
        return mModelAdapter.exists(this);
    }

}
