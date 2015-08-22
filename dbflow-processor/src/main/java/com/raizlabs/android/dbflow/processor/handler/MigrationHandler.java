package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.processor.definition.MigrationDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Description: Handles {@link com.raizlabs.android.dbflow.annotation.Migration} by creating {@link com.raizlabs.android.dbflow.processor.definition.MigrationDefinition}
 * and adds them to the {@link com.raizlabs.android.dbflow.processor.model.ProcessorManager}
 */
public class MigrationHandler extends BaseContainerHandler<Migration> {

    @Override
    protected Class<Migration> getAnnotationClass() {
        return Migration.class;
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        MigrationDefinition migrationDefinition = new MigrationDefinition(processorManager, (TypeElement) element);
        processorManager.addMigrationDefinition(migrationDefinition);
    }
}
