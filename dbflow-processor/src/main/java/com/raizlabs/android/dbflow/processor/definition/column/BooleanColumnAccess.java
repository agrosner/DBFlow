package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Description: Slightly different access than a regular column and has a type converter.
 */
public class BooleanColumnAccess extends TypeConverterAccess {

    public BooleanColumnAccess(ProcessorManager manager, ColumnDefinition columnDefinition) {
        super(manager, columnDefinition);
    }
}
