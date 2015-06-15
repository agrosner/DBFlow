package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.DBTransactionQueue;

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

    private ModelAdapter mModelAdapter;

    public BaseModel() {
        mModelAdapter = FlowManager.getModelAdapter(getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void save() {
        mModelAdapter.save(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void delete() {
        mModelAdapter.delete(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        mModelAdapter.update(this);
    }

    /**
     * Directly tries to insert this item into the DB without updating.
     *
     */
    @SuppressWarnings("unchecked")
    @Override
    public void insert() {
        mModelAdapter.insert(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean exists() {
        return mModelAdapter.exists(this);
    }

    /**
     * @return An async instance of this model where all transactions are on the {@link DBTransactionQueue}
     */
    public AsyncModel<BaseModel> async() {
        return new AsyncModel<>(this);
    }

    public ModelAdapter getModelAdapter() {
        return mModelAdapter;
    }
}
