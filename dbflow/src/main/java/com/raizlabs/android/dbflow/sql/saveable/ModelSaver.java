package com.raizlabs.android.dbflow.sql.saveable;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Defines how models get saved into the DB. It will bind values to {@link android.content.ContentValues} for
 * an update, execute a {@link DatabaseStatement}, or delete an object via the {@link Delete} wrapper.
 */
public class ModelSaver<TModel> {

    private static final int INSERT_FAILED = -1;

    private ModelAdapter<TModel> modelAdapter;

    public void setModelAdapter(ModelAdapter<TModel> modelAdapter) {
        this.modelAdapter = modelAdapter;
    }

    public synchronized boolean save(@NonNull TModel model) {
        return save(model, getWritableDatabase(), modelAdapter.getInsertStatement(), new ContentValues());
    }

    public synchronized boolean save(@NonNull TModel model, DatabaseWrapper wrapper) {
        return save(model, wrapper, modelAdapter.getInsertStatement(wrapper), new ContentValues());
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean save(@NonNull TModel model, DatabaseWrapper wrapper,
                                     DatabaseStatement insertStatement, ContentValues contentValues) {
        boolean exists = modelAdapter.exists(model, wrapper);

        if (exists) {
            exists = update(model, wrapper, contentValues);
        }

        if (!exists) {
            exists = insert(model, insertStatement, wrapper) > INSERT_FAILED;
        }

        if (exists) {
            SqlUtils.notifyModelChanged(model, modelAdapter, BaseModel.Action.SAVE);
        }

        // return successful store into db.
        return exists;
    }

    public synchronized boolean update(@NonNull TModel model) {
        return update(model, getWritableDatabase(), new ContentValues());
    }

    public synchronized boolean update(@NonNull TModel model, @NonNull DatabaseWrapper wrapper) {
        return update(model, wrapper, new ContentValues());
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean update(@NonNull TModel model, @NonNull DatabaseWrapper wrapper,
                                       @NonNull ContentValues contentValues) {
        modelAdapter.saveForeignKeys(model, wrapper);
        modelAdapter.bindToContentValues(contentValues, model);
        boolean successful = wrapper.updateWithOnConflict(modelAdapter.getTableName(), contentValues,
                modelAdapter.getPrimaryConditionClause(model).getQuery(), null,
                ConflictAction.getSQLiteDatabaseAlgorithmInt(modelAdapter.getUpdateOnConflictAction())) != 0;
        if (successful) {
            SqlUtils.notifyModelChanged(model, modelAdapter, BaseModel.Action.UPDATE);
        }
        return successful;
    }

    @SuppressWarnings("unchecked")
    public synchronized long insert(@NonNull TModel model) {
        return insert(model, modelAdapter.getInsertStatement(), getWritableDatabase());
    }

    @SuppressWarnings("unchecked")
    public synchronized long insert(@NonNull TModel model, @NonNull DatabaseWrapper wrapper) {
        DatabaseStatement insertStatement = modelAdapter.getInsertStatement(wrapper);
        long result = 0;
        try {
            result = insert(model, insertStatement, wrapper);
        } finally {
            // since we generate an insert every time, we can safely close the statement here.
            insertStatement.close();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public synchronized long insert(@NonNull TModel model, @NonNull DatabaseStatement insertStatement,
                                    DatabaseWrapper wrapper) {
        modelAdapter.saveForeignKeys(model, wrapper);
        modelAdapter.bindToInsertStatement(insertStatement, model);
        long id = insertStatement.executeInsert();
        if (id > INSERT_FAILED) {
            modelAdapter.updateAutoIncrement(model, id);
            SqlUtils.notifyModelChanged(model, modelAdapter, BaseModel.Action.INSERT);
        }
        return id;
    }

    public synchronized boolean delete(@NonNull TModel model) {
        return delete(model, getWritableDatabase());
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean delete(@NonNull TModel model, @NonNull DatabaseWrapper wrapper) {
        boolean successful = SQLite.delete(modelAdapter.getModelClass())
                .where(modelAdapter.getPrimaryConditionClause(model))
                .count(wrapper) != 0;
        if (successful) {
            SqlUtils.notifyModelChanged(model, modelAdapter, BaseModel.Action.DELETE);
        }
        modelAdapter.updateAutoIncrement(model, 0);
        return successful;
    }

    protected DatabaseWrapper getWritableDatabase() {
        return FlowManager.getDatabaseForTable(modelAdapter.getModelClass()).getWritableDatabase();
    }

    public ModelAdapter<TModel> getModelAdapter() {
        return modelAdapter;
    }
}

