package com.raizlabs.android.dbflow.sql.saveable;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.NotifyDistributor;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Defines how models get saved into the DB. It will bind values to {@link DatabaseStatement}
 * for all CRUD operations as they are wildly faster and more efficient than {@link ContentValues}.
 */
public class ModelSaver<TModel> {

    private static final int INSERT_FAILED = -1;

    private ModelAdapter<TModel> modelAdapter;

    public void setModelAdapter(@NonNull ModelAdapter<TModel> modelAdapter) {
        this.modelAdapter = modelAdapter;
    }

    public synchronized boolean save(@NonNull TModel model) {
        return save(model, getWritableDatabase(), modelAdapter.getInsertStatement(),
            modelAdapter.getUpdateStatement());
    }

    public synchronized boolean save(@NonNull TModel model,
                                     @NonNull DatabaseWrapper wrapper) {
        return save(model, wrapper, modelAdapter.getInsertStatement(wrapper),
            modelAdapter.getUpdateStatement(wrapper));
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean save(@NonNull TModel model, DatabaseWrapper wrapper,
                                     @NonNull DatabaseStatement insertStatement,
                                     @NonNull DatabaseStatement updateStatement) {
        boolean exists = modelAdapter.exists(model, wrapper);

        if (exists) {
            exists = update(model, wrapper, updateStatement);
        }

        if (!exists) {
            exists = insert(model, insertStatement, wrapper) > INSERT_FAILED;
        }

        if (exists) {
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.SAVE);
        }

        // return successful store into db.
        return exists;
    }

    public synchronized boolean update(@NonNull TModel model) {
        return update(model, getWritableDatabase(), modelAdapter.getUpdateStatement());
    }

    public synchronized boolean update(@NonNull TModel model, @NonNull DatabaseWrapper wrapper) {
        DatabaseStatement insertStatement = modelAdapter.getUpdateStatement(wrapper);
        boolean success = false;
        try {
            success = update(model, wrapper, insertStatement);
        } finally {
            // since we generate an insert every time, we can safely close the statement here.
            insertStatement.close();
        }
        return success;
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean update(@NonNull TModel model, @NonNull DatabaseWrapper wrapper,
                                       @NonNull DatabaseStatement databaseStatement) {
        modelAdapter.saveForeignKeys(model, wrapper);
        modelAdapter.bindToUpdateStatement(databaseStatement, model);
        boolean successful = databaseStatement.executeUpdateDelete() != 0;
        if (successful) {
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.UPDATE);
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
    public synchronized long insert(@NonNull TModel model,
                                    @NonNull DatabaseStatement insertStatement,
                                    @NonNull DatabaseWrapper wrapper) {
        modelAdapter.saveForeignKeys(model, wrapper);
        modelAdapter.bindToInsertStatement(insertStatement, model);
        long id = insertStatement.executeInsert();
        if (id > INSERT_FAILED) {
            modelAdapter.updateAutoIncrement(model, id);
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.INSERT);
        }
        return id;
    }

    public synchronized boolean delete(@NonNull TModel model) {
        return delete(model, modelAdapter.getDeleteStatement(), getWritableDatabase());
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean delete(@NonNull TModel model, @NonNull DatabaseWrapper wrapper) {
        DatabaseStatement deleteStatement = modelAdapter.getDeleteStatement(wrapper);
        boolean success = false;
        try {
            success = delete(model, deleteStatement, wrapper);
        } finally {
            // since we generate an insert every time, we can safely close the statement here.
            deleteStatement.close();
        }
        return success;
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean delete(@NonNull TModel model,
                                       @NonNull DatabaseStatement deleteStatement,
                                       @NonNull DatabaseWrapper wrapper) {
        modelAdapter.deleteForeignKeys(model, wrapper);
        modelAdapter.bindToDeleteStatement(deleteStatement, model);

        boolean success = deleteStatement.executeUpdateDelete() != 0;
        if (success) {
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.DELETE);
        }
        modelAdapter.updateAutoIncrement(model, 0);
        return success;
    }

    protected DatabaseWrapper getWritableDatabase() {
        return FlowManager.getDatabaseForTable(modelAdapter.getModelClass()).getWritableDatabase();
    }

    @NonNull
    public ModelAdapter<TModel> getModelAdapter() {
        return modelAdapter;
    }

    /**
     * Legacy save method. Uses {@link ContentValues} vs. the faster {@link DatabaseStatement} for updates.
     *
     * @see #save(Object, DatabaseWrapper, DatabaseStatement, DatabaseStatement)
     */
    @Deprecated
    @SuppressWarnings({"unchecked", "deprecation"})
    public synchronized boolean save(@NonNull TModel model,
                                     @NonNull DatabaseWrapper wrapper,
                                     @NonNull DatabaseStatement insertStatement,
                                     @NonNull ContentValues contentValues) {
        boolean exists = modelAdapter.exists(model, wrapper);

        if (exists) {
            exists = update(model, wrapper, contentValues);
        }

        if (!exists) {
            exists = insert(model, insertStatement, wrapper) > INSERT_FAILED;
        }

        if (exists) {
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.SAVE);
        }

        // return successful store into db.
        return exists;
    }

    /**
     * @see #update(Object, DatabaseWrapper, DatabaseStatement)
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public synchronized boolean update(@NonNull TModel model,
                                       @NonNull DatabaseWrapper wrapper,
                                       @NonNull ContentValues contentValues) {
        modelAdapter.saveForeignKeys(model, wrapper);
        modelAdapter.bindToContentValues(contentValues, model);
        boolean successful = wrapper.updateWithOnConflict(modelAdapter.getTableName(), contentValues,
            modelAdapter.getPrimaryConditionClause(model).getQuery(), null,
            ConflictAction.getSQLiteDatabaseAlgorithmInt(modelAdapter.getUpdateOnConflictAction())) != 0;
        if (successful) {
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.UPDATE);
        }
        return successful;
    }
}

