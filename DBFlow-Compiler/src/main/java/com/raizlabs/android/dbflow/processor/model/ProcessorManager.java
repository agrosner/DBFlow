package com.raizlabs.android.dbflow.processor.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.MigrationDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
import com.raizlabs.android.dbflow.processor.definition.QueryModelDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.handler.BaseContainerHandler;
import com.raizlabs.android.dbflow.processor.handler.Handler;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.validator.ContentProviderValidator;
import com.raizlabs.android.dbflow.processor.writer.DatabaseWriter;
import com.raizlabs.android.dbflow.processor.writer.FlowManagerHolderWriter;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Description: Holds onto {@link com.raizlabs.android.dbflow.processor.definition.Definition}, Writers,
 * and provides some handy methods for interacting with the {@link javax.annotation.processing.Processor}
 */
public class ProcessorManager implements Handler {

    private ProcessingEnvironment processingEnvironment;
    private List<String> uniqueDatabases = Lists.newArrayList();
    private Map<String, String> modelToDatabaseMap = Maps.newHashMap();
    private Map<String, TypeConverterDefinition> typeConverters = Maps.newHashMap();
    private Map<String, Map<String, ModelContainerDefinition>> modelContainers = Maps.newHashMap();
    private Map<String, Map<String, TableDefinition>> tableDefinitions = Maps.newHashMap();
    private Map<String, Map<String, TableDefinition>> tableNameDefinitionMap = Maps.newHashMap();
    private Map<String, Map<String, QueryModelDefinition>> queryModelDefinitionMap = Maps.newHashMap();
    private Map<String, Map<String, ModelViewDefinition>> modelViewDefinition = Maps.newHashMap();
    private Map<String, Map<Integer, List<MigrationDefinition>>> migrations = Maps.newHashMap();
    private Map<String, DatabaseWriter> managerWriters = Maps.newHashMap();
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

    public void addDatabase(String database) {
        if (!uniqueDatabases.contains(database)) {
            uniqueDatabases.add(database);
        }
    }

    public boolean hasOneDatabase() {
        return uniqueDatabases.size() == 1;
    }

    public void addFlowManagerWriter(DatabaseWriter databaseWriter) {
        managerWriters.put(databaseWriter.databaseName, databaseWriter);
    }

    public List<DatabaseWriter> getManagerWriters() {
        return new ArrayList<>(managerWriters.values());
    }

    public DatabaseWriter getDatabaseWriter(String databaseName) {
        return managerWriters.get(databaseName);
    }

    public void addTypeConverterDefinition(TypeConverterDefinition definition) {
        typeConverters.put(definition.getModelClassQualifiedName(), definition);
    }

    public TypeConverterDefinition getTypeConverterDefinition(TypeElement typeElement) {
        return typeConverters.get(typeElement.getQualifiedName().toString());
    }

    public void addModelToDatabase(String modelName, String databaseName) {
        addDatabase(databaseName);
        modelToDatabaseMap.put(modelName, databaseName);
    }

    public String getDatabase(String modelName) {
        return modelToDatabaseMap.get(modelName);
    }

    public void addModelContainerDefinition(ModelContainerDefinition modelContainerDefinition) {
        String modelClassName = modelContainerDefinition.element.getSimpleName().toString();
        Map<String, ModelContainerDefinition> modelContainerDefinitionMap = modelContainers.get(
                getDatabase(modelClassName));
        if (modelContainerDefinitionMap == null) {
            modelContainerDefinitionMap = Maps.newHashMap();
            modelContainers.put(getDatabase(modelClassName), modelContainerDefinitionMap);
        }
        modelContainerDefinitionMap.put(modelContainerDefinition.getModelClassQualifiedName(),
                                        modelContainerDefinition);
    }

    public void addQueryModelDefinition(QueryModelDefinition queryModelDefinition) {
        Map<String, QueryModelDefinition> modelDefinitionMap = queryModelDefinitionMap.get(
                getDatabase(queryModelDefinition.getQualifiedModelClassName()));
        if (modelDefinitionMap == null) {
            modelDefinitionMap = Maps.newHashMap();
            queryModelDefinitionMap.put(getDatabase(queryModelDefinition.getQualifiedModelClassName()),
                                        modelDefinitionMap);
        }
        modelDefinitionMap.put(queryModelDefinition.getQualifiedModelClassName(), queryModelDefinition);
    }

    public ModelContainerDefinition getModelContainerDefinition(String databaseName, TypeElement typeElement) {
        return modelContainers.get(databaseName).get(typeElement.getQualifiedName().toString());
    }

