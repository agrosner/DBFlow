package com.raizlabs.android.dbflow.structure;

/**
 * Description:
 */
public class BaseQueryModel extends BaseFinalModel {

    @Override
    public boolean exists() {
        throw new InvalidSqlViewOperationException("View " + getClass().getName() + " does not exist");
    }
}
