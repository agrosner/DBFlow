package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Description: Holds information regarding how to handle a list of {@link com.raizlabs.android.dbflow.structure.Model}
 * . Typically used in a {@link com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelTransaction}
 */
public class ProcessModelInfo<ModelClass extends Model> {

    List<ModelClass> mModels = new ArrayList<ModelClass>();

    TransactionListener<List<ModelClass>> mTransactionListener;

    DBTransactionInfo mInfo;

    Class<ModelClass> mTable;

    /**
     * Required to construct this information with Models
     */
    ProcessModelInfo() {
    }

    /**
     * Creates a new instance with the specified models
     *
     * @param models       The varg of models to use
     * @param <ModelClass>
     * @return New instance with the specified models.
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <ModelClass extends Model> ProcessModelInfo<ModelClass> withModels(ModelClass... models) {
        return new ProcessModelInfo<ModelClass>()
                .models(models);
    }

    /**
     * Creates a new instance with the specified models
     *
     * @param models       The collection of models to use.
     * @param <ModelClass>
     * @return New instance with specified models
     */
    public static <ModelClass extends Model> ProcessModelInfo<ModelClass> withModels(Collection<ModelClass> models) {
        return new ProcessModelInfo<ModelClass>()
                .models(models);
    }

    /**
     * Adds the specified {@link ModelClass} into the {@link java.util.List} of models in this class.
     *
     * @param models The list of models to add
     * @return This instance
     */
    @SuppressWarnings("unchecked")
    public ProcessModelInfo<ModelClass> models(ModelClass... models) {
        mModels.addAll(Arrays.asList(models));
        if (models.length > 0) {
            Class modelClass = models[0].getClass();
            if (ModelContainer.class.isAssignableFrom(modelClass)) {
                mTable = ((ModelContainer) models[0]).getTable();
            } else {
                mTable = (Class<ModelClass>) modelClass;
            }
        }
        return this;
    }

    /**
     * Adds a {@link java.util.Collection} of models to the {@link java.util.List} of models in this class.
     *
     * @param models The collection of models to append.
     * @return This instance
     */
    @SuppressWarnings("unchecked")
    public ProcessModelInfo<ModelClass> models(Collection<ModelClass> models) {
        mModels.addAll(models);
        if (models != null && models.size() > 0) {
            ArrayList<ModelClass> modelList = new ArrayList<>(models);
            Class modelClass = modelList.get(0).getClass();

            if (ModelContainer.class.isAssignableFrom(modelClass)) {
                mTable = ((ModelContainer) modelList.get(0)).getTable();
            } else {
                mTable = (Class<ModelClass>) modelClass;
            }
        }
        return this;
    }

    /**
     * Sets the {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener} to be called
     * back when a result has been received and during the process of the data. Note: the result is not always called unless a transaction has
     * {@link com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction#hasResult(Object)}
     *
     * @param transactionListener The callback to listener
     * @return This instance.
     */
    public ProcessModelInfo<ModelClass> result(TransactionListener<List<ModelClass>> transactionListener) {
        mTransactionListener = transactionListener;
        return this;
    }

    /**
     * Sets a {@link com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction#PRIORITY_UI} to this transaction.
     *
     * @return This instance.
     */
    public ProcessModelInfo<ModelClass> fetch() {
        return info(DBTransactionInfo.createFetch());
    }

    /**
     * Sets a specific {@link com.raizlabs.android.dbflow.runtime.DBTransactionInfo} to use for this instance.
     *
     * @param dbTransactionInfo Specifies information about this transaction
     * @return This instance.
     */
    public ProcessModelInfo<ModelClass> info(DBTransactionInfo dbTransactionInfo) {
        mInfo = dbTransactionInfo;
        return this;
    }

    public DBTransactionInfo getInfo() {
        if (mInfo == null) {
            mInfo = DBTransactionInfo.create();
        }
        return mInfo;
    }

    /**
     * @return True if there are models in this class
     */
    public boolean hasData() {
        return !mModels.isEmpty();
    }

    /**
     * Iterates through the models in this class and allows for a process on it.
     *
     * @param processModel Process model
     */
    @SuppressWarnings("unchecked")
    public void processModels(ProcessModel<ModelClass> processModel) {

        // ignore empty list.
        if(!mModels.isEmpty()) {
            Class<? extends Model> processClass = mTable;
            if (ModelContainer.class.isAssignableFrom(processClass) && !mModels.isEmpty()) {
                processClass = ((ModelContainer<ModelClass, ?>) mModels.get(0)).getTable();
            }
            ProcessModelHelper.process(processClass, mModels, processModel);
        }
    }
}
