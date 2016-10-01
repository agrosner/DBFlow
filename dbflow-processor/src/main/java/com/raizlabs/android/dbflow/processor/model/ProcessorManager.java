package com.raizlabs.android.dbflow.processor.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.FlowManagerHolderDefinition;
import com.raizlabs.android.dbflow.processor.definition.ManyToManyDefinition;
import com.raizlabs.android.dbflow.processor.definition.MigrationDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
import com.raizlabs.android.dbflow.processor.definition.QueryModelDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseHolderDefinition;
import com.raizlabs.android.dbflow.processor.handler.BaseContainerHandler;
import com.raizlabs.android.dbflow.processor.handler.Handler;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.validator.ContentProviderValidator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Description: Holds onto {@link com.raizlabs.android.dbflow.processor.definition.Definition}, Writers,
 * and provides some handy methods for interacting with the {@link javax.annotation.processing.Processor}
 */
public class ProcessorManager implements Handler {

    private static ProcessorManager manager;

    public static ProcessorManager getManager() {
        return manager;
    }

    public static void setManager(ProcessorManager manager) {
        ProcessorManager.manager = manager;
    }

    private ProcessingEnvironment processingEnvironment;
    private List<TypeName> uniqueDatabases = Lists.newArrayList();
    private Map<TypeName, TypeName> modelToDatabaseMap = Maps.newHashMap();
    private Map<TypeName, TypeConverterDefinition> typeConverters = Maps.newHashMap();
    private Map<TypeName, Map<Integer, List<MigrationDefinition>>> migrations = Maps.newHashMap();

    private Map<TypeName, DatabaseHolderDefinition> databaseDefinitionMap = Maps.newHashMap();
    private List<BaseContainerHandler> handlers = new ArrayList<>();
    private Map<TypeName, ContentProviderDefinition> providerMap = Maps.newHashMap();


    public ProcessorManager(ProcessingEnvironment processingEnv) {
        processingEnvironment = processingEnv;
        setManager(this);
    }

    public void addHandlers(BaseContainerHandler... containerHandlers) {
        for (BaseContainerHandler containerHandler : containerHandlers) {
            if (!handlers.contains(containerHandler)) {
                handlers.add(containerHandler);
            }
        }
    }

    public Messager getMessager() {
        return processingEnvironment.getMessager();
    }

    public Types getTypeUtils() {
        return processingEnvironment.getTypeUtils();
    }

    public Elements getElements() {
        return processingEnvironment.getElementUtils();
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnvironment;
    }

    public void addDatabase(TypeName database) {
        if (!uniqueDatabases.contains(database)) {
            uniqueDatabases.add(database);
        }
    }

    public void addFlowManagerWriter(DatabaseDefinition databaseDefinition) {
        DatabaseHolderDefinition holderDefinition = getOrPutDatabase(databaseDefinition.elementClassName);
        holderDefinition.setDatabaseDefinition(databaseDefinition);
    }

    public List<DatabaseHolderDefinition> getDatabaseDefinitionMap() {
        return new ArrayList<>(databaseDefinitionMap.values());
    }

    public DatabaseHolderDefinition getDatabaseHolderDefinition(TypeName databaseName) {
        return databaseDefinitionMap.get(databaseName);
    }

    public void addTypeConverterDefinition(TypeConverterDefinition definition) {
        typeConverters.put(definition.getModelTypeName(), definition);
    }

    public TypeConverterDefinition getTypeConverterDefinition(TypeName typeName) {
        return typeConverters.get(typeName);
    }

    public void addModelToDatabase(TypeName modelType, TypeName databaseName) {
        addDatabase(databaseName);
        modelToDatabaseMap.put(modelType, databaseName);
    }

    public TypeName getDatabase(TypeName modelType) {
        return modelToDatabaseMap.get(modelType);
    }

    public String getDatabaseName(TypeName databaseTypeName) {
        return getOrPutDatabase(databaseTypeName).getDatabaseDefinition().databaseName;
    }

