package com.raizlabs.android.dbflow.runtime;

import com.raizlabs.android.dbflow.structure.Model;

import java.util.Collection;

/**
 * Description: This class manages batch database interactions. It is useful for retrieving, updating, saving,
 * and deleting lists of items. The bulk of DB operations should exist in this class.
 */
public class DefaultTransactionManager extends BaseTransactionManager {

    public DefaultTransactionManager() {
        super(new DefaultTransactionQueue("DBFlow Transaction Queue"));
    }

    public DefaultTransactionManager(ITransactionQueue transactionQueue) {
        super(transactionQueue);
    }

    /**
     * Saves the passed in model to the {@link com.raizlabs.android.dbflow.runtime.DBBatchSaveQueue}.
     * This method is recommended for saving large amounts of continuous data as to batch up as much data as possible in a save.
     *
     * @param model        The model to save
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void saveOnSaveQueue(ModelClass model) {

        // Only start save queue if we are going to use it
        if (!getSaveQueue().isAlive()) {
            getSaveQueue().start();
        }
        getSaveQueue().add(model);
    }

    // endregion

    // region Database Save methods

    public DBBatchSaveQueue getSaveQueue() {
        return DBBatchSaveQueue.getSharedSaveQueue();
    }

    /**
     * Saves all of the passed in models to the {@link com.raizlabs.android.dbflow.runtime.DBBatchSaveQueue}.
     * This method is recommended for saving large amounts of continuous data as to batch up as much data as possible in a save.
     *
     * @param models       The list of models to save
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void saveOnSaveQueue(Collection<ModelClass> models) {

        // Only start save queue if we are going to use it
        if (!getSaveQueue().isAlive()) {
            getSaveQueue().start();
        }
        getSaveQueue().addAll(models);
    }

    // endregion
}
