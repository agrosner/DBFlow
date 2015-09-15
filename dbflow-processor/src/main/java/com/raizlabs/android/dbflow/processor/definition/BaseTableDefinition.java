package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Description: Used to write Models and ModelViews
 */
public abstract class BaseTableDefinition extends BaseDefinition {

    protected List<ColumnDefinition> columnDefinitions;
    protected Map<ClassName, List<ColumnDefinition>> associatedTypeConverters = new HashMap<>();

    private String modelClassName;
    public DatabaseDefinition databaseDefinition;

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

    public abstract ClassName getPropertyClassName();

    public TypeName getParameterClassName(boolean isModelContainerAdapter) {
        return isModelContainerAdapter ? ModelUtils.getModelContainerType(manager, elementClassName)
                : elementClassName;
    }

    public String addColumnForTypeConverter(ColumnDefinition columnDefinition, ClassName typeConverterName) {
        List<ColumnDefinition> columnDefinitions = associatedTypeConverters.get(typeConverterName);
        if (columnDefinitions == null) {
            columnDefinitions = new ArrayList<>();
        }
        columnDefinitions.add(columnDefinition);

        return "typeConverter" + typeConverterName.simpleName();
    }

    public Map<ClassName, List<ColumnDefinition>> getAssociatedTypeConverters() {
        return associatedTypeConverters;
    }

    public boolean hasAutoIncrement() {
        return false;
    }

    public String getModelClassName() {
        return modelClassName;
    }

}
