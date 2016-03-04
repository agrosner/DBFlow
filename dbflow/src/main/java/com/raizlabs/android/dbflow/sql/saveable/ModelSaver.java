package com.raizlabs.android.dbflow.sql.saveable;

import android.content.ContentValues;

import com.raizlabs.android.dbflow.SQLiteCompatibilityUtils;
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
public class ModelSaver<ModelClass extends Model, TableClass extends Model, AdapterClass extends RetrievalAdapter & InternalAdapter> {

    private final ModelAdapter<ModelClass> modelAdapter;
    private final AdapterClass adapter;

    public ModelSaver(ModelAdapter<ModelClass> modelAdapter, AdapterClass adapter) {
        this.modelAdapter = modelAdapter;
        this.adapter = adapter;
    }

    public void save(TableClass model) {
        DatabaseWrapper wrapper = FlowManager.getDatabaseForTable(modelAdapter.getModelClass()).getWritableDatabase();
        save(model, wrapper);
    }

    @SuppressWarnings("unchecked")
    public void save(TableClass model, DatabaseWrapper wrapper) {
        if (model == null) {
            throw new IllegalArgumentException("Model from " + modelAdapter.getModelClass() + " was null");
        }

        boolean exists = adapter.exists(model, wrapper);

        if (exists) {
            exists = update(model, wrapper);
        }

        if (!exists) {
            insert(model, wrapper);
        }

        SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.SAVE);
    }

    public boolean update(TableClass model) {
        return update(model, FlowManager.getDatabaseForTable(modelAdapter.getModelClass()).getWritableDatabase());
    }

    @SuppressWarnings("unchecked")
    public boolean update(TableClass model, DatabaseWrapper wrapper) {
        ContentValues contentValues = new ContentValues();
        adapter.bindToContentValues(contentValues, model);
        boolean successful = SQLiteCompatibilityUtils.updateWithOnConflict(wrapper,
            modelAdapter.getTableName(), contentValues, adapter.getPrimaryConditionClause(model).getQuery(), null,
            ConflictAction.getSQLiteDatabaseAlgorithmInt(modelAdapter.getUpdateOnConflictAction())) != 0;
        if (successful) {
            SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.UPDATE);
        }
        return successful;
    }

    @SuppressWarnings("unchecked")
    public long insert(TableClass model, DatabaseWrapper wrapper) {
        DatabaseStatement insertStatement = modelAdapter.getInsertStatement(wrapper);
        adapter.bindToInsertStatement(insertStatement, model);
        long id = insertStatement.executeInsert();
        if (id > -1) {
            adapter.updateAutoIncrement(model, id);
            SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.INSERT);
        }
        return id;
    }

    @SuppressWarnings("unchecked")
    public long insert(TableClass model) {
        DatabaseStatement insertStatement = modelAdapter.getInsertStatement();
        adapter.bindToInsertStatement(insertStatement, model);
        long id = insertStatement.executeInsert();
        if (id > -1) {
            adapter.updateAutoIncrement(model, id);
            SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.INSERT);
        }
        return id;
    }

    public boolean delete(TableClass model) {
        return delete(model, FlowManager.getDatabaseForTable(modelAdapter.getModelClass()).getWritableDatabase());
    }

    @SuppressWarnings("unchecked")
    public boolean delete(TableClass model, DatabaseWrapper wrapper) {
        boolean successful = SQLite.delete((Class<TableClass>) adapter.getModelClass()).where(
            adapter.getPrimaryConditionClause(model)).count(wrapper) != 0;
        if (successful) {
            SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.DELETE);
        }
        adapter.updateAutoIncrement(model, 0);
        return successful;
    }
}
