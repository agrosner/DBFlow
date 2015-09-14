package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description: Used in holding data about migration files.
 */
public class MigrationDefinition extends BaseDefinition {


    public TypeName databaseName;

    public Integer version;

    public int priority = -1;

    public MigrationDefinition(ProcessorManager processorManager, TypeElement typeElement) {
        super(typeElement, processorManager);
        setOutputClassName("");

        Migration migration = typeElement.getAnnotation(Migration.class);
        try {
            migration.database();
        } catch (MirroredTypeException mte) {
            databaseName = TypeName.get(mte.getTypeMirror());
        }
        version = migration.version();
        priority = migration.priority();
    }
}
