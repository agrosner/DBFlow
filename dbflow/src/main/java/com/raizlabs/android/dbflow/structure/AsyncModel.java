package com.raizlabs.android.dbflow.structure;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.lang.ref.WeakReference;

/**
 * Description: Called from a {@link BaseModel}, this places the current {@link Model} interaction on the background.
 */
public class AsyncModel<ModelClass extends Model> implements Model {

    /**
     * Listens for when this {@link Model} modification completes.
     */
    public interface OnModelChangedListener {

        /**
         * Called when the change finishes on the {@link DefaultTransactionQueue}. This method is called on the UI thread.
         */
        void onModelChanged(Model model);
    }

    private final ModelClass model;
    private transient WeakReference<OnModelChangedListener> onModelChangedListener;
    private final BaseDatabaseDefinition databaseDefinition;

    private Transaction.Error errorCallback;

    public AsyncModel(@NonNull ModelClass referenceModel) {
        model = referenceModel;
        databaseDefinition = FlowManager.getDatabaseForTable(model.getClass());
    }

    /**
     * Call before {@link #save()}, {@link #delete()}, {@link #update()}, or {@link #insert()} since post
     * call to those the listener is nulled out.
     *
     * @param onModelChangedListener The listener to use for a corresponding call to a method.
     * @return This instance.
     */
    public AsyncModel<ModelClass> withListener(OnModelChangedListener onModelChangedListener) {
        this.onModelChangedListener = new WeakReference<>(onModelChangedListener);
        return this;
    }

    /**
     * Listen for any errors that occur during operations on this {@link AsyncModel}.
     *
     * @param errorCallback The error callback.
     * @return This instance.
     */
    public AsyncModel<ModelClass> error(Transaction.Error errorCallback) {
        this.errorCallback = errorCallback;
        return this;
    }

    @Override
    public void save() {
        databaseDefinition.getWritableDatabase()
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<ModelClass>() {
                            @Override
                            public void processModel(ModelClass model) {
                                model.save();
                            }
                        }).build())
                .error(error)
                .success(success)
                .build().execute();
    }

    @Override
    public void delete() {
        databaseDefinition.getWritableDatabase()
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<ModelClass>() {
                            @Override
                            public void processModel(ModelClass model) {
                                model.delete();
                            }
                        }).build())
                .error(error)
                .success(success)
                .build().execute();
    }

    @Override
    public void update() {
        databaseDefinition.getWritableDatabase()
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<ModelClass>() {
                            @Override
                            public void processModel(ModelClass model) {
                                model.update();
                            }
                        }).build())
                .error(error)
                .success(success)
                .build().execute();
    }

    @Override
    public void insert() {
        databaseDefinition.getWritableDatabase()
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<ModelClass>() {
                            @Override
                            public void processModel(ModelClass model) {
                                model.insert();
                            }
                        }).build())
                .error(error)
                .success(success)
                .build().execute();
    }

    @Override
    public boolean exists() {
        return model.exists();
    }

    private final Transaction.Error error = new Transaction.Error() {
        @Override
        public void onError(Transaction transaction, Throwable error) {
            if (errorCallback != null) {
                errorCallback.onError(transaction, error);
            }
        }
    };

    private final Transaction.Success success = new Transaction.Success() {
        @Override
        public void onSuccess(Transaction transaction) {
            if (onModelChangedListener != null && onModelChangedListener.get() != null) {
                onModelChangedListener.get().onModelChanged(model);
            }
        }
    };
}
