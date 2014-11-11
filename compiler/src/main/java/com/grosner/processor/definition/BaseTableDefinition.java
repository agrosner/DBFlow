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
public abstract class BaseTableDefinition extends BaseDefinition {

    protected List<ColumnDefinition> columnDefinitions;

    private String modelClassName;

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
