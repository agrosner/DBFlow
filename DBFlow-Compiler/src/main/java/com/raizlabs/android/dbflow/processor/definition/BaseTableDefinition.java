package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.writer.DatabaseWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: Used to write Models and ModelViews
 */
public abstract class BaseTableDefinition extends BaseDefinition {

    protected List<ColumnDefinition> columnDefinitions;

    private String modelClassName;
    public DatabaseWriter databaseWriter;

    public BaseTableDefinition(Element typeElement, ProcessorManager processorManager) {
        super(typeElement, processorManager);
        this.modelClassName = typeElement.getSimpleName().toString();
        columnDefinitions = new ArrayList<>();
    }

    protected abstract void createColumnDefinitions(TypeElement typeElement);

    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public abstract List<ColumnDefinition> getPrimaryColumnDefinitions();

    public String getModelClassName() {
        return modelClassName;
    }

    public abstract String getTableSourceClassName();
}
