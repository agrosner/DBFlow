package com.raizlabs.android.dbflow.test.customhelper;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperDelegate;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;

/**
 * Description: A custom open helper class that you can specify.
 */
public class CustomOpenHelper implements OpenHelper {

    public CustomOpenHelper(DatabaseDefinition flowManager,
                            DatabaseHelperListener listener) {

    }

    @Override
    public DatabaseWrapper getDatabase() {
        return null;
    }

    @Override
    public DatabaseHelperDelegate getDelegate() {
        return null;
    }

    @Override
    public boolean isDatabaseIntegrityOk() {
        return false;
    }

    @Override
    public void backupDB() {

    }

    @Override
    public void setDatabaseListener(DatabaseHelperListener helperListener) {

    }
}
