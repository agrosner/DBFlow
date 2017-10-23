package com.raizlabs.android.dbflow.sql.saveable;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.runtime.NotifyDistributor;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Used to properly handle autoincrementing fields.
 */
public class AutoIncrementModelSaver<TModel> extends ModelSaver<TModel> {

    @Override
    public synchronized long insert(@NonNull TModel model) {
        return insert(model, getWritableDatabase());
    }

    @Override
    public synchronized long insert(@NonNull TModel model, @NonNull DatabaseWrapper wrapper) {
        final boolean hasAutoIncrement = getModelAdapter().hasAutoIncrement(model);
        DatabaseStatement insertStatement = hasAutoIncrement
                ? getModelAdapter().getCompiledStatement(wrapper)
                : getModelAdapter().getInsertStatement(wrapper);
        long id;
        try {
            getModelAdapter().saveForeignKeys(model, wrapper);
            if (hasAutoIncrement) {
                getModelAdapter().bindToStatement(insertStatement, model);
            } else {
                getModelAdapter().bindToInsertStatement(insertStatement, model);
            }
            id = insertStatement.executeInsert();
            if (id > INSERT_FAILED) {
                getModelAdapter().updateAutoIncrement(model, id);
                NotifyDistributor.Companion.get().notifyModelChanged(model, getModelAdapter(), BaseModel.Action.INSERT);
            }
        } finally {
            // since we generate an insert every time, we can safely close the statement here.
            insertStatement.close();
        }
        return id;
    }

    @Override
    public synchronized long insert(@NonNull TModel model,
                                    @NonNull DatabaseStatement insertStatement,
                                    @NonNull DatabaseWrapper wrapper) {
        if (!getModelAdapter().hasAutoIncrement(model)) {
            return super.insert(model, insertStatement, wrapper);
        } else {
            FlowLog.log(FlowLog.Level.W, "Ignoring insert statement " + insertStatement + " since an autoincrement column specified in the insert.");
            return insert(model, wrapper);
        }
    }
}
