package com.raizlabs.android.dbflow.runtime.transaction.process;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Collection;

/**
 * Description: Provides a few helper methods for handling lists of models in a structured way.
 */
public class ProcessModelHelper {

    /**
     * Runs through a {@link java.util.Collection} of {@link ModelClass} and enables singular processing of each one.
     *
     * @param modelClass   The class of the model. It may be a {@link com.raizlabs.android.dbflow.structure.container.ModelContainer} as well, so be careful.
     * @param collection   The nonnull collection of {@link ModelClass}
     * @param processModel The callback to run custom handling of the model object
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     */
    public static <ModelClass extends Model> void process(Class<? extends Model> modelClass,
                                                          @NonNull final Collection<ModelClass> collection,
                                                          final ProcessModel<ModelClass> processModel) {
        if (!collection.isEmpty()) {
            TransactionManager.transact(FlowManager.getDatabaseForTable(modelClass).getWritableDatabase(),
                    new Runnable() {
                        @Override
                        public void run() {
                            for (ModelClass collectionModel : collection) {
                                processModel.processModel(collectionModel);
                            }
                        }
                    });
        }
    }

    /**
     * Runs through a varg of {@link ModelClass} and enables singular processing of each one.
     *
     * @param modelClass   The class of the model. It may be a {@link com.raizlabs.android.dbflow.structure.container.ModelContainer} as well, so be careful.
     * @param processModel The callback to run custom handling of the model object
     * @param models       The varg list of {@link ModelClass}
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     */
    @SafeVarargs
    public static <ModelClass extends Model> void process(Class<? extends Model> modelClass,
                                                          final ProcessModel<ModelClass> processModel,
                                                          final ModelClass... models) {
        TransactionManager.transact(FlowManager.getDatabaseForTable(modelClass).getWritableDatabase(),
                new Runnable() {
                    @Override
                    public void run() {
                        for (ModelClass model : models) {
                            processModel.processModel(model);
                        }
                    }
                });
    }
}
