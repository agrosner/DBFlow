package com.raizlabs.android.dbflow.sqlcipher;

import com.raizlabs.android.dbflow.structure.database.BaseDatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;

import net.sqlcipher.database.SQLiteStatement;

/**
 * Description: Implements the methods necessary for {@link DatabaseStatement}. Delegates calls to
 * the contained {@link SQLiteStatement}.
 */
public class SQLCipherStatement extends BaseDatabaseStatement {

    public static SQLCipherStatement from(SQLiteStatement statement) {
        return new SQLCipherStatement(statement);
    }

    private final SQLiteStatement statement;

    SQLCipherStatement(SQLiteStatement statement) {
        this.statement = statement;
    }

    public SQLiteStatement getStatement() {
        return statement;
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
    public void bindString(int index, String s) {
        statement.bindString(index, s);
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
