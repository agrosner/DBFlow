package com.raizlabs.android.dbflow.processor.definition.column;

/**
 * Description:
 */
abstract class WrapperColumnAccess extends BaseColumnAccess {

    protected ColumnDefinition columnDefinition;
    protected BaseColumnAccess existingColumnAccess;

    public WrapperColumnAccess(ColumnDefinition columnDefinition) {
        this.existingColumnAccess = columnDefinition.columnAccess;
        this.columnDefinition = columnDefinition;
    }

    protected BaseColumnAccess getExistingColumnAccess() {
        return existingColumnAccess;
    }

}
