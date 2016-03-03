package com.raizlabs.android.dbflow.test.sqlcipher;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.dbflow.android.sqlcipher.SQLCipherOpenHelper;

/**
 * Description: Simple implementation.
 */
public class SQLCipherHelperImpl extends SQLCipherOpenHelper {

    public SQLCipherHelperImpl(BaseDatabaseDefinition databaseDefinition, DatabaseHelperListener listener) {
        super(databaseDefinition, listener);
    }

    @Override
    protected String getCipherSecret() {
        return "dbflow-rules";
    }
}
