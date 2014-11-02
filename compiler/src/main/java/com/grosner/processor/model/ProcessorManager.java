package com.grosner.processor.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.grosner.processor.definition.ModelContainerDefinition;
import com.grosner.processor.definition.ModelViewDefinition;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.definition.TypeConverterDefinition;
import com.grosner.processor.writer.FlowManagerWriter;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ProcessorManager {

    private ProcessingEnvironment mProcessingEnv;

    private Map<String, String> mModelToDatabaseMap = Maps.newHashMap();

    private Map<String, TypeConverterDefinition> mTypeConverters = Maps.newHashMap();

    private Map<String, Map<String, ModelContainerDefinition>> mModelContainers = Maps.newHashMap();

    private Map<String, Map<String, TableDefinition>> mTableDefinitions = Maps.newHashMap();

    private Map<String, Map<String, ModelViewDefinition>> mModelViewDefinition = Maps.newHashMap();

    private List<FlowManagerWriter> mManagerWriters = Lists.newArrayList();

    public ProcessorManager(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
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

    public void addFlowManagerWriter(FlowManagerWriter flowManagerWriter) {
        mManagerWriters.add(flowManagerWriter);
    }

    public List<FlowManagerWriter> getManagerWriters() {
        return mManagerWriters;
    }

    public void addTypeConverterDefinition(TypeConverterDefinition definition) {
        mTypeConverters.put(definition.getModelClassQualifiedName(), definition);
    }

    public TypeConverterDefinition getTypeConverterDefinition(TypeElement typeElement) {
        return mTypeConverters.get(typeElement.getQualifiedName().toString());
    }

    public void addModelToDatabase(String modelName, String databaseName) {
        mModelToDatabaseMap.put(modelName, databaseName);
    }

    public String getDatabase(String modelName) {
        return mModelToDatabaseMap.get(modelName);
    }

    public void addModelContainerDefinition(ModelContainerDefinition modelContainerDefinition) {
        String modelClassName = modelContainerDefinition.classElement.getSimpleName().toString();
        Map<String, ModelContainerDefinition> modelContainerDefinitionMap = mModelContainers.get(getDatabase(modelClassName));
        if(modelContainerDefinitionMap == null) {
            modelContainerDefinitionMap = Maps.newHashMap();
            mModelContainers.put(getDatabase(modelClassName), modelContainerDefinitionMap);
        }
        modelContainerDefinitionMap.put(modelContainerDefinition.getModelClassQualifiedName(), modelContainerDefinition);
    }

    public ModelContainerDefinition getModelContainerDefinition(String databaseName, TypeElement typeElement) {
        return mModelContainers.get(databaseName).get(typeElement.getQualifiedName().toString());
    }

    public void addTableDefinition(TableDefinition modelContainerDefinition) {
        Map<String, TableDefinition> tableDefinitionMap = mTableDefinitions.get(modelContainerDefinition.databaseName);
        if(tableDefinitionMap == null) {
            tableDefinitionMap = Maps.newHashMap();
            mTableDefinitions.put(modelContainerDefinition.databaseName, tableDefinitionMap);
        }
        tableDefinitionMap.put(modelContainerDefinition.element.asType().toString(), modelContainerDefinition);
    }

    public TableDefinition getTableDefinition(String databaseName, TypeElement typeElement) {
        return mTableDefinitions.get(databaseName).get(typeElement.getQualifiedName().toString());
    }

    public void addModelViewDefinition(ModelViewDefinition modelViewDefinition) {
        Map<String, ModelViewDefinition> modelViewDefinitionMap = mModelViewDefinition.get(modelViewDefinition.databaseName);
        if(modelViewDefinitionMap == null) {
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
        if(modelContainerDefinitionMap!= null) {
            return  Sets.newHashSet(mModelContainers.get(databaseName).values());
        }
        return Sets.newHashSet();
    }

    public Set<TableDefinition> getTableDefinitions(String databaseName) {
        Map<String, TableDefinition> tableDefinitionMap = mTableDefinitions.get(databaseName);
        if(tableDefinitionMap != null) {
            return Sets.newHashSet(mTableDefinitions.get(databaseName).values());
        }
        return Sets.newHashSet();
    }

    public Set<ModelViewDefinition> getModelViewDefinitions(String databaseName) {
        Map<String, ModelViewDefinition> modelViewDefinitionMap = mModelViewDefinition.get(databaseName);
        if(modelViewDefinitionMap != null) {
            return Sets.newHashSet(mModelViewDefinition.get(databaseName).values());
        } else {
            return Sets.newHashSet();
        }
    }
}
