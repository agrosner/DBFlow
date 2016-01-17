package com.raizlabs.android.dbflow.structure.database;

/**
 * Description:
 */
public interface DatabaseStatement {

    long executeUpdateDelete();


    void execute();

    void close();

    long simpleQueryForLong();
}
