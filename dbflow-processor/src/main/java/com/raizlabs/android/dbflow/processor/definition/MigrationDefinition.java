package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import javax.lang.model.element.TypeElement;

/**
 * Description: Used in holding data about migration files.
 */
public class MigrationDefinition extends BaseDefinition {


    public String databaseName;

    public Integer version;

    public int priority = -1;

    public MigrationDefinition(ProcessorManager processorManager, TypeElement typeElement) {
        super(typeElement, processorManager);
        setOutputClassName("");

        Migration migration = typeElement.getAnnotation(Migration.class);

        this.databaseName = migration.databaseName();
        version = migration.version();
        priority = migration.priority();
    }
}
