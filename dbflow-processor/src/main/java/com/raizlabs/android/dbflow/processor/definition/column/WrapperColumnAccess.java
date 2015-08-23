package com.raizlabs.android.dbflow.processor.definition.column;

/**
 * Description:
 */
abstract class WrapperColumnAccess extends BaseColumnAccess {

    protected BaseColumnAccess existingColumnAccess;
    protected ColumnDefinition columnDefinition;

    public WrapperColumnAccess(ColumnDefinition columnDefinition) {
        existingColumnAccess = columnDefinition.columnAccess;
        this.columnDefinition = columnDefinition;
    }
}
