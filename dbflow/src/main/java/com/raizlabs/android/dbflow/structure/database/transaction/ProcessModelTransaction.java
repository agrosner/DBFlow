package com.raizlabs.android.dbflow.structure.database.transaction;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Description: Allows you to process a {@link List} of models in a transaction.
 */
public class ProcessModelTransaction<TModel extends Model> implements ITransaction {


    /**
     * Description: Simple interface for acting on a model in a Transaction or list of {@link Model}
     */
    public interface ProcessModel<ModelClass extends Model> {

        /**
         * Called when processing models
         *
         * @param model The model to process
         */
        void processModel(ModelClass model);
    }

    /**
     * Listener for providing callbacks as models are processed in this {@link ITransaction}.
     *
     * @param <TModel> The model class.
     */
    public interface OnModelProcessListener<TModel extends Model> {

        void onModelProcessed(long current, long total, TModel modifiedModel);
    }

    final OnModelProcessListener<TModel> processListener;
    final List<TModel> models;
    final ProcessModel<TModel> processModel;

    ProcessModelTransaction(Builder<TModel> builder) {
        processListener = builder.processListener;
        models = builder.models;
        processModel = builder.processModel;
    }

    @Override
    public void execute(DatabaseWrapper databaseWrapper) {
        if (models != null && models.size() > 0) {
            int size = models.size();
            for (int i = 0; i < size; i++) {
                TModel model = models.get(i);
                processModel.processModel(model);

                if (processListener != null) {
                    processListener.onModelProcessed(i, size, model);
                }
            }
        }
    }

    public static final class Builder<TModel extends Model> {

        private final ProcessModel<TModel> processModel;
        OnModelProcessListener<TModel> processListener;
        List<TModel> models = new ArrayList<>();


        public Builder(@NonNull ProcessModel<TModel> processModel) {
            this.processModel = processModel;
        }

        public Builder add(TModel model) {
            models.add(model);
            return this;
        }

        @SafeVarargs
        public final Builder addAll(TModel... models) {
            this.models.addAll(Arrays.asList(models));
            return this;
        }

        public Builder addAll(Collection<TModel> models) {
            this.models.addAll(models);
            return this;
        }

        public Builder processListener(OnModelProcessListener<TModel> processListener) {
            this.processListener = processListener;
            return this;
        }

        public ProcessModelTransaction<TModel> build() {
            return new ProcessModelTransaction<>(this);
        }
    }
}
