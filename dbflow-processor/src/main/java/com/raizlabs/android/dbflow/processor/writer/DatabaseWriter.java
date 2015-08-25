package com.raizlabs.android.dbflow.processor.writer;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.BaseDefinition;
import com.raizlabs.android.dbflow.processor.definition.MigrationDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
import com.raizlabs.android.dbflow.processor.definition.QueryModelDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeDefinition;
import com.raizlabs.android.dbflow.processor.handler.DatabaseHandler;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Description: Writes {@link com.raizlabs.android.dbflow.annotation.Database} definitions,
 * which contain {@link com.raizlabs.android.dbflow.annotation.Table},
 * {@link com.raizlabs.android.dbflow.annotation.ModelView}, and {@link com.raizlabs.android.dbflow.annotation.Migration}
 */
public class DatabaseWriter extends BaseDefinition implements TypeDefinition {

    public String databaseName;

    public int databaseVersion;

    boolean foreignKeysSupported;

    boolean consistencyChecksEnabled;

    boolean backupEnabled;

    public ConflictAction insertConflict;

    public ConflictAction updateConflict;

    public String classSeparator;

    String sqliteOpenHelperClass;

    public DatabaseWriter(ProcessorManager manager, Element element) {
        super(element, manager);
        packageName = ClassNames.FLOW_MANAGER_PACKAGE;

        Database database = element.getAnnotation(Database.class);
        databaseName = database.name();
        if (databaseName == null || databaseName.isEmpty()) {
            databaseName = element.getSimpleName().toString();
        }
        if (!isValidDatabaseName(databaseName)) {
            throw new Error("Database name [ " + databaseName + " ] is not valid. It must pass [A-Za-z_$]+[a-zA-Z0-9_$]* " +
                    "regex so it can't start with a number or contain any special character except '$'. Especially a dot character is not allowed!");
        }

        sqliteOpenHelperClass = ProcessorUtils.getOpenHelperClass(database);

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
        return ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME;
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {
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
            javaWriter.emitStatement("holder.putDatabaseForTable(%1s, this)",
                    ModelUtils.getFieldClass(tableDefinition.getQualifiedModelClassName()));
        }

        for (ModelViewDefinition modelViewDefinition : manager.getModelViewDefinitions(databaseName)) {
            javaWriter.emitStatement("holder.putDatabaseForTable(%1s, this)",
                    ModelUtils.getFieldClass(modelViewDefinition.getFullyQualifiedModelClassName()));
        }

        for (QueryModelDefinition queryModelDefinition : manager.getQueryModelDefinitions(databaseName)) {
            javaWriter.emitStatement("holder.putDatabaseForTable(%1s, this)",
                    ModelUtils.getFieldClass(queryModelDefinition.getQualifiedModelClassName()));
        }

        javaWriter.emitEmptyLine();
        javaWriter.emitSingleLineComment("Begin Migrations");
        Map<Integer, List<MigrationDefinition>> migrationDefinitionMap = manager.getMigrationsForDatabase(databaseName);
        if (migrationDefinitionMap != null && !migrationDefinitionMap.isEmpty()) {
            List<Integer> versionSet = new ArrayList<>(migrationDefinitionMap.keySet());
            Collections.sort(versionSet);
            for (Integer version : versionSet) {
                List<MigrationDefinition> migrationDefinitions = migrationDefinitionMap.get(version);
                Collections.sort(migrationDefinitions, new Comparator<MigrationDefinition>() {
                    @Override
                    public int compare(MigrationDefinition o1, MigrationDefinition o2) {
                        return Integer.valueOf(o2.priority).compareTo(o1.priority);
                    }
                });
                javaWriter.emitStatement("List<%1s> migrations%1s = new ArrayList<>()", ClassNames.MIGRATION, version);
                javaWriter.emitStatement("%1s.put(%1s,%1s%1s)", DatabaseHandler.MIGRATION_FIELD_NAME, version,
                        "migrations", version);
                for (MigrationDefinition migrationDefinition : migrationDefinitions) {
                    javaWriter.emitStatement("%1s%1s.add(new %1s())", "migrations", version,
                            migrationDefinition.getSourceFileName());
                }
            }
        }
        javaWriter.emitSingleLineComment("End Migrations");
        javaWriter.emitEmptyLine();

        for (TableDefinition tableDefinition : manager.getTableDefinitions(databaseName)) {
            javaWriter.emitStatement(DatabaseHandler.MODEL_FIELD_NAME + ".add(%1s)",
                    ModelUtils.getFieldClass(tableDefinition.getQualifiedModelClassName()));
            javaWriter.emitStatement(DatabaseHandler.MODEL_NAME_MAP + ".put(\"%1s\", %1s)", tableDefinition.tableName,
                    ModelUtils.getFieldClass(tableDefinition.getQualifiedModelClassName()));
            javaWriter.emitStatement(DatabaseHandler.MODEL_ADAPTER_MAP_FIELD_NAME + ".put(%1s, new %1s())",
                    ModelUtils.getFieldClass(tableDefinition.getQualifiedModelClassName()),
                    tableDefinition.getQualifiedAdapterClassName());
        }

        for (ModelContainerDefinition modelContainerDefinition : manager.getModelContainers(databaseName)) {
            javaWriter.emitStatement(DatabaseHandler.MODEL_CONTAINER_ADAPTER_MAP_FIELD_NAME + ".put(%1s, new %1s())",
                    ModelUtils.getFieldClass(modelContainerDefinition.getModelClassQualifiedName()),
                    modelContainerDefinition.getSourceFileName());
        }

        for (ModelViewDefinition modelViewDefinition : manager.getModelViewDefinitions(databaseName)) {
            javaWriter.emitStatement(DatabaseHandler.MODEL_VIEW_FIELD_NAME + ".add(%1s)",
                    ModelUtils.getFieldClass(modelViewDefinition.getFullyQualifiedModelClassName()));
            javaWriter.emitStatement(DatabaseHandler.MODEL_VIEW_ADAPTER_MAP_FIELD_NAME + ".put(%1s, new %1s())",
                    ModelUtils.getFieldClass(modelViewDefinition.getFullyQualifiedModelClassName()),
                    modelViewDefinition.getSourceFileName());
        }

        javaWriter.emitSingleLineComment("Writing Query Models");
        for (QueryModelDefinition queryModelDefinition : manager.getQueryModelDefinitions(databaseName)) {
            javaWriter.emitStatement(DatabaseHandler.QUERY_MODEL_ADAPTER_MAP_FIELD_NAME + ".put(%1s, new %1s())",
                    ModelUtils.getFieldClass(queryModelDefinition.getQualifiedModelClassName()),
                    queryModelDefinition.getQualifiedAdapterName());
        }

        javaWriter.endConstructor();
    }

    private void writeGetters(JavaWriter javaWriter) throws IOException {

        // create helper
        if (!Void.class.getCanonicalName().equals(sqliteOpenHelperClass)) {
            WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                @Override
                public void write(JavaWriter javaWriter) throws IOException {
                    javaWriter.emitStatement("return new %1s(this, internalHelperListener)", sqliteOpenHelperClass);
                }
            }, "FlowSQLiteOpenHelper", "createHelper", DatabaseHandler.METHOD_MODIFIERS);
        }

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

    /**
     * <p>Checks if databaseName is valid. It will check if databaseName matches regex pattern
     * [A-Za-z_$]+[a-zA-Z0-9_$]*</p> Examples: <ul> <li>database - valid</li> <li>DbFlow1 - valid</li> <li>database.db -
     * invalid (contains a dot)</li> <li>1database - invalid (starts with a number)</li> </ul>
     *
     * @param databaseName database name to validate.
     * @return {@code true} if parameter is a valid database name, {@code false} otherwise.
     */
    private static boolean isValidDatabaseName(final String databaseName) {
        final Pattern javaClassNamePattern = Pattern.compile("[A-Za-z_$]+[a-zA-Z0-9_$]*");
        return javaClassNamePattern.matcher(databaseName).matches();
    }
}
