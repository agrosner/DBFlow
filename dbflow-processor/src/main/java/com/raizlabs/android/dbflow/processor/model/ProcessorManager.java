package com.raizlabs.android.dbflow.processor.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.FlowManagerHolderDefinition;
import com.raizlabs.android.dbflow.processor.definition.MigrationDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
import com.raizlabs.android.dbflow.processor.definition.QueryModelDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.handler.BaseContainerHandler;
import com.raizlabs.android.dbflow.processor.handler.Handler;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.validator.ContentProviderValidator;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private ProcessingEnvironment processingEnvironment;
    private List<TypeName> uniqueDatabases = Lists.newArrayList();
    private Map<TypeName, TypeName> modelToDatabaseMap = Maps.newHashMap();
    private Map<TypeName, TypeConverterDefinition> typeConverters = Maps.newHashMap();
    private Map<TypeName, Map<Integer, List<MigrationDefinition>>> migrations = Maps.newHashMap();

    private Map<TypeName, DatabaseDefinition> databaseDefinitionMap = Maps.newHashMap();
    private List<BaseContainerHandler> handlers = new ArrayList<>();
    private Map<String, ContentProviderDefinition> providerMap = Maps.newHashMap();

    public ProcessorManager(ProcessingEnvironment processingEnv) {
        processingEnvironment = processingEnv;
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
        databaseDefinitionMap.put(databaseDefinition.elementClassName, databaseDefinition);
    }

    public List<DatabaseDefinition> getDatabaseDefinitionMap() {
        return new ArrayList<>(databaseDefinitionMap.values());
    }

    public DatabaseDefinition getDatabaseWriter(TypeName databaseName) {
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
        return databaseDefinitionMap.get(databaseTypeName).databaseName;
    }

    public void addModelContainerDefinition(ModelContainerDefinition modelContainerDefinition) {
        databaseDefinitionMap.get(modelContainerDefinition.getDatabaseName())
                .modelContainerDefinitionMap.put(modelContainerDefinition.elementClassName,
                modelContainerDefinition);
    }

    public void addQueryModelDefinition(QueryModelDefinition queryModelDefinition) {
        databaseDefinitionMap.get(queryModelDefinition.databaseTypeName).queryModelDefinitionMap.
                put(queryModelDefinition.elementClassName, queryModelDefinition);
    }

    public void addTableDefinition(TableDefinition tableDefinition) {
        DatabaseDefinition databaseDefinition = databaseDefinitionMap.get(tableDefinition.databaseTypeName);
        databaseDefinition.tableDefinitionMap.put(tableDefinition.elementClassName, tableDefinition);
        databaseDefinition.tableNameMap.put(tableDefinition.tableName, tableDefinition);
    }

    public TableDefinition getTableDefinition(TypeName databaseName, TypeName typeName) {
        return databaseDefinitionMap.get(databaseName).tableDefinitionMap.get(typeName);
    }

    public TableDefinition getTableDefinition(TypeName databaseName, String tableName) {
        return databaseDefinitionMap.get(databaseName).tableNameMap.get(tableName);
    }

    public void addModelViewDefinition(ModelViewDefinition modelViewDefinition) {
        databaseDefinitionMap.get(modelViewDefinition.databaseName).modelViewDefinitionMap
                .put(modelViewDefinition.elementClassName, modelViewDefinition);
    }

    public Set<TypeConverterDefinition> getTypeConverters() {
        return Sets.newHashSet(typeConverters.values());
    }

    public Set<ModelContainerDefinition> getModelContainers(TypeName databaseName) {
        DatabaseDefinition databaseDefinition = databaseDefinitionMap.get(databaseName);
        if (databaseDefinition != null) {
            return Sets.newHashSet(databaseDefinition.modelContainerDefinitionMap.values());
        }
        return Sets.newHashSet();
    }

    public Set<TableDefinition> getTableDefinitions(TypeName databaseName) {
        DatabaseDefinition databaseDefinition = databaseDefinitionMap.get(databaseName);
        if (databaseDefinition != null) {
            return Sets.newHashSet(databaseDefinition.tableNameMap.values());
        }
        return Sets.newHashSet();
    }

    public Set<ModelViewDefinition> getModelViewDefinitions(TypeName databaseName) {
        DatabaseDefinition databaseDefinition = databaseDefinitionMap.get(databaseName);
        if (databaseDefinition != null) {
            return Sets.newHashSet(databaseDefinition.modelViewDefinitionMap.values());
        }
        return Sets.newHashSet();
    }

    public Set<QueryModelDefinition> getQueryModelDefinitions(TypeName databaseName) {
        DatabaseDefinition databaseDefinition = databaseDefinitionMap.get(databaseName);
        if (databaseDefinition != null) {
            return Sets.newHashSet(databaseDefinition.queryModelDefinitionMap.values());
        } else {
            return Sets.newHashSet();
        }
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
        providerMap.put(contentProviderDefinition.elementClassName.simpleName(), contentProviderDefinition);
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
        getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(error, args));
    }

    public void logError(Class callingClass, String error, Object... args) {
        logError(callingClass + ": " + error, args);
    }

    @Override
    public void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment) {
        for (BaseContainerHandler containerHandler : handlers) {
            containerHandler.handle(processorManager, roundEnvironment);
        }

        ContentProviderValidator validator = new ContentProviderValidator();
        Collection<ContentProviderDefinition> contentProviderDefinitions = providerMap.values();
        for (ContentProviderDefinition contentProviderDefinition : contentProviderDefinitions) {
            if (validator.validate(processorManager, contentProviderDefinition)) {
                WriterUtils.writeBaseDefinition(contentProviderDefinition, processorManager);
            }
        }
        List<DatabaseDefinition> databaseDefinitions = getDatabaseDefinitionMap();
        for (DatabaseDefinition databaseDefinition : databaseDefinitions) {
            try {
                JavaFile.builder(databaseDefinition.packageName, databaseDefinition.getTypeSpec())
                        .build().writeTo(processorManager.getProcessingEnvironment().getFiler());

                Collection<TableDefinition> tableDefinitions = databaseDefinition.tableDefinitionMap.values();
                for (TableDefinition tableDefinition : tableDefinitions) {
                    WriterUtils.writeBaseDefinition(tableDefinition, processorManager);
                    tableDefinition.writeAdapter(processorManager.getProcessingEnvironment());
                }

                Collection<ModelContainerDefinition> modelContainerDefinitions = databaseDefinition.modelContainerDefinitionMap.values();
                for (ModelContainerDefinition modelContainerDefinition : modelContainerDefinitions) {
                    WriterUtils.writeBaseDefinition(modelContainerDefinition, processorManager);
                }

                Collection<ModelViewDefinition> modelViewDefinitions = databaseDefinition.modelViewDefinitionMap.values();
                for (ModelViewDefinition modelViewDefinition : modelViewDefinitions) {
                    WriterUtils.writeBaseDefinition(modelViewDefinition, processorManager);
                }

                Collection<QueryModelDefinition> queryModelDefinitions = databaseDefinition.queryModelDefinitionMap.values();
                for (QueryModelDefinition queryModelDefinition : queryModelDefinitions) {
                    WriterUtils.writeBaseDefinition(queryModelDefinition, processorManager);
                    queryModelDefinition.writeAdapter(processorManager.getProcessingEnvironment());
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
            }
        }
    }
}
