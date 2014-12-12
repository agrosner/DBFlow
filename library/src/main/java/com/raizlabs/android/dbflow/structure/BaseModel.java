package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: The base implementation of {@link com.raizlabs.android.dbflow.structure.Model} using the
 * shared {@link com.raizlabs.android.dbflow.config.FlowManager}. It implements the methods so you don't have to.
 * If you wish not to extend from this class you will need to implement {@link com.raizlabs.android.dbflow.structure.Model}
 * instead.
 */
public abstract class BaseModel implements Model {

    private ModelAdapter mModelAdapter;

    public BaseModel() {
        mModelAdapter = FlowManager.getModelAdapter(getClass());
    }

    @Override
    public void save(boolean async) {
       mModelAdapter.save(async, this, SqlUtils.SAVE_MODE_DEFAULT);
    }

    @Override
    public void delete(boolean async) {
       mModelAdapter.delete(async, this);
    }

    @Override
    public void update(boolean async) {
        mModelAdapter.save(async, this, SqlUtils.SAVE_MODE_UPDATE);
    }

    /**
     * Directly tries to insert this item into the DB without updating.
     *
     * @param async If we want this to happen on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     */
    @Override
    public void insert(boolean async) {
        mModelAdapter.save(async, this, SqlUtils.SAVE_MODE_INSERT);
    }

    @Override
    public boolean exists() {
        return mModelAdapter.exists(this);
    }

    public enum Action {
        SAVE,
        INSERT,
        UPDATE,
        DELETE;
    }
}
