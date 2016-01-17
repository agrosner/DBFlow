package com.raizlabs.android.dbflow.test.customhelper;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.structure.database.FlowSQLiteOpenHelper;

/**
 * Description: A custom open helper class that you can specify.
 */
public class CustomOpenHelper extends FlowSQLiteOpenHelper {

    public CustomOpenHelper(BaseDatabaseDefinition flowManager,
                            DatabaseHelperListener listener) {
        super(flowManager, listener);
    }
}
