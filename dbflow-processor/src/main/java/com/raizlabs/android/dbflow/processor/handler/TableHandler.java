package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.annotation.MultipleManyToMany;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.processor.definition.ManyToManyDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Description: Handles {@link com.raizlabs.android.dbflow.annotation.Table} annotations, writing ModelAdapters,
 * and adding them to the {@link com.raizlabs.android.dbflow.processor.model.ProcessorManager}
 */
public class TableHandler extends BaseContainerHandler<Table> {

    @Override
    protected Class<Table> getAnnotationClass() {
        return Table.class;
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        if (element instanceof TypeElement && element.getAnnotation(getAnnotationClass()) != null) {
            TableDefinition tableDefinition = new TableDefinition(processorManager, (TypeElement) element);
            processorManager.addTableDefinition(tableDefinition);

            if (element.getAnnotation(ManyToMany.class) != null) {
                ManyToManyDefinition manyToManyDefinition = new ManyToManyDefinition((TypeElement) element, processorManager);
                processorManager.addManyToManyDefinition(manyToManyDefinition);
            }

            if (element.getAnnotation(MultipleManyToMany.class) != null) {
                MultipleManyToMany multipleManyToMany = element.getAnnotation(MultipleManyToMany.class);
                for (ManyToMany manyToMany : multipleManyToMany.value()) {
                    ManyToManyDefinition manyToManyDefinition = new ManyToManyDefinition((TypeElement) element, processorManager, manyToMany);
                    processorManager.addManyToManyDefinition(manyToManyDefinition);
                }
            }
        }
    }
}
