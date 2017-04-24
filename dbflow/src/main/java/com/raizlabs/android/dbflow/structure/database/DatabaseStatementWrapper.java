package com.raizlabs.android.dbflow.structure.database;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.runtime.NotifyDistributor;
import com.raizlabs.android.dbflow.sql.language.BaseQueriable;

/**
 * Description: Delegates all of its calls to the contained {@link DatabaseStatement}, while
 * providing notification methods for when operations occur.
 */
public class DatabaseStatementWrapper<TModel> extends BaseDatabaseStatement {

    @NonNull
    private final DatabaseStatement databaseStatement;
    private final BaseQueriable<TModel> modelQueriable;

    public DatabaseStatementWrapper(@NonNull DatabaseStatement databaseStatement,
                                    BaseQueriable<TModel> modelQueriable) {
        this.databaseStatement = databaseStatement;
        this.modelQueriable = modelQueriable;
    }

    @Override
    public long executeUpdateDelete() {
        long affected = databaseStatement.executeUpdateDelete();
        if (affected > 0) {
            NotifyDistributor.get().notifyTableChanged(modelQueriable.getTable(),
                modelQueriable.getPrimaryAction());
        }
        return affected;
    }

    @Override
    public void execute() {
        databaseStatement.execute();
    }

    @Override
    public void close() {
        databaseStatement.close();
    }

    @Override
    public long simpleQueryForLong() {
        return databaseStatement.simpleQueryForLong();
    }

    @Override
    public String simpleQueryForString() {
        return databaseStatement.simpleQueryForString();
    }

    @Override
    public long executeInsert() {
        long affected = databaseStatement.executeInsert();
        if (affected > 0) {
            NotifyDistributor.get().notifyTableChanged(modelQueriable.getTable(),
                modelQueriable.getPrimaryAction());
        }
        return affected;
    }

    @Override
    public void bindString(int index, String s) {
        databaseStatement.bindString(index, s);
    }

    @Override
    public void bindNull(int index) {
        databaseStatement.bindNull(index);
    }

    @Override
    public void bindLong(int index, long aLong) {
        databaseStatement.bindLong(index, aLong);
    }

    @Override
    public void bindDouble(int index, double aDouble) {
        databaseStatement.bindDouble(index, aDouble);
    }

    @Override
    public void bindBlob(int index, byte[] bytes) {
        databaseStatement.bindBlob(index, bytes);
    }
}
