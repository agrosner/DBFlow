package com.raizlabs.android.dbflow.structure.database.transaction;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Description: Allows you to process a single or {@link List} of models in a transaction. You
 * can operate on a set of {@link Model} to {@link Model#save()}, {@link Model#update()}, etc.
 */
public class ProcessModelTransaction<TModel> implements ITransaction {


    /**
     * Description: Simple interface for acting on a model in a Transaction or list of {@link Model}
     */
    public interface ProcessModel<TModel> {

        /**
         * Called when processing models
         *
         * @param model   The model to process
         * @param wrapper
         */
        void processModel(TModel model, DatabaseWrapper wrapper);
    }

    /**
     * Listener for providing callbacks as models are processed in this {@link ITransaction}.
     *
     * @param <TModel> The model class.
     */
    public interface OnModelProcessListener<TModel> {

        /**
         * Called when model has been operated on.
         *
         * @param current       The current index of items processed.
         * @param total         The total number of items to process.
         * @param modifiedModel The model previously modified.
         */
        void onModelProcessed(long current, long total, TModel modifiedModel);
    }

    final OnModelProcessListener<TModel> processListener;
    final List<TModel> models;
    final ProcessModel<TModel> processModel;
    final boolean runProcessListenerOnSameThread;

    ProcessModelTransaction(Builder<TModel> builder) {
        processListener = builder.processListener;
        models = builder.models;
        processModel = builder.processModel;
        runProcessListenerOnSameThread = builder.runProcessListenerOnSameThread;
    }

    @Override
    public void execute(DatabaseWrapper databaseWrapper) {
        if (models != null) {
            final int size = models.size();
            for (int i = 0; i < size; i++) {
                final TModel model = models.get(i);
                processModel.processModel(model, databaseWrapper);

                if (processListener != null) {
                    if (runProcessListenerOnSameThread) {
                        processListener.onModelProcessed(i, size, model);
                    } else {
                        final int finalI = i;
                        Transaction.getTransactionHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                processListener.onModelProcessed(finalI, size, model);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Makes it easy to build a {@link ProcessModelTransaction}.
     *
     * @param <TModel>
     */
    public static final class Builder<TModel> {

        private final ProcessModel<TModel> processModel;
        OnModelProcessListener<TModel> processListener;
        List<TModel> models = new ArrayList<>();
        private boolean runProcessListenerOnSameThread;


        public Builder(@NonNull ProcessModel<TModel> processModel) {
            this.processModel = processModel;
        }

        /**
         * @param models       The models to process. This constructor creates a new {@link ArrayList}
         *                     from the {@link Collection} passed.
         * @param processModel The method call interface.
         */
        public Builder(Collection<TModel> models, @NonNull ProcessModel<TModel> processModel) {
            this.processModel = processModel;
            this.models = new ArrayList<>(models);
        }

        public Builder<TModel> add(TModel model) {
            models.add(model);
            return this;
        }

        /**
         * Adds all specified models to the {@link ArrayList}.
         */
        @SafeVarargs
        public final Builder<TModel> addAll(TModel... models) {
            this.models.addAll(Arrays.asList(models));
            return this;
        }

        /**
         * Adds a {@link Collection} of {@link Model} to the existing {@link ArrayList}.
         */
        public Builder<TModel> addAll(Collection<? extends TModel> models) {
            if (models != null) {
                this.models.addAll(models);
            }
            return this;
        }

        /**
         * @param processListener Allows you to listen for when models are processed to update UI,
         *                        this is called on the UI thread.
         */
        public Builder<TModel> processListener(OnModelProcessListener<TModel> processListener) {
            this.processListener = processListener;
            return this;
        }

        /**
         * @param runProcessListenerOnSameThread Default is false. If true we return callback
         *                                       on same calling thread, if false we push the callback
         *                                       to the UI thread.
         */
        public Builder<TModel> runProcessListenerOnSameThread(boolean runProcessListenerOnSameThread) {
            this.runProcessListenerOnSameThread = runProcessListenerOnSameThread;
            return this;
        }

        /**
         * @return A new {@link ProcessModelTransaction}. Subsequent calls to this method produce
         * new instances.
         */
        public ProcessModelTransaction<TModel> build() {
            return new ProcessModelTransaction<>(this);
        }
    }
}