    public void addQueryModelDefinition(QueryModelDefinition queryModelDefinition) {
        getOrPutDatabase(queryModelDefinition.databaseTypeName).queryModelDefinitionMap.
                put(queryModelDefinition.elementClassName, queryModelDefinition);
    }

    public void addTableDefinition(TableDefinition tableDefinition) {
        DatabaseHolderDefinition holderDefinition = getOrPutDatabase(tableDefinition.databaseTypeName);
        holderDefinition.tableDefinitionMap.put(tableDefinition.elementClassName, tableDefinition);
        if (holderDefinition.tableNameMap.containsKey(tableDefinition.tableName)) {
            logError("Found duplicate table %1s for database %1s", tableDefinition.tableName,
                    holderDefinition.getDatabaseDefinition().databaseName);
        } else {
            holderDefinition.tableNameMap.put(tableDefinition.tableName, tableDefinition);
        }
    }

    public void addManyToManyDefinition(ManyToManyDefinition manyToManyDefinition) {
        DatabaseHolderDefinition databaseHolderDefinition = getOrPutDatabase(manyToManyDefinition.databaseTypeName);

        List<ManyToManyDefinition> manyToManyDefinitions = databaseHolderDefinition.manyToManyDefinitionMap.get(manyToManyDefinition.elementClassName);
        if (manyToManyDefinitions == null) {
            manyToManyDefinitions = new ArrayList<>();
            databaseHolderDefinition.manyToManyDefinitionMap.put(manyToManyDefinition.elementClassName, manyToManyDefinitions);
        }

        manyToManyDefinitions.add(manyToManyDefinition);
    }

    public TableDefinition getTableDefinition(TypeName databaseName, TypeName typeName) {
        return getOrPutDatabase(databaseName).tableDefinitionMap.get(typeName);
    }

    public TableDefinition getTableDefinition(TypeName databaseName, String tableName) {
        return getOrPutDatabase(databaseName).tableNameMap.get(tableName);
    }

    public void addModelViewDefinition(ModelViewDefinition modelViewDefinition) {
        getOrPutDatabase(modelViewDefinition.databaseName).modelViewDefinitionMap
                .put(modelViewDefinition.elementClassName, modelViewDefinition);
    }

    public Set<TypeConverterDefinition> getTypeConverters() {
        return Sets.newHashSet(typeConverters.values());
    }

    public Set<TableDefinition> getTableDefinitions(TypeName databaseName) {
        DatabaseHolderDefinition databaseHolderDefinition = getOrPutDatabase(databaseName);
        return Sets.newHashSet(databaseHolderDefinition.tableDefinitionMap.values());
    }

    public void setTableDefinitions(Map<TypeName, TableDefinition> tableDefinitionSet, TypeName databaseName) {
        DatabaseHolderDefinition databaseDefinition = getOrPutDatabase(databaseName);
        databaseDefinition.tableDefinitionMap = tableDefinitionSet;
    }

    public Set<ModelViewDefinition> getModelViewDefinitions(TypeName databaseName) {
        DatabaseHolderDefinition databaseDefinition = getOrPutDatabase(databaseName);
        return Sets.newHashSet(databaseDefinition.modelViewDefinitionMap.values());
    }

    public void setModelViewDefinitions(Map<TypeName, ModelViewDefinition> modelViewDefinitionMap, ClassName elementClassName) {
        DatabaseHolderDefinition databaseDefinition = getOrPutDatabase(elementClassName);
        databaseDefinition.modelViewDefinitionMap = modelViewDefinitionMap;
    }

    public Set<QueryModelDefinition> getQueryModelDefinitions(TypeName databaseName) {
        DatabaseHolderDefinition databaseDefinition = getOrPutDatabase(databaseName);
        return Sets.newHashSet(databaseDefinition.queryModelDefinitionMap.values());
    }

