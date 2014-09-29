package com.grosner.dbflow.runtime.transaction.process;

import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Required to construct this information with Models
     */
    ProcessModelInfo(){
    }

    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ProcessModelInfo<ModelClass> withModels(ModelClass...models) {
        return new ProcessModelInfo<ModelClass>()
                .models(models);
    }

    public static <ModelClass extends Model> ProcessModelInfo<ModelClass> withModels(List<ModelClass> models) {
        return new ProcessModelInfo<ModelClass>()
                .models(models);
    }

    public ProcessModelInfo<ModelClass> models(ModelClass...models) {
        mModels.addAll(Arrays.asList(models));
        return this;
    }

    public ProcessModelInfo<ModelClass> models(List<ModelClass> models) {
        mModels.addAll(models);
        return this;
    }

    public ProcessModelInfo<ModelClass> result(ResultReceiver<List<ModelClass>> resultReceiver) {
        mReceiver = resultReceiver;
        return this;
    }

    public ProcessModelInfo<ModelClass> fetch(){
        return info(DBTransactionInfo.create());
    }

    public ProcessModelInfo<ModelClass> info(DBTransactionInfo dbTransactionInfo) {
        mInfo = dbTransactionInfo;
        return this;
    }

    public DBTransactionInfo getInfo() {
        if(mInfo == null) {
            mInfo = DBTransactionInfo.create();
        }
        return mInfo;
    }

    public boolean hasData() {
        return !mModels.isEmpty();
    }
}
