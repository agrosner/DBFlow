package com.grosner.processor.definition;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.ModelView;
import com.grosner.processor.Classes;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelViewDefinition implements FlowWriter {

    private static final String DBFLOW_MODEL_VIEW_TAG = "$View";

    private final String modelViewSourceClassName;

    public Element element;

    public String databaseName;

    public String packageName;

    public String modelViewClassName;

    public ModelViewDefinition(ProcessorManager manager, String packageName, Element element) {
        this.element = element;
        this.packageName = packageName;
        this.modelViewClassName = element.getSimpleName().toString();
        this.databaseName = element.getAnnotation(ModelView.class).databaseName();
        this.modelViewSourceClassName = modelViewClassName + DBFLOW_MODEL_VIEW_TAG;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitPackage(packageName);

        //javaWriter.emitImports(;

        javaWriter.beginType(modelViewSourceClassName, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), null, Classes.MODEL_VIEW);


        javaWriter.endType();
    }
}
