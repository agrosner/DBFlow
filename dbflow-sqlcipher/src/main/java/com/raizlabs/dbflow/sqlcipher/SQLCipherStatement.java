package com.raizlabs.dbflow.sqlcipher;

import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;

import net.sqlcipher.database.SQLiteStatement;

/**
 * Description:
 */
public class SQLCipherStatement implements DatabaseStatement {

    private final SQLiteStatement statement;

    public static SQLCipherStatement from(SQLiteStatement statement) {
        return new SQLCipherStatement(statement);
    }

    SQLCipherStatement(SQLiteStatement statement) {
        this.statement = statement;
    }

    @Override
    public long executeUpdateDelete() {
        return statement.executeUpdateDelete();
    }

    @Override
    public void execute() {
        statement.execute();
    }

    @Override
    public void close() {
        statement.close();
    }

    @Override
    public long simpleQueryForLong() {
        return statement.simpleQueryForLong();
    }

    @Override
    public String simpleQueryForString() {
        return statement.simpleQueryForString();
    }

    @Override
    public long executeInsert() {
        return statement.executeInsert();
    }

    @Override
    public void bindString(int index, String name) {
        statement.bindString(index, name);
    }

    @Override
    public void bindNull(int index) {
        statement.bindNull(index);
    }

    @Override
    public void bindLong(int index, long aLong) {
        statement.bindLong(index, aLong);
    }

    @Override
    public void bindDouble(int index, double aDouble) {
        statement.bindDouble(index, aDouble);
    }

    @Override
    public void bindBlob(int index, byte[] bytes) {
        statement.bindBlob(index, bytes);
    }
}