    public void addTableDefinition(TableDefinition tableDefinition) {
        Map<String, TableDefinition> tableDefinitionMap = tableDefinitions.get(tableDefinition.databaseName);
        if (tableDefinitionMap == null) {
            tableDefinitionMap = Maps.newHashMap();
            tableDefinitions.put(tableDefinition.databaseName, tableDefinitionMap);
        }
        Map<String, TableDefinition> tableNameMap = tableNameDefinitionMap.get(tableDefinition.databaseName);
        if (tableNameMap == null) {
            tableNameMap = Maps.newHashMap();
            tableNameDefinitionMap.put(tableDefinition.databaseName, tableNameMap);
        }

        tableDefinitionMap.put(tableDefinition.element.asType().toString(), tableDefinition);
        tableNameMap.put(tableDefinition.tableName, tableDefinition);
    }

    public TableDefinition getTableDefinition(String databaseName, TypeElement typeElement) {
        return tableDefinitions.get(databaseName).get(typeElement.getQualifiedName().toString());
    }

    public TableDefinition getTableDefinition(String databaseName, String tableName) {
        return tableNameDefinitionMap.get(databaseName).get(tableName);
    }

    public void addModelViewDefinition(ModelViewDefinition modelViewDefinition) {
        Map<String, ModelViewDefinition> modelViewDefinitionMap = this.modelViewDefinition.get(
                modelViewDefinition.databaseName);
        if (modelViewDefinitionMap == null) {
            modelViewDefinitionMap = Maps.newHashMap();
            this.modelViewDefinition.put(modelViewDefinition.databaseName, modelViewDefinitionMap);
        }
        modelViewDefinitionMap.put(modelViewDefinition.element.asType().toString(), modelViewDefinition);
    }

    public ModelViewDefinition getModelViewDefinition(String databaseName, TypeElement typeElement) {
        return modelViewDefinition.get(databaseName).get(typeElement.getQualifiedName().toString());
    }

    public Set<TypeConverterDefinition> getTypeConverters() {
        return Sets.newHashSet(typeConverters.values());
    }

    public Set<ModelContainerDefinition> getModelContainers(String databaseName) {
        Map<String, ModelContainerDefinition> modelContainerDefinitionMap = modelContainers.get(databaseName);
        if (modelContainerDefinitionMap != null) {
            return Sets.newHashSet(modelContainers.get(databaseName).values());
        }
        return Sets.newHashSet();
    }

    public Set<TableDefinition> getTableDefinitions(String databaseName) {
        Map<String, TableDefinition> tableDefinitionMap = tableDefinitions.get(databaseName);
        if (tableDefinitionMap != null) {
            return Sets.newHashSet(tableDefinitions.get(databaseName).values());
        }
        return Sets.newHashSet();
    }

    public Set<ModelViewDefinition> getModelViewDefinitions(String databaseName) {
        Map<String, ModelViewDefinition> modelViewDefinitionMap = modelViewDefinition.get(databaseName);
        if (modelViewDefinitionMap != null) {
            return Sets.newHashSet(modelViewDefinition.get(databaseName).values());
        } else {
            return Sets.newHashSet();
        }
    }

    public Set<QueryModelDefinition> getQueryModelDefinitions(String databaseName) {
        Map<String, QueryModelDefinition> modelQueryDefinition = queryModelDefinitionMap.get(databaseName);
        if (modelQueryDefinition != null) {
            return Sets.newHashSet(queryModelDefinitionMap.get(databaseName).values());
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

    public Map<Integer, List<MigrationDefinition>> getMigrationsForDatabase(String databaseName) {
        Map<Integer, List<MigrationDefinition>> migrationDefinitions = migrations.get(databaseName);
        if (migrationDefinitions != null) {
            return migrationDefinitions;
        } else {
            return Maps.newHashMap();
        }
    }

    public void addContentProviderDefinition(ContentProviderDefinition contentProviderDefinition) {
        providerMap.put(contentProviderDefinition.elementClassName, contentProviderDefinition);
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
        List<DatabaseWriter> databaseWriters = getManagerWriters();
        for (DatabaseWriter databaseWriter : databaseWriters) {
            try {
                JavaWriter javaWriter = new JavaWriter(processorManager.getProcessingEnvironment().getFiler()
                                                               .createSourceFile(
                                                                       databaseWriter.getSourceFileName()).openWriter());
                databaseWriter.write(javaWriter);
                javaWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (roundEnvironment.processingOver()) {
            try {
                JavaWriter staticFlowManager = new JavaWriter(processorManager.getProcessingEnvironment().getFiler()
                                                                      .createSourceFile(
                                                                              Classes.FLOW_MANAGER_PACKAGE + "." +
                                                                              Classes.DATABASE_HOLDER_STATIC_CLASS_NAME).openWriter());
                new FlowManagerHolderWriter(processorManager).write(staticFlowManager);

                staticFlowManager.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
