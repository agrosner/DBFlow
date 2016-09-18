package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.BaseDefinition;
import com.raizlabs.android.dbflow.processor.definition.MigrationDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
import com.raizlabs.android.dbflow.processor.definition.QueryModelDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeDefinition;
import com.raizlabs.android.dbflow.processor.handler.DatabaseHandler;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.raizlabs.android.dbflow.processor.validator.ModelViewValidator;
import com.raizlabs.android.dbflow.processor.validator.TableValidator;
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
    public String fieldRefSeparator; // safe field for javapoet

    public boolean isInMemory;

    private DatabaseHolderDefinition holderDefinition;

    public DatabaseDefinition(ProcessorManager manager, Element element) {
        super(element, manager);
        packageName = ClassNames.FLOW_MANAGER_PACKAGE;

        Database database = element.getAnnotation(Database.class);
        if (database != null) {
            databaseName = database.name();
            if (databaseName == null || databaseName.isEmpty()) {
                databaseName = element.getSimpleName().toString();
            }
            if (!isValidDatabaseName(databaseName)) {
                throw new Error("Database name [ " + databaseName + " ] is not valid. It must pass [A-Za-z_$]+[a-zA-Z0-9_$]* " +
                        "regex so it can't start with a number or contain any special character except '$'. Especially a dot character is not allowed!");
            }

            consistencyChecksEnabled = database.consistencyCheckEnabled();
            backupEnabled = database.backupEnabled();

            classSeparator = database.generatedClassSeparator();

            if (!StringUtils.isNullOrEmpty(classSeparator)) {
                // all are $
                fieldRefSeparator = classSeparator;
            } else {
                fieldRefSeparator = classSeparator;
            }

            setOutputClassName(databaseName + classSeparator + "Database");

            databaseVersion = database.version();
            foreignKeysSupported = database.foreignKeysSupported();

            insertConflict = database.insertConflict();
            updateConflict = database.updateConflict();
            isInMemory = database.inMemory();
        }
    }

    public DatabaseHolderDefinition getHolderDefinition() {
        return holderDefinition;
    }

    public void setHolderDefinition(DatabaseHolderDefinition holderDefinition) {
        this.holderDefinition = holderDefinition;
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

    public void validateAndPrepareToWrite() {
        prepareDefinitions();
        validateDefinitions();
    }

    private void validateDefinitions() {
        // TODO: validate them here before preparing them
        Map<TypeName, TableDefinition> map = new HashMap<>();
        TableValidator tableValidator = new TableValidator();
        for (TableDefinition tableDefinition : manager.getTableDefinitions(elementClassName)) {
            if (tableValidator.validate(ProcessorManager.getManager(), tableDefinition)) {
                map.put(tableDefinition.elementClassName, tableDefinition);
            }
        }
        manager.setTableDefinitions(map, elementClassName);

        Map<TypeName, ModelViewDefinition> modelViewDefinitionMap = new HashMap<>();
        ModelViewValidator modelViewValidator = new ModelViewValidator();
        for (ModelViewDefinition modelViewDefinition : manager.getModelViewDefinitions(elementClassName)) {
            if (modelViewValidator.validate(ProcessorManager.getManager(), modelViewDefinition)) {
                modelViewDefinitionMap.put(modelViewDefinition.elementClassName, modelViewDefinition);
            }
        }
        manager.setModelViewDefinitions(modelViewDefinitionMap, elementClassName);

    }

    private void prepareDefinitions() {
        for (TableDefinition tableDefinition : manager.getTableDefinitions(elementClassName)) {
            tableDefinition.prepareForWrite();
        }

        for (ModelViewDefinition modelViewDefinition : manager.getModelViewDefinitions(elementClassName)) {
            modelViewDefinition.prepareForWrite();
        }

        for (QueryModelDefinition queryModelDefinition : manager.getQueryModelDefinitions(elementClassName)) {
            queryModelDefinition.prepareForWrite();
        }
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
                    constructor.addStatement("migrations$L.add(new $T$L)", version, migrationDefinition.elementClassName,
                            migrationDefinition.getConstructorName());
                }
            }
        }

        for (TableDefinition tableDefinition : manager.getTableDefinitions(elementClassName)) {
            constructor.addStatement("$L.add($T.class)", DatabaseHandler.MODEL_FIELD_NAME, tableDefinition.elementClassName);
            constructor.addStatement("$L.put($S, $T.class)", DatabaseHandler.MODEL_NAME_MAP, tableDefinition.tableName, tableDefinition.elementClassName);
            constructor.addStatement("$L.put($T.class, new $T(holder, this))", DatabaseHandler.MODEL_ADAPTER_MAP_FIELD_NAME,
                    tableDefinition.elementClassName, tableDefinition.getAdapterClassName());
        }

        for (ModelViewDefinition modelViewDefinition : manager.getModelViewDefinitions(elementClassName)) {
            constructor.addStatement("$L.add($T.class)", DatabaseHandler.MODEL_VIEW_FIELD_NAME, modelViewDefinition.elementClassName);
            constructor.addStatement("$L.put($T.class, new $T(holder, this))", DatabaseHandler.MODEL_VIEW_ADAPTER_MAP_FIELD_NAME,
                    modelViewDefinition.elementClassName, modelViewDefinition.outputClassName);
        }

        for (QueryModelDefinition queryModelDefinition : manager.getQueryModelDefinitions(elementClassName)) {
            constructor.addStatement("$L.put($T.class, new $T(holder, this))", DatabaseHandler.QUERY_MODEL_ADAPTER_MAP_FIELD_NAME,
                    queryModelDefinition.elementClassName, queryModelDefinition.getAdapterClassName());
        }

        builder.addMethod(constructor.build());
    }

    private void writeGetters(TypeSpec.Builder typeBuilder) {

        typeBuilder.addMethod(MethodSpec.methodBuilder("getAssociatedDatabaseClassFile")
                .addAnnotation(Override.class)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return $T.class", elementTypeName)
                .returns(ParameterizedTypeName.get(Class.class)).build());

        typeBuilder.addMethod(MethodSpec.methodBuilder("isForeignKeysSupported")
                .addAnnotation(Override.class)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return $L", foreignKeysSupported)
                .returns(TypeName.BOOLEAN).build());

        typeBuilder.addMethod(MethodSpec.methodBuilder("isInMemory")
                .addAnnotation(Override.class)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return $L", isInMemory)
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
