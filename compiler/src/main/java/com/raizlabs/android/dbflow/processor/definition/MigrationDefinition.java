package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.processor.DBFlowProcessor;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.TypeElement;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class MigrationDefinition extends BaseDefinition implements FlowWriter {


    public static final String DBFLOW_MIGRATION_CONTAINER_TAG = "$Migrations";

    public String databaseName;

    public Integer version;

    public MigrationDefinition(ProcessorManager processorManager, TypeElement typeElement) {
        super(typeElement, processorManager);
        setDefinitionClassName("");

        Migration migration = typeElement.getAnnotation(Migration.class);

        this.databaseName = migration.databaseName();
        if(databaseName == null || databaseName.isEmpty()) {
            databaseName = DBFlowProcessor.DEFAULT_DB_NAME;
        }

        version = migration.version();
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {

    }
}
