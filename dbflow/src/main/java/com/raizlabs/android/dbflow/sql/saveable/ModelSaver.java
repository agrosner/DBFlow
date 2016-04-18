package com.raizlabs.android.dbflow.sql.saveable;

import android.content.ContentValues;

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
public class ModelSaver {

    public <TModel extends Model, TTable extends Model, TAdapter extends RetrievalAdapter & InternalAdapter>
    void save(ModelAdapter<TModel> modelAdapter, TAdapter adapter, TTable model) {
        DatabaseWrapper wrapper = FlowManager.getDatabaseForTable(modelAdapter.getModelClass()).getWritableDatabase();
        save(modelAdapter, adapter, model, wrapper);
    }

    @SuppressWarnings("unchecked")
    public <TModel extends Model, TTable extends Model, TAdapter extends RetrievalAdapter & InternalAdapter>
    void save(ModelAdapter<TModel> modelAdapter, TAdapter adapter,
              TTable model, DatabaseWrapper wrapper) {
        if (model == null) {
            throw new IllegalArgumentException("Model from " + modelAdapter.getModelClass() + " was null");
        }

        boolean exists = adapter.exists(model, wrapper);

        if (exists) {
            exists = update(modelAdapter, adapter, model, wrapper);
        }

        if (!exists) {
            insert(modelAdapter, adapter, model, wrapper);
        }

        SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.SAVE);
    }

    public <TModel extends Model, TTable extends Model, TAdapter extends RetrievalAdapter & InternalAdapter>
    boolean update(ModelAdapter<TModel> modelAdapter, TAdapter adapter, TTable model) {
        return update(modelAdapter, adapter, model,
                FlowManager.getDatabaseForTable(modelAdapter.getModelClass()).getWritableDatabase());
    }

    @SuppressWarnings("unchecked")
    public <TModel extends Model, TTable extends Model, TAdapter extends RetrievalAdapter & InternalAdapter>
    boolean update(ModelAdapter<TModel> modelAdapter, TAdapter adapter,
                   TTable model, DatabaseWrapper wrapper) {
        ContentValues contentValues = new ContentValues();
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
    public <TModel extends Model, TTable extends Model, TAdapter extends RetrievalAdapter & InternalAdapter>
    long insert(ModelAdapter<TModel> modelAdapter, TAdapter adapter,
                TTable model, DatabaseWrapper wrapper) {
        DatabaseStatement insertStatement = modelAdapter.getInsertStatement(wrapper);
        adapter.bindToInsertStatement(insertStatement, model);
        long id = insertStatement.executeInsert();
        insertStatement.close();
        if (id > -1) {
            adapter.updateAutoIncrement(model, id);
            SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.INSERT);
        }
        return id;
    }

    @SuppressWarnings("unchecked")
    public <TModel extends Model, TTable extends Model, TAdapter extends RetrievalAdapter & InternalAdapter>
    long insert(ModelAdapter<TModel> modelAdapter, TAdapter adapter, TTable model) {
        DatabaseStatement insertStatement = modelAdapter.getInsertStatement();
        adapter.bindToInsertStatement(insertStatement, model);
        long id = insertStatement.executeInsert();
        insertStatement.close();
        if (id > -1) {
            adapter.updateAutoIncrement(model, id);
            SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.INSERT);
        }
        return id;
    }

    public <TModel extends Model, TTable extends Model, TAdapter extends RetrievalAdapter & InternalAdapter>
    boolean delete(ModelAdapter<TModel> modelAdapter, TAdapter adapter, TTable model) {
        return delete(modelAdapter, adapter, model,
                FlowManager.getDatabaseForTable(modelAdapter.getModelClass()).getWritableDatabase());
    }

    @SuppressWarnings("unchecked")
    public <TModel extends Model, TTable extends Model, TAdapter extends RetrievalAdapter & InternalAdapter>
    boolean delete(ModelAdapter<TModel> modelAdapter, TAdapter adapter, TTable model, DatabaseWrapper wrapper) {
        boolean successful = SQLite.delete((Class<TTable>) adapter.getModelClass()).where(
                adapter.getPrimaryConditionClause(model)).count(wrapper) != 0;
        if (successful) {
            SqlUtils.notifyModelChanged(model, adapter, modelAdapter, BaseModel.Action.DELETE);
        }
        adapter.updateAutoIncrement(model, 0);
        return successful;
    }
}