    public void addMigrationDefinition(MigrationDefinition migrationDefinition) {
        Map<Integer, List<MigrationDefinition>> migrationDefinitionMap = migrations.get(
                migrationDefinition.databaseName);
        if (migrationDefinitionMap == null) {
            migrationDefinitionMap = Maps.newHashMap();
            migrations.put(migrationDefinition.databaseName, migrationDefinitionMap);
        }

        List<MigrationDefinition> migrationDefinitions = migrationDefinitionMap.get(migrationDefinition.version);
        if (migrationDefinitions == null) {
            migrationDefinitions = Lists.newArrayList();
            migrationDefinitionMap.put(migrationDefinition.version, migrationDefinitions);
        }

        if (!migrationDefinitions.contains(migrationDefinition)) {
            migrationDefinitions.add(migrationDefinition);
        }
    }

    public Map<Integer, List<MigrationDefinition>> getMigrationsForDatabase(TypeName databaseName) {
        Map<Integer, List<MigrationDefinition>> migrationDefinitions = migrations.get(databaseName);
        if (migrationDefinitions != null) {
            return migrationDefinitions;
        } else {
            return Maps.newHashMap();
        }
    }

    public void addContentProviderDefinition(ContentProviderDefinition contentProviderDefinition) {
        DatabaseHolderDefinition holderDefinition = getOrPutDatabase(contentProviderDefinition.databaseName);
        holderDefinition.providerMap.put(contentProviderDefinition.elementTypeName, contentProviderDefinition);
        providerMap.put(contentProviderDefinition.elementTypeName, contentProviderDefinition);
    }

    public void putTableEndpointForProvider(TableEndpointDefinition tableEndpointDefinition) {
        ContentProviderDefinition contentProviderDefinition = providerMap.get(
                tableEndpointDefinition.contentProviderName);
        if (contentProviderDefinition == null) {
            logError("Content Provider %1s was not found for the @TableEndpoint %1s",
                    tableEndpointDefinition.contentProviderName, tableEndpointDefinition.elementClassName);
        } else {
            contentProviderDefinition.endpointDefinitions.add(tableEndpointDefinition);
        }
    }

