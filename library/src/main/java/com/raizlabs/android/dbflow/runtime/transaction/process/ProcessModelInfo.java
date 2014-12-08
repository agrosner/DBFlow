package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ProcessModelInfo<ModelClass extends Model> {

    List<ModelClass> mModels = new ArrayList<ModelClass>();

    ResultReceiver<List<ModelClass>> mReceiver;

    DBTransactionInfo mInfo;

    Class<ModelClass> mTable;

    /**
     * Required to construct this information with Models
     */
    ProcessModelInfo() {
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <ModelClass extends Model> ProcessModelInfo<ModelClass> withModels(ModelClass... models) {
        return new ProcessModelInfo<ModelClass>()
                .models(models);
    }

    @SuppressWarnings("unchecked")
    public ProcessModelInfo<ModelClass> models(ModelClass... models) {
        mModels.addAll(Arrays.asList(models));
        if (models.length > 0) {
            mTable = (Class<ModelClass>) models[0].getClass();
        }
        return this;
    }

    public static <ModelClass extends Model> ProcessModelInfo<ModelClass> withModels(Collection<ModelClass> models) {
        return new ProcessModelInfo<ModelClass>()
                .models(models);
    }

    @SuppressWarnings("unchecked")
    public ProcessModelInfo<ModelClass> models(Collection<ModelClass> models) {
        mModels.addAll(models);
        if (models != null && models.size() > 0) {
            mTable = (Class<ModelClass>) new ArrayList<>(models).get(0).getClass();
        }
        return this;
    }

    public ProcessModelInfo<ModelClass> result(ResultReceiver<List<ModelClass>> resultReceiver) {
        mReceiver = resultReceiver;
        return this;
    }

    public ProcessModelInfo<ModelClass> fetch() {
        return info(DBTransactionInfo.create());
    }

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

    public boolean hasData() {
        return !mModels.isEmpty();
    }

    /**
     * Iterates through the models in this class and allows for a process on it.
     *
     * @param processModel Process model
     */
    public void processModels(ProcessModel<ModelClass> processModel) {
        if (processModel != null) {
            for (ModelClass model : mModels) {
                processModel.processModel(model);
            }
        }
    }
}
