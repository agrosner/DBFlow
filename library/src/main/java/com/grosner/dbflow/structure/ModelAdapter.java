package com.grosner.dbflow.structure;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.sql.language.Update;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class ModelAdapter<ModelClass extends Model> implements InternalAdapter<ModelClass> {

    private ConditionQueryBuilder<ModelClass> mPrimaryWhere;

    private ConditionQueryBuilder<ModelClass> mFullWhere;

    private SQLiteStatement mInsertStatement;

    private SQLiteStatement mUpdateStatement;

    /**
     * @return The precompiled insert statement for this table model adapter
     */
    public SQLiteStatement getInsertStatement() {
        if (mInsertStatement == null) {
            mInsertStatement = FlowManager.getDatabaseForTable(getModelClass())
                    .getWritableDatabase().compileStatement(getInsertStatementQuery());
        }

        return mInsertStatement;
    }

    public SQLiteStatement getUpdateStatement() {
        if(mUpdateStatement == null) {
            mUpdateStatement = FlowManager.getDatabaseForTable(getModelClass())
                    .getWritableDatabase().compileStatement(getUpdateStatementQuery());
        }

        return mUpdateStatement;
    }


    public abstract ModelClass loadFromCursor(Cursor cursor);

    public synchronized void save(boolean async, ModelClass model, int saveMode) {
        SqlUtils.sync(async, model, this, saveMode);
    }

    /**
     * Binds a {@link ModelClass} to the specified db statement
     *
     * @param sqLiteStatement The statement to insert
     */
    public abstract void bindToStatement(SQLiteStatement sqLiteStatement, ModelClass model);

    /**
     * If a {@link com.grosner.dbflow.structure.Model} has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    public void updateAutoIncrement(ModelClass model, long id) {

    }

    public abstract boolean exists(ModelClass model);

    public abstract void delete(boolean async, ModelClass model);

    public abstract ConditionQueryBuilder<ModelClass> getPrimaryModelWhere(ModelClass model);

    public abstract ConditionQueryBuilder<ModelClass> getFullModelWhere(ModelClass model);

    protected abstract ConditionQueryBuilder<ModelClass> createPrimaryModelWhere();

    protected abstract ConditionQueryBuilder<ModelClass> createFullModelWhere();

    public ConditionQueryBuilder<ModelClass> getPrimaryModelWhere() {
        if (mPrimaryWhere == null) {
            mPrimaryWhere = createPrimaryModelWhere();
        }
        mPrimaryWhere.setUseEmptyParams(true);
        return mPrimaryWhere;
    }

    public ConditionQueryBuilder<ModelClass> getFullModelWhere() {
        if (mFullWhere == null) {
            mFullWhere = createFullModelWhere();
        }
        mFullWhere.setUseEmptyParams(true);
        return mFullWhere;
    }

    public abstract String getCreationQuery();

    protected abstract String getInsertStatementQuery();

    protected String getUpdateStatementQuery() {
        return new Update().table(getModelClass()).set(getFullModelWhere()).getQuery();
    }

    @Override
    public abstract Class<ModelClass> getModelClass();

    @Override
    public abstract String getTableName();

    public abstract ModelClass newInstance();
}
