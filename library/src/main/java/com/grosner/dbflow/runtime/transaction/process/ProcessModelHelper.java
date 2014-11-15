package com.grosner.dbflow.runtime.transaction.process;

import android.support.annotation.NonNull;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: andrewgrosner
 * Description: Provides a few helper methods for handling lists of models in a structured way.
 */
public class ProcessModelHelper {

    /**
     * Runs through a {@link java.util.Collection} of {@link ModelClass} and enables singular processing of each one.
     *
     * @param collection   The nonnull collection of {@link ModelClass}
     * @param processModel The callback to run custom handling of the model object
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    public static <ModelClass extends Model> void process(Class<ModelClass> modelClass, @NonNull final Collection<ModelClass> collection, final ProcessModel<ModelClass> processModel) {
        if(!collection.isEmpty()) {
            TransactionManager.transact(FlowManager.getManagerForTable(modelClass).getWritableDatabase(),
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
     * @param processModel The callback to run custom handling of the model object
     * @param models       The varg list of {@link ModelClass}
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    @SafeVarargs
    public static <ModelClass extends Model> void process(Class<ModelClass> modelClass,
                                                          final ProcessModel<ModelClass> processModel, final ModelClass... models) {
        TransactionManager.transact(FlowManager.getManagerForTable(modelClass).getWritableDatabase(),
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
