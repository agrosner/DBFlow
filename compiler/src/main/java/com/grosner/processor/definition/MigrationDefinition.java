package com.grosner.processor.definition;

import com.grosner.dbflow.annotation.Migration;
import com.grosner.processor.DBFlowProcessor;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.TypeElement;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class MigrationDefinition implements FlowWriter {


    public static final String DBFLOW_MIGRATION_CONTAINER_TAG = "$Migrations";

    public final TypeElement classElement;

    private final ProcessorManager manager;

    private String sourceFileName;

    private String packageName;

    public String databaseName;

    public Integer version;

    public MigrationDefinition(String packageName, ProcessorManager processorManager, TypeElement typeElement) {
        this.classElement = typeElement;
        this.manager = processorManager;
        this.sourceFileName = classElement.getSimpleName().toString() + DBFLOW_MIGRATION_CONTAINER_TAG;
        this.packageName = packageName;

        Migration migration = typeElement.getAnnotation(Migration.class);

        this.databaseName = migration.databaseName();
        if(databaseName == null || databaseName.isEmpty()) {
            databaseName = DBFlowProcessor.DEFAULT_DB_NAME;
        }

        version = migration.version();
    }

    public String getMigrationClassName() {
        return packageName + "." + classElement.getSimpleName().toString();
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {

    }
}
