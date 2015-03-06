package com.raizlabs.android.dbflow.processor.writer;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.definition.BaseDefinition;
import com.raizlabs.android.dbflow.processor.definition.MigrationDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.handler.DatabaseHandler;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Description: Writes {@link com.raizlabs.android.dbflow.annotation.Database} definitions,
 * which contain {@link com.raizlabs.android.dbflow.annotation.Table},
 * {@link com.raizlabs.android.dbflow.annotation.ModelView}, and {@link com.raizlabs.android.dbflow.annotation.Migration}
 */
public class DatabaseWriter extends BaseDefinition implements FlowWriter {

    public String databaseName;

    public int databaseVersion;

    boolean foreignKeysSupported;

    boolean consistencyChecksEnabled;

    boolean backupEnabled;

    public ConflictAction insertConflict;

    public ConflictAction updateConflict;

    public String classSeparator;

    public DatabaseWriter(ProcessorManager manager, Element element) {
        super(element, manager);
        packageName = Classes.FLOW_MANAGER_PACKAGE;

        Database database = element.getAnnotation(Database.class);
        databaseName = database.name();
        if (databaseName == null || databaseName.isEmpty()) {
            databaseName = element.getSimpleName().toString();
        }

        consistencyChecksEnabled = database.consistencyCheckEnabled();
        backupEnabled = database.backupEnabled();

        classSeparator = database.generatedClassSeparator();

        definitionClassName = databaseName + classSeparator + "Database";

        databaseVersion = database.version();
        foreignKeysSupported = database.foreignKeysSupported();

        insertConflict = database.insertConflict();
        updateConflict = database.updateConflict();

    }

    @Override
    protected String getExtendsClass() {
        return Classes.BASE_DATABASE_DEFINITION;
    }

    @Override
    protected String[] getImports() {
        return new String[]{
                Classes.LIST,
                Classes.ARRAY_LIST
        };
    }

    @Override
    public void onWriteDefinition(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();

        writeConstructor(javaWriter);
        writeGetters(javaWriter);
    }

    private void writeConstructor(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();

        javaWriter.beginConstructor(Sets.newHashSet(Modifier.PUBLIC), "DatabaseHolder", "holder");
        // Register this manager with classes if multitable is enabled.
        // Need to figure out how to

        javaWriter.emitSingleLineComment("Writing for: " + databaseName);

        for (TableDefinition tableDefinition : manager.getTableDefinitions(databaseName)) {
            javaWriter.emitStatement("holder.putDatabaseForTable(%1s, this)", ModelUtils.getFieldClass(tableDefinition.getQualifiedModelClassName()));
        }

        for (ModelViewDefinition modelViewDefinition : manager.getModelViewDefinitions(databaseName)) {
            javaWriter.emitStatement("holder.putDatabaseForTable(%1s, this)", ModelUtils.getFieldClass(modelViewDefinition.getFullyQualifiedModelClassName()));
        }

        javaWriter.emitEmptyLine();
        javaWriter.emitSingleLineComment("Begin Migrations");
        Map<Integer, List<MigrationDefinition>> migrationDefinitionMap = manager.getMigrationsForDatabase(databaseName);
        if (migrationDefinitionMap != null && !migrationDefinitionMap.isEmpty()) {
            List<Integer> versionSet = new ArrayList<>(migrationDefinitionMap.keySet());
            Collections.sort(versionSet);
            for (Integer version : versionSet) {
                List<MigrationDefinition> migrationDefinitions = migrationDefinitionMap.get(version);
                javaWriter.emitStatement("List<%1s> migrations%1s = new ArrayList<>()", Classes.MIGRATION, version);
                javaWriter.emitStatement("%1s.put(%1s,%1s%1s)", DatabaseHandler.MIGRATION_FIELD_NAME, version, "migrations", version);
                for (MigrationDefinition migrationDefinition : migrationDefinitions) {
                    javaWriter.emitStatement("%1s%1s.add(new %1s())", "migrations", version, migrationDefinition.getSourceFileName());
                }
            }
        }
        javaWriter.emitSingleLineComment("End Migrations");
        javaWriter.emitEmptyLine();

        for (TableDefinition tableDefinition : manager.getTableDefinitions(databaseName)) {
            javaWriter.emitStatement(DatabaseHandler.MODEL_FIELD_NAME + ".add(%1s)", ModelUtils.getFieldClass(tableDefinition.getQualifiedModelClassName()));
            javaWriter.emitStatement(DatabaseHandler.MODEL_NAME_MAP + ".put(\"%1s\", %1s)", tableDefinition.tableName, ModelUtils.getFieldClass(tableDefinition.getQualifiedModelClassName()));
            javaWriter.emitStatement(DatabaseHandler.MODEL_ADAPTER_MAP_FIELD_NAME + ".put(%1s, new %1s())", ModelUtils.getFieldClass(tableDefinition.getQualifiedModelClassName()),
                    tableDefinition.getQualifiedAdapterClassName());
        }

        for (ModelContainerDefinition modelContainerDefinition : manager.getModelContainers(databaseName)) {
            javaWriter.emitStatement(DatabaseHandler.MODEL_CONTAINER_ADAPTER_MAP_FIELD_NAME + ".put(%1s, new %1s())", ModelUtils.getFieldClass(modelContainerDefinition.getModelClassQualifiedName()),
                    modelContainerDefinition.getSourceFileName());
        }

        for (ModelViewDefinition modelViewDefinition : manager.getModelViewDefinitions(databaseName)) {
            javaWriter.emitStatement(DatabaseHandler.MODEL_VIEW_FIELD_NAME + ".add(%1s)", ModelUtils.getFieldClass(modelViewDefinition.getFullyQualifiedModelClassName()));
            javaWriter.emitStatement(DatabaseHandler.MODEL_VIEW_ADAPTER_MAP_FIELD_NAME + ".put(%1s, new %1s())",
                    ModelUtils.getFieldClass(modelViewDefinition.getFullyQualifiedModelClassName()), modelViewDefinition.getSourceFileName());
        }

        javaWriter.endConstructor();
    }

    private void writeGetters(JavaWriter javaWriter) throws IOException {

        // Get Model Container
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s", foreignKeysSupported);
            }
        }, "boolean", "isForeignKeysSupported", DatabaseHandler.METHOD_MODIFIERS);

        // Get Model Container
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s", backupEnabled);
            }
        }, "boolean", "backupEnabled", DatabaseHandler.METHOD_MODIFIERS);

        // Get Model Container
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s", consistencyChecksEnabled);
            }
        }, "boolean", "areConsistencyChecksEnabled", DatabaseHandler.METHOD_MODIFIERS);

        // Get Model Container
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s", databaseVersion);
            }
        }, "int", "getDatabaseVersion", DatabaseHandler.METHOD_MODIFIERS);

        // Get Model Container
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return \"%1s\"", databaseName);
            }
        }, "String", "getDatabaseName", DatabaseHandler.METHOD_MODIFIERS);

    }


}
