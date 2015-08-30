package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Description: Used to write Models and ModelViews
 */
public abstract class BaseTableDefinition extends BaseDefinition {

    protected List<ColumnDefinition> columnDefinitions;

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

    public boolean hasAutoIncrement() {
        return false;
    }

    public String getModelClassName() {
        return modelClassName;
    }

}
