package com.grosner.processor.definition;

import com.grosner.processor.model.ProcessorManager;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class BaseTableDefinition {

    protected List<ColumnDefinition> columnDefinitions;

    protected ProcessorManager manager;

    private String modelClassName;

    public BaseTableDefinition(Element typeElement) {
        this.modelClassName = typeElement.getSimpleName().toString();
        columnDefinitions = new ArrayList<>();
    }

    protected abstract void createColumnDefinitions(TypeElement typeElement);

    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public abstract List<ColumnDefinition> getPrimaryColumnDefinitions();

    public abstract String getTableSourceClassName();

    public String getModelClassName() {
        return modelClassName;
    }
}
