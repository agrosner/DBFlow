package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.validator.TableValidator;
import com.raizlabs.android.dbflow.processor.validator.Validator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Description: Handles {@link com.raizlabs.android.dbflow.annotation.Table} annotations, writing ModelAdapters,
 * and adding them to the {@link com.raizlabs.android.dbflow.processor.model.ProcessorManager}
 */
public class TableHandler extends BaseContainerHandler<Table> {

    private Validator<TableDefinition> definitionValidator;

    public TableHandler() {
        definitionValidator = new TableValidator();
    }

    @Override
    protected Class<Table> getAnnotationClass() {
        return Table.class;
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        if (element instanceof TypeElement) {
            TableDefinition tableDefinition = new TableDefinition(processorManager, (TypeElement) element);
            if (definitionValidator.validate(processorManager, tableDefinition)) {
                processorManager.addTableDefinition(tableDefinition);
            }
        }
    }
}