    public void logError(String error, Object... args) {
        getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("*==========*\n" + error.trim() + "\n*==========*", args));
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length > 5) {
            stackTraceElements = Arrays.copyOf(stackTraceElements, 5);
        }
        for (StackTraceElement stackTrace : stackTraceElements) {
            getMessager().printMessage(Diagnostic.Kind.ERROR, stackTrace + "");
        }
    }

    public void logError(Class callingClass, String error, Object... args) {
        logError(callingClass + ": " + error, args);
    }

    public void logWarning(String error, Object... args) {
        getMessager().printMessage(Diagnostic.Kind.WARNING, String.format("*==========*\n" + error + "\n*==========*", args));
    }

    public void logWarning(Class callingClass, String error, Object... args) {
        logWarning(callingClass + ":" + error, args);
    }

    private DatabaseHolderDefinition getOrPutDatabase(TypeName databaseName) {
        DatabaseHolderDefinition holderDefinition = databaseDefinitionMap.get(databaseName);
        if (holderDefinition == null) {
            holderDefinition = new DatabaseHolderDefinition();
            databaseDefinitionMap.put(databaseName, holderDefinition);
        }
        return holderDefinition;
    }

    @Override
    public void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment) {
        for (BaseContainerHandler containerHandler : handlers) {
            containerHandler.handle(processorManager, roundEnvironment);
        }

        List<DatabaseHolderDefinition> databaseDefinitions = getDatabaseDefinitionMap();
        for (DatabaseHolderDefinition databaseDefinition : databaseDefinitions) {
            try {

                if (databaseDefinition.getDatabaseDefinition() == null) {
                    ProcessorManager.getManager().logError("Found null db with: %1s tables, %1s modelviews. " +
                                    "Attempt to rebuild project should fix this intermittant issue.",
                            databaseDefinition.tableNameMap.values().size(),
                            databaseDefinition.modelViewDefinitionMap.values().size());
                    ProcessorManager.getManager().logError("Found tables: " +
                            databaseDefinition.tableNameMap.values());
                    continue;
                }

                Collection<List<ManyToManyDefinition>> manyToManyDefinitions =
                        databaseDefinition.manyToManyDefinitionMap.values();
                for (List<ManyToManyDefinition> manyToManyList : manyToManyDefinitions) {
                    for (ManyToManyDefinition manyToMany : manyToManyList) {
                        manyToMany.prepareForWrite();
                        WriterUtils.writeBaseDefinition(manyToMany, processorManager);
                    }
                }

                // process all in next round.
                if (!manyToManyDefinitions.isEmpty()) {
                    manyToManyDefinitions.clear();
                    continue;
                }

                ContentProviderValidator validator = new ContentProviderValidator();
                Collection<ContentProviderDefinition> contentProviderDefinitions = databaseDefinition.providerMap.values();
                for (ContentProviderDefinition contentProviderDefinition : contentProviderDefinitions) {
                    contentProviderDefinition.prepareForWrite();
                    if (validator.validate(processorManager, contentProviderDefinition)) {
                        WriterUtils.writeBaseDefinition(contentProviderDefinition, processorManager);
                    }
                }

                databaseDefinition.getDatabaseDefinition().validateAndPrepareToWrite();

                if (roundEnvironment.processingOver()) {
                    JavaFile.builder(databaseDefinition.getDatabaseDefinition().packageName,
                            databaseDefinition.getDatabaseDefinition().getTypeSpec())
                            .build().writeTo(processorManager.getProcessingEnvironment().getFiler());
                }


                Collection<TableDefinition> tableDefinitions = databaseDefinition.tableDefinitionMap.values();

                for (TableDefinition tableDefinition : tableDefinitions) {
                    WriterUtils.writeBaseDefinition(tableDefinition, processorManager);
                }

                List<ModelViewDefinition> modelViewDefinitions = new ArrayList<>(databaseDefinition.modelViewDefinitionMap.values());
                Collections.sort(modelViewDefinitions);
                for (ModelViewDefinition modelViewDefinition : modelViewDefinitions) {
                    WriterUtils.writeBaseDefinition(modelViewDefinition, processorManager);
                }

                Collection<QueryModelDefinition> queryModelDefinitions = databaseDefinition.queryModelDefinitionMap.values();
                for (QueryModelDefinition queryModelDefinition : queryModelDefinitions) {
                    WriterUtils.writeBaseDefinition(queryModelDefinition, processorManager);
                }

                for (TableDefinition tableDefinition : tableDefinitions) {
                    try {
                        tableDefinition.writePackageHelper(processorManager.getProcessingEnvironment());
                    } catch (FilerException e) { /*Ignored intentionally to allow multi-round table generation*/ }
                }

                for (ModelViewDefinition modelViewDefinition : modelViewDefinitions) {
                    try {
                        modelViewDefinition.writePackageHelper(processorManager.getProcessingEnvironment());
                    } catch (FilerException e) { /*Ignored intentionally to allow multi-round table generation*/ }
                }

                for (QueryModelDefinition queryModelDefinition : queryModelDefinitions) {
                    try {
                        queryModelDefinition.writePackageHelper(processorManager.getProcessingEnvironment());
                    } catch (FilerException e) { /*Ignored intentionally to allow multi-round table generation*/ }
                }
            } catch (IOException e) {
            }
        }

        if (roundEnvironment.processingOver()) {
            try {
                JavaFile.builder(ClassNames.FLOW_MANAGER_PACKAGE,
                        new FlowManagerHolderDefinition(processorManager).getTypeSpec())
                        .build().writeTo(processorManager.getProcessingEnvironment().getFiler());
            } catch (IOException e) {
                logError(e.getMessage());
            }
        }
    }
}
