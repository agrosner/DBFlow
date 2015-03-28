package com.raizlabs.android.dbflow.structure;

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

    private ModelClass mModel;

    AsyncModel(ModelClass referenceModel) {
        mModel = referenceModel;
    }

    @Override
    public void save() {
        TransactionManager.getInstance()
                .addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(mModel)));
    }

    @Override
    public void delete() {
        TransactionManager.getInstance()
                .addTransaction(new DeleteModelListTransaction<>(ProcessModelInfo.withModels(mModel)));
    }

    @Override
    public void update() {
        TransactionManager.getInstance()
                .addTransaction(new UpdateModelListTransaction<>(ProcessModelInfo.withModels(mModel)));
    }

    @Override
    public void insert() {
        TransactionManager.getInstance()
                .addTransaction(new InsertModelTransaction<>());
    }

    private ProcessModelInfo<ModelClass> getProcessModelInfoInternal() {
        return ProcessModelInfo.withModels(mModel).result(internalListener);
    }

    @Override
    public boolean exists() {
        return mModel.exists();
    }

    /**
     * Called when the model has finished computing.
     */
    public void onModelChanged() {

    }

    private final TransactionListener<List<ModelClass>> internalListener = new TransactionListener<List<ModelClass>>() {
        @Override
        public void onResultReceived(List<ModelClass> result) {
            onModelChanged();
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
