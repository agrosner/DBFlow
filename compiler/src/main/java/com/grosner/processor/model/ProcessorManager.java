package com.grosner.processor.model;

import com.google.common.collect.Maps;
import com.grosner.processor.definition.ModelContainerDefinition;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.definition.TypeConverterDefinition;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ProcessorManager {

    private ProcessingEnvironment mProcessingEnv;

    private Map<String, TypeConverterDefinition> mTypeConverters = Maps.newHashMap();

    private Map<String, ModelContainerDefinition> mModelContainers = Maps.newHashMap();

    private Map<String, TableDefinition> mTableDefinitions = Maps.newHashMap();

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

    public void addTypeConverterDefinition(TypeConverterDefinition definition) {
        mTypeConverters.put(definition.getModelClassQualifiedName(), definition);
    }

    public TypeConverterDefinition getTypeConverterDefinition(TypeElement typeElement) {
        return mTypeConverters.get(typeElement.getQualifiedName().toString());
    }

    public void addModelContainerDefinition(ModelContainerDefinition modelContainerDefinition) {
        mModelContainers.put(modelContainerDefinition.getModelClassQualifiedName(), modelContainerDefinition);
    }

    public ModelContainerDefinition getModelContainerDefinition(TypeElement typeElement) {
        return mModelContainers.get(typeElement.getQualifiedName().toString());
    }

    public void addTableDefinition(TableDefinition modelContainerDefinition) {
        mTableDefinitions.put(modelContainerDefinition.element.asType().toString(), modelContainerDefinition);
    }

    public TableDefinition getTableDefinition(TypeElement typeElement) {
        return mTableDefinitions.get(typeElement.getQualifiedName().toString());
    }


}
