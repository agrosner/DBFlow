package com.raizlabs.android.dbflow.processor.definition.method;

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
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Description: Writes {@link com.raizlabs.android.dbflow.annotation.Database} definitions,
 * which contain {@link com.raizlabs.android.dbflow.annotation.Table},
 * {@link com.raizlabs.android.dbflow.annotation.ModelView}, and {@link com.raizlabs.android.dbflow.annotation.Migration}
 */
public class DatabaseDefinition extends BaseDefinition implements TypeDefinition {

    public String databaseName;

    public int databaseVersion;

    boolean foreignKeysSupported;

    boolean consistencyChecksEnabled;

    boolean backupEnabled;

    public ConflictAction insertConflict;

    public ConflictAction updateConflict;

    public String classSeparator;

    TypeName sqliteOpenHelperClass;

    public Map<TypeName, TableDefinition> tableDefinitionMap = new HashMap<>();
    public Map<String, TableDefinition> tableNameMap = new HashMap<>();

    public Map<TypeName, QueryModelDefinition> queryModelDefinitionMap = new HashMap<>();
    public Map<TypeName, ModelContainerDefinition> modelContainerDefinitionMap = new HashMap<>();
    public Map<TypeName, ModelViewDefinition> modelViewDefinitionMap = new HashMap<>();

    public DatabaseDefinition(ProcessorManager manager, Element element) {
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

        TypeMirror openHelper = ProcessorUtils.getOpenHelperClass(database);
        if (openHelper != null) {
            sqliteOpenHelperClass = TypeName.get(openHelper);
            if (sqliteOpenHelperClass.equals(TypeName.VOID.box())) {
                sqliteOpenHelperClass = ClassNames.FLOW_SQLITE_OPEN_HELPER;
            }
        } else {
            sqliteOpenHelperClass = ClassNames.FLOW_SQLITE_OPEN_HELPER;
        }

        consistencyChecksEnabled = database.consistencyCheckEnabled();
        backupEnabled = database.backupEnabled();

        classSeparator = database.generatedClassSeparator();

        setOutputClassName(databaseName + classSeparator + "Database");

        databaseVersion = database.version();
        foreignKeysSupported = database.foreignKeysSupported();

        insertConflict = database.insertConflict();
        updateConflict = database.updateConflict();

    }

    @Override
    protected TypeName getExtendsClass() {
        return ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME;
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {

        writeConstructor(typeBuilder);
        writeGetters(typeBuilder);
    }

    private void writeConstructor(TypeSpec.Builder builder) {

        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassNames.DATABASE_HOLDER, "holder");

        for (TableDefinition tableDefinition : manager.getTableDefinitions(elementClassName)) {
            constructor.addStatement("holder.putDatabaseForTable($T.class, this)", tableDefinition.elementClassName);
        }

        for (ModelViewDefinition modelViewDefinition : manager.getModelViewDefinitions(elementClassName)) {
            constructor.addStatement("holder.putDatabaseForTable($T.class, this)", modelViewDefinition.elementClassName);
        }

        for (QueryModelDefinition queryModelDefinition : manager.getQueryModelDefinitions(elementClassName)) {
            constructor.addStatement("holder.putDatabaseForTable($T.class, this)", queryModelDefinition.elementClassName);
        }

        Map<Integer, List<MigrationDefinition>> migrationDefinitionMap = manager.getMigrationsForDatabase(elementClassName);
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
                constructor.addStatement("$T migrations$L = new $T()", ParameterizedTypeName.get(ClassName.get(List.class), ClassNames.MIGRATION),
                        version, ParameterizedTypeName.get(ArrayList.class));
                constructor.addStatement("$L.put($L, migrations$L)", DatabaseHandler.MIGRATION_FIELD_NAME,
                        version, version);
                for (MigrationDefinition migrationDefinition : migrationDefinitions) {
                    constructor.addStatement("migrations$L.add(new $T())", version, migrationDefinition.elementClassName);
                }
            }
        }

        for (TableDefinition tableDefinition : manager.getTableDefinitions(elementClassName)) {
            constructor.addStatement("$L.add($T.class)", DatabaseHandler.MODEL_FIELD_NAME, tableDefinition.elementClassName);
            constructor.addStatement("$L.put($S, $T.class)", DatabaseHandler.MODEL_NAME_MAP, tableDefinition.tableName, tableDefinition.elementClassName);
            constructor.addStatement("$L.put($T.class, new $T())", DatabaseHandler.MODEL_ADAPTER_MAP_FIELD_NAME,
                    tableDefinition.elementClassName, tableDefinition.getAdapterClassName());
        }

        for (ModelContainerDefinition modelContainerDefinition : manager.getModelContainers(elementClassName)) {
            constructor.addStatement("$L.put($T.class, new $T())", DatabaseHandler.MODEL_CONTAINER_ADAPTER_MAP_FIELD_NAME,
                    modelContainerDefinition.elementClassName, modelContainerDefinition.outputClassName);
        }

        for (ModelViewDefinition modelViewDefinition : manager.getModelViewDefinitions(elementClassName)) {
            constructor.addStatement("$L.add($T.class)", DatabaseHandler.MODEL_VIEW_FIELD_NAME, modelViewDefinition.elementClassName);
            constructor.addStatement("$L.put($T.class, new $T())", DatabaseHandler.MODEL_VIEW_ADAPTER_MAP_FIELD_NAME,
                    modelViewDefinition.elementClassName, modelViewDefinition.outputClassName);
        }

        for (QueryModelDefinition queryModelDefinition : manager.getQueryModelDefinitions(elementClassName)) {
            constructor.addStatement("$L.put($T.class, new $T())", DatabaseHandler.QUERY_MODEL_ADAPTER_MAP_FIELD_NAME,
                    queryModelDefinition.elementClassName, queryModelDefinition.getAdapterClassName());
        }

        builder.addMethod(constructor.build());
    }

    private void writeGetters(TypeSpec.Builder typeBuilder) {

        // create helper
        if (!TypeName.VOID.equals(sqliteOpenHelperClass)) {
            typeBuilder.addMethod(MethodSpec.methodBuilder("createHelper")
                    .addAnnotation(Override.class)
                    .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                    .addStatement("return new $T(this, internalHelperListener)", sqliteOpenHelperClass)
                    .returns(ClassNames.FLOW_SQLITE_OPEN_HELPER).build());
        }

        typeBuilder.addMethod(MethodSpec.methodBuilder("isForeignKeysSupported")
                .addAnnotation(Override.class)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return $L", foreignKeysSupported)
                .returns(TypeName.BOOLEAN).build());

        typeBuilder.addMethod(MethodSpec.methodBuilder("backupEnabled")
                .addAnnotation(Override.class)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return $L", backupEnabled)
                .returns(TypeName.BOOLEAN).build());

        typeBuilder.addMethod(MethodSpec.methodBuilder("areConsistencyChecksEnabled")
                .addAnnotation(Override.class)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return $L", consistencyChecksEnabled)
                .returns(TypeName.BOOLEAN).build());

        typeBuilder.addMethod(MethodSpec.methodBuilder("getDatabaseVersion")
                .addAnnotation(Override.class)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return $L", databaseVersion)
                .returns(TypeName.INT).build());

        typeBuilder.addMethod(MethodSpec.methodBuilder("getDatabaseName")
                .addAnnotation(Override.class)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return $S", databaseName)
                .returns(ClassName.get(String.class)).build());
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
