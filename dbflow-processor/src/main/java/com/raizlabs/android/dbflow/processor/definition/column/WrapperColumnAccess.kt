package com.raizlabs.android.dbflow.processor.definition.column

/**
 * Description:
 */
abstract class WrapperColumnAccess(protected var columnDefinition: ColumnDefinition)
: BaseColumnAccess() {
    var existingColumnAccess: BaseColumnAccess
        protected set

    init {
        this.existingColumnAccess = columnDefinition.columnAccess
    }

}
