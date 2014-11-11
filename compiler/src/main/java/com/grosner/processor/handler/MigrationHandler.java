package com.grosner.processor.handler;

import com.grosner.dbflow.annotation.Migration;
import com.grosner.processor.definition.MigrationDefinition;
import com.grosner.processor.model.ProcessorManager;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
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
