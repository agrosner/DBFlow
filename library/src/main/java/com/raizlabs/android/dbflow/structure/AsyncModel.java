package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.runtime.DBTransactionQueue;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.runtime.transaction.process.DeleteModelListTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.InsertModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.UpdateModelListTransaction;

import java.util.List;

/**
 * Description: Called from a {@link BaseModel}, this places the current {@link Model} interaction on the background.
 */
public class AsyncModel<ModelClass extends Model> implements Model {

    /**
     * Listens for when this {@link Model} modification completes.
     */
    public interface OnModelChangedListener {

        /**
         * Called when the change finishes on the {@link DBTransactionQueue}. This method is called on the UI thread.
         */
        void onModelChanged(Model model);
    }

    private ModelClass model;

    private OnModelChangedListener onModelChangedListener;

    AsyncModel(ModelClass referenceModel) {
        model = referenceModel;
    }

    /**
     * Call before {@link #save()}, {@link #delete()}, {@link #update()}, or {@link #insert()} since post
     * call to those the listener is nulled out.
     *
     * @param onModelChangedListener The listener to use for a corresponding call to a method.
     * @return This instance.
     */
    public AsyncModel<ModelClass> withListener(OnModelChangedListener onModelChangedListener) {
        this.onModelChangedListener = onModelChangedListener;
        return this;
    }

    @Override
    public void save() {
        TransactionManager.getInstance()
                .addTransaction(new SaveModelTransaction<>(getProcessModelInfoInternal()));
        onModelChangedListener = null;
    }

    @Override
    public void delete() {
        TransactionManager.getInstance()
                .addTransaction(new DeleteModelListTransaction<>(getProcessModelInfoInternal()));
        onModelChangedListener = null;
    }

    @Override
    public void update() {
        TransactionManager.getInstance()
                .addTransaction(new UpdateModelListTransaction<>(getProcessModelInfoInternal()));
        onModelChangedListener = null;
    }

    @Override
    public void insert() {
        TransactionManager.getInstance()
                .addTransaction(new InsertModelTransaction<>(getProcessModelInfoInternal()));
        onModelChangedListener = null;
    }

    private ProcessModelInfo<ModelClass> getProcessModelInfoInternal() {
        return ProcessModelInfo.withModels(model).result(internalListener);
    }

    @Override
    public boolean exists() {
        return model.exists();
    }

    private final TransactionListener<List<ModelClass>> internalListener = new TransactionListener<List<ModelClass>>() {
        @Override
        public void onResultReceived(List<ModelClass> result) {
            if (onModelChangedListener != null) {
                onModelChangedListener.onModelChanged(model);
            }
        }

        @Override
        public boolean onReady(BaseTransaction<List<ModelClass>> transaction) {
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<List<ModelClass>> transaction, List<ModelClass> result) {
            return true;
        }
    };
}
