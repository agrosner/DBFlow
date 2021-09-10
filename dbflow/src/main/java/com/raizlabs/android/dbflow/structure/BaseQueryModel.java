package com.raizlabs.android.dbflow.structure;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Provides a base class for objects that represent {@link QueryModel}.
 */
public class BaseQueryModel extends NoModificationModel {

    @Override
    public boolean exists() {
        throw new InvalidSqlViewOperationException("Query " + getClass().getName() +
                " does not exist as a table." + "It's a convenient representation of a complex SQLite query.");
    }

    @Override
    public boolean exists(@NonNull DatabaseWrapper databaseWrapper) {
        return exists();
    }

}
