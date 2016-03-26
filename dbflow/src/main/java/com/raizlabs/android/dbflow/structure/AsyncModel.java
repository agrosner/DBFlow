package com.raizlabs.android.dbflow.structure;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.lang.ref.WeakReference;

/**
 * Description: Called from a {@link BaseModel}, this places the current {@link Model} interaction on the background.
 */
public class AsyncModel<TModel extends Model> implements Model {

    /**
     * Listens for when this {@link Model} modification completes.
     */
    public interface OnModelChangedListener {

        /**
         * Called when the change finishes on the {@link DefaultTransactionQueue}. This method is called on the UI thread.
         */
        void onModelChanged(Model model);
    }

    private final TModel model;
    private transient WeakReference<OnModelChangedListener> onModelChangedListener;
    private final DatabaseDefinition databaseDefinition;
    private Transaction.Success successCallback;
    private Transaction.Error errorCallback;

    public AsyncModel(@NonNull TModel referenceModel) {
        model = referenceModel;
        databaseDefinition = FlowManager.getDatabaseForTable(model.getClass());
    }

    /**
     * Call before {@link #save()}, {@link #delete()}, {@link #update()}, or {@link #insert()} since post
     * call to those the listener is nulled out.
     *
     * @param onModelChangedListener The listener to use for a corresponding call to a method.
     */
    public AsyncModel<TModel> withListener(OnModelChangedListener onModelChangedListener) {
        this.onModelChangedListener = new WeakReference<>(onModelChangedListener);
        return this;
    }

    /**
     * Listen for any errors that occur during operations on this {@link AsyncModel}.
     */
    public AsyncModel<TModel> error(Transaction.Error errorCallback) {
        this.errorCallback = errorCallback;
        return this;
    }

    /**
     * Listens for successes on this {@link AsyncModel}. Will return the {@link Transaction}.
     */
    public AsyncModel<TModel> success(Transaction.Success success) {
        this.successCallback = success;
        return this;
    }

    @Override
    public void save() {
        databaseDefinition
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<TModel>() {
                            @Override
                            public void processModel(TModel model) {
                                model.save();
                            }
                        }).build())
                .error(error)
                .success(success)
                .build().execute();
    }

    @Override
    public void delete() {
        databaseDefinition
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<TModel>() {
                            @Override
                            public void processModel(TModel model) {
                                model.delete();
                            }
                        }).build())
                .error(error)
                .success(success)
                .build().execute();
    }

    @Override
    public void update() {
        databaseDefinition
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<TModel>() {
                            @Override
                            public void processModel(TModel model) {
                                model.update();
                            }
                        }).build())
                .error(error)
                .success(success)
                .build().execute();
    }

    @Override
    public void insert() {
        databaseDefinition
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<TModel>() {
                            @Override
                            public void processModel(TModel model) {
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
            if (successCallback != null) {
                successCallback.onSuccess(transaction);
            }
            if (onModelChangedListener != null && onModelChangedListener.get() != null) {
                onModelChangedListener.get().onModelChanged(model);
            }
        }
    };
}
