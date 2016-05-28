package com.raizlabs.android.dbflow.sql.saveable;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.InternalAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.RetrievalAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Defines how models get saved into the DB. It will bind values to {@link android.content.ContentValues} for
 * an update, execute a {@link DatabaseStatement}, or delete an object via the {@link Delete} wrapper.
 */
public class ModelSaver<TModel extends Model, TTable extends Model,
        TAdapter extends RetrievalAdapter & InternalAdapter> {

    private static final int INSERT_FAILED = -1;

    private final ModelAdapter<TModel> modelAdapter;
    private final TAdapter adapter;

    public ModelSaver(ModelAdapter<TModel> modelAdapter, TAdapter adapter) {
        this.modelAdapter = modelAdapter;
        this.adapter = adapter;
    }

    public synchronized void save(@NonNull TTable model) {
        save(model, getWritableDatabase(modelAdapter));
    }

    @SuppressWarnings("unchecked")
    public synchronized void save(@NonNull TTable model, DatabaseWrapper wrapper) {
        boolean exists = adapter.exists(model, wrapper);

        if (exists) {
            exists = update(model, wrapper);
        }

        if (!exists) {
            exists = insert(model, wrapper) > INSERT_FAILED;
        }

        if (exists) {
            SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.SAVE);
        }
    }

    public synchronized boolean update(@NonNull TTable model) {
        return update(model, getWritableDatabase(modelAdapter), new ContentValues());
    }

    public synchronized boolean update(@NonNull TTable model, @NonNull DatabaseWrapper wrapper) {
        return update(model, wrapper, new ContentValues());
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean update(@NonNull TTable model, @NonNull DatabaseWrapper wrapper,
                                       @NonNull ContentValues contentValues) {
        adapter.bindToContentValues(contentValues, model);
        boolean successful = wrapper.updateWithOnConflict(modelAdapter.getTableName(), contentValues,
                adapter.getPrimaryConditionClause(model).getQuery(), null,
                ConflictAction.getSQLiteDatabaseAlgorithmInt(modelAdapter.getUpdateOnConflictAction())) != 0;
        if (successful) {
            SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.UPDATE);
        }
        return successful;
    }

    @SuppressWarnings("unchecked")
    public synchronized long insert(@NonNull TTable model) {
        return insert(model, modelAdapter.getInsertStatement());
    }

    @SuppressWarnings("unchecked")
    public synchronized long insert(@NonNull TTable model, @NonNull DatabaseWrapper wrapper) {
        return insert(model, modelAdapter.getInsertStatement(wrapper));
    }

    @SuppressWarnings("unchecked")
    public synchronized long insert(@NonNull TTable model, @NonNull DatabaseStatement insertStatement) {
        adapter.bindToInsertStatement(insertStatement, model);
        long id = insertStatement.executeInsert();
        if (id > INSERT_FAILED) {
            adapter.updateAutoIncrement(model, id);
            SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.INSERT);
        }
        return id;
    }

    public synchronized boolean delete(@NonNull TTable model) {
        return delete(model, getWritableDatabase(modelAdapter));
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean delete(@NonNull TTable model, @NonNull DatabaseWrapper wrapper) {
        boolean successful = SQLite.delete(model.getClass())
                .where(adapter.getPrimaryConditionClause(model))
                .count(wrapper) != 0;
        if (successful) {
            SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.DELETE);
        }
        adapter.updateAutoIncrement(model, 0);
        return successful;
    }

    protected DatabaseWrapper getWritableDatabase(ModelAdapter<TModel> modelAdapter) {
        return FlowManager.getDatabaseForTable(modelAdapter.getModelClass()).getWritableDatabase();
    }
}
