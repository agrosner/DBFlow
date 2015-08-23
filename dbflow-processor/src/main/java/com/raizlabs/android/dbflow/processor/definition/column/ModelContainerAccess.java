package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

/**
 * Description:
 */
public class ModelContainerAccess extends BaseColumnAccess {


    private final ColumnDefinition columnDefinition;

    private final BaseColumnAccess existingColumnAccess;

    private final ProcessorManager manager;

    public ModelContainerAccess(ProcessorManager manager, ColumnDefinition columnDefinition) {

        this.columnDefinition = columnDefinition;
        this.existingColumnAccess = columnDefinition.columnAccess;
        this.manager = manager;
    }

    @Override
    String getColumnAccessString(String variableNameString, String elementName) {
        return null;
    }

    @Override
    String getShortAccessString(String elementName) {
        return null;
    }

    @Override
    String setColumnAccessString(String variableNameString, String elementName, String formattedAccess) {
        return null;
    }
}
