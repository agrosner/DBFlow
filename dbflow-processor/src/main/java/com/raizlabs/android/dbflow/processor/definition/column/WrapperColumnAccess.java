package com.raizlabs.android.dbflow.processor.definition.column;

/**
 * Description:
 */
public abstract class WrapperColumnAccess extends BaseColumnAccess {

    protected ColumnDefinition columnDefinition;
    protected BaseColumnAccess existingColumnAccess;

    public WrapperColumnAccess(ColumnDefinition columnDefinition) {
        this.existingColumnAccess = columnDefinition.getColumnAccess();
        this.columnDefinition = columnDefinition;
    }

    public BaseColumnAccess getExistingColumnAccess() {
        return existingColumnAccess;
    }

}
