package com.raizlabs.android.dbflow.processor.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.MigrationDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelContainerDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
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

    private ProcessingEnvironment mProcessingEnv;

    private List<String> mUniqueDatabases = Lists.newArrayList();

    private Map<String, String> mModelToDatabaseMap = Maps.newHashMap();

    private Map<String, TypeConverterDefinition> mTypeConverters = Maps.newHashMap();

    private Map<String, Map<String, ModelContainerDefinition>> mModelContainers = Maps.newHashMap();

    private Map<String, Map<String, TableDefinition>> mTableDefinitions = Maps.newHashMap();

    private Map<String, Map<String, TableDefinition>> mTableNameDefinitionMap = Maps.newHashMap();


    private Map<String, Map<String, ModelViewDefinition>> mModelViewDefinition = Maps.newHashMap();

    private Map<String, Map<Integer, List<MigrationDefinition>>> mMigrations = Maps.newHashMap();

    private Map<String, DatabaseWriter> mManagerWriters = Maps.newHashMap();

    private List<BaseContainerHandler> mHandlers = new ArrayList<>();

    private Map<String, ContentProviderDefinition> mProviderMap = Maps.newHashMap();

    public ProcessorManager(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
    }

    public void addHandlers(BaseContainerHandler... containerHandlers) {
        for (BaseContainerHandler containerHandler : containerHandlers) {
            if (!mHandlers.contains(containerHandler)) {
                mHandlers.add(containerHandler);
            }
        }
    }

    public Messager getMessager() {
        return mProcessingEnv.getMessager();
    }

    public Types getTypeUtils() {
        return mProcessingEnv.getTypeUtils();
    }

    public Elements getElements() {
        return mProcessingEnv.getElementUtils();
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return mProcessingEnv;
    }

    public void addDatabase(String database) {
        if (!mUniqueDatabases.contains(database)) {
            mUniqueDatabases.add(database);
        }
    }

    public boolean hasOneDatabase() {
        return mUniqueDatabases.size() == 1;
    }

    public void addFlowManagerWriter(DatabaseWriter databaseWriter) {
        mManagerWriters.put(databaseWriter.databaseName, databaseWriter);
    }

    public List<DatabaseWriter> getManagerWriters() {
        return new ArrayList<>(mManagerWriters.values());
    }

    public DatabaseWriter getDatabaseWriter(String databaseName) {
        return mManagerWriters.get(databaseName);
    }

    public void addTypeConverterDefinition(TypeConverterDefinition definition) {
        mTypeConverters.put(definition.getModelClassQualifiedName(), definition);
    }

    public TypeConverterDefinition getTypeConverterDefinition(TypeElement typeElement) {
        return mTypeConverters.get(typeElement.getQualifiedName().toString());
    }

    public void addModelToDatabase(String modelName, String databaseName) {
        addDatabase(databaseName);
        mModelToDatabaseMap.put(modelName, databaseName);
    }

    public String getDatabase(String modelName) {
        return mModelToDatabaseMap.get(modelName);
    }

    public void addModelContainerDefinition(ModelContainerDefinition modelContainerDefinition) {
        String modelClassName = modelContainerDefinition.element.getSimpleName().toString();
        Map<String, ModelContainerDefinition> modelContainerDefinitionMap = mModelContainers.get(getDatabase(modelClassName));
        if (modelContainerDefinitionMap == null) {
            modelContainerDefinitionMap = Maps.newHashMap();
            mModelContainers.put(getDatabase(modelClassName), modelContainerDefinitionMap);
        }
        modelContainerDefinitionMap.put(modelContainerDefinition.getModelClassQualifiedName(), modelContainerDefinition);
    }

    public ModelContainerDefinition getModelContainerDefinition(String databaseName, TypeElement typeElement) {
        return mModelContainers.get(databaseName).get(typeElement.getQualifiedName().toString());
    }

    public void addTableDefinition(TableDefinition tableDefinition) {
        Map<String, TableDefinition> tableDefinitionMap = mTableDefinitions.get(tableDefinition.databaseName);
        if (tableDefinitionMap == null) {
            tableDefinitionMap = Maps.newHashMap();
            mTableDefinitions.put(tableDefinition.databaseName, tableDefinitionMap);
        }
        Map<String, TableDefinition> tableNameMap = mTableNameDefinitionMap.get(tableDefinition.databaseName);
        if (tableNameMap == null) {
            tableNameMap = Maps.newHashMap();
            mTableNameDefinitionMap.put(tableDefinition.databaseName, tableNameMap);
        }

        tableDefinitionMap.put(tableDefinition.element.asType().toString(), tableDefinition);
        tableNameMap.put(tableDefinition.tableName, tableDefinition);
    }

    public TableDefinition getTableDefinition(String databaseName, TypeElement typeElement) {
        return mTableDefinitions.get(databaseName).get(typeElement.getQualifiedName().toString());
    }

    public TableDefinition getTableDefinition(String databaseName, String tableName) {
        return mTableNameDefinitionMap.get(databaseName).get(tableName);
    }

    public void addModelViewDefinition(ModelViewDefinition modelViewDefinition) {
        Map<String, ModelViewDefinition> modelViewDefinitionMap = mModelViewDefinition.get(modelViewDefinition.databaseName);
        if (modelViewDefinitionMap == null) {
            modelViewDefinitionMap = Maps.newHashMap();
            mModelViewDefinition.put(modelViewDefinition.databaseName, modelViewDefinitionMap);
        }
        modelViewDefinitionMap.put(modelViewDefinition.element.asType().toString(), modelViewDefinition);
    }

    public ModelViewDefinition getModelViewDefinition(String databaseName, TypeElement typeElement) {
        return mModelViewDefinition.get(databaseName).get(typeElement.getQualifiedName().toString());
    }

    public Set<TypeConverterDefinition> getTypeConverters() {
        return Sets.newHashSet(mTypeConverters.values());
    }

    public Set<ModelContainerDefinition> getModelContainers(String databaseName) {
        Map<String, ModelContainerDefinition> modelContainerDefinitionMap = mModelContainers.get(databaseName);
        if (modelContainerDefinitionMap != null) {
            return Sets.newHashSet(mModelContainers.get(databaseName).values());
        }
        return Sets.newHashSet();
    }

    public Set<TableDefinition> getTableDefinitions(String databaseName) {
        Map<String, TableDefinition> tableDefinitionMap = mTableDefinitions.get(databaseName);
        if (tableDefinitionMap != null) {
            return Sets.newHashSet(mTableDefinitions.get(databaseName).values());
        }
        return Sets.newHashSet();
    }

    public Set<ModelViewDefinition> getModelViewDefinitions(String databaseName) {
        Map<String, ModelViewDefinition> modelViewDefinitionMap = mModelViewDefinition.get(databaseName);
        if (modelViewDefinitionMap != null) {
            return Sets.newHashSet(mModelViewDefinition.get(databaseName).values());
        } else {
            return Sets.newHashSet();
        }
    }

    public void addMigrationDefinition(MigrationDefinition migrationDefinition) {
        Map<Integer, List<MigrationDefinition>> migrationDefinitionMap = mMigrations.get(migrationDefinition.databaseName);
        if (migrationDefinitionMap == null) {
            migrationDefinitionMap = Maps.newHashMap();
            mMigrations.put(migrationDefinition.databaseName, migrationDefinitionMap);
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
        Map<Integer, List<MigrationDefinition>> migrationDefinitions = mMigrations.get(databaseName);
        if (migrationDefinitions != null) {
            return migrationDefinitions;
        } else {
            return Maps.newHashMap();
        }
    }

    public void addContentProviderDefinition(ContentProviderDefinition contentProviderDefinition) {
        mProviderMap.put(contentProviderDefinition.elementClassName, contentProviderDefinition);
    }

    public void putTableEndpointForProvider(TableEndpointDefinition tableEndpointDefinition) {
        ContentProviderDefinition contentProviderDefinition = mProviderMap.get(tableEndpointDefinition.contentProviderName);
        if (contentProviderDefinition == null) {
            logError("Content Provider %1s was not found for the @TableEndpoint %1s", tableEndpointDefinition.contentProviderName, tableEndpointDefinition.elementClassName);
        } else {
            contentProviderDefinition.endpointDefinitions.add(tableEndpointDefinition);
        }
    }

    public void logError(String error, Object... args) {
        getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(error, args));
    }

    @Override
    public void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment) {
        for (BaseContainerHandler containerHandler : mHandlers) {
            containerHandler.handle(processorManager, roundEnvironment);
        }

        ContentProviderValidator validator = new ContentProviderValidator();
        Collection<ContentProviderDefinition> contentProviderDefinitions = mProviderMap.values();
        for (ContentProviderDefinition contentProviderDefinition : contentProviderDefinitions) {
            if (validator.validate(processorManager, contentProviderDefinition)) {
                WriterUtils.writeBaseDefinition(contentProviderDefinition, processorManager);
            }
        }
        List<DatabaseWriter> databaseWriters = getManagerWriters();
        for(DatabaseWriter databaseWriter: databaseWriters) {
            try {
                JavaWriter javaWriter = new JavaWriter(processorManager.getProcessingEnvironment().getFiler()
                        .createSourceFile(databaseWriter.getSourceFileName()).openWriter());
                databaseWriter.write(javaWriter);
                javaWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (roundEnvironment.processingOver()) {
            try {
                JavaWriter staticFlowManager = new JavaWriter(processorManager.getProcessingEnvironment().getFiler()
                        .createSourceFile(Classes.FLOW_MANAGER_PACKAGE + "." + Classes.DATABASE_HOLDER_STATIC_CLASS_NAME).openWriter());
                new FlowManagerHolderWriter(processorManager).write(staticFlowManager);

                staticFlowManager.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
