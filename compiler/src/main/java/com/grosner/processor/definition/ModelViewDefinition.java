package com.grosner.processor.definition;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ModelView;
import com.grosner.processor.Classes;
import com.grosner.processor.DBFlowProcessor;
import com.grosner.processor.handler.FlowManagerHandler;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.utils.WriterUtils;
import com.grosner.processor.writer.ExistenceWriter;
import com.grosner.processor.writer.FlowWriter;
import com.grosner.processor.writer.LoadCursorWriter;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelViewDefinition extends BaseTableDefinition implements FlowWriter {

    private static final String DBFLOW_MODEL_VIEW_TAG = "$View";

    public final String modelViewSourceClassName;

    public Element element;

    public String databaseName;

    public String packageName;

    private String query;

    private String name;

    private ProcessorManager manager;

    private LoadCursorWriter mCursorWriter;

    private ExistenceWriter mExistenceWriter;

    public ModelViewDefinition(ProcessorManager manager, String packageName, Element element) {
        super(element);
        this.manager = manager;
        this.element = element;
        this.packageName = packageName;

        ModelView modelView = element.getAnnotation(ModelView.class);
        this.query = modelView.query();
        this.databaseName = modelView.databaseName();
        if(databaseName == null || databaseName.isEmpty()) {
            databaseName = DBFlowProcessor.DEFAULT_DB_NAME;
        }

        this.name = modelView.name();
        if(name == null || name.isEmpty()) {
            name = getModelClassName();
        }

        this.modelViewSourceClassName = getModelClassName() + DBFLOW_MODEL_VIEW_TAG;

        createColumnDefinitions((TypeElement) element);

        mCursorWriter = new LoadCursorWriter(this, false);
        mExistenceWriter = new ExistenceWriter(this, false);
    }

    @Override
    protected void createColumnDefinitions(TypeElement typeElement) {
        List<? extends Element> variableElements = manager.getElements().getAllMembers(typeElement);
        for(Element variableElement: variableElements) {
            if(variableElement.getAnnotation(Column.class) != null) {
                ColumnDefinition columnDefinition = new ColumnDefinition(manager, (VariableElement) variableElement);
                columnDefinitions.add(columnDefinition);

                if(columnDefinition.columnType == Column.PRIMARY_KEY || columnDefinition.columnType == Column.FOREIGN_KEY) {
                    manager.getMessager().printMessage(Diagnostic.Kind.ERROR, "ModelViews cannot have primary or foreign keys");
                }
            }
        }
    }

    public String getFullyQualifiedModelClassName() {
        return packageName + "." + getModelClassName();
    }

    public String getFQCN() {
        return packageName + "." + modelViewSourceClassName;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitPackage(packageName);

        javaWriter.emitImports(Classes.CURSOR, Classes.SELECT);

        javaWriter.beginType(modelViewSourceClassName, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), Classes.MODEL_VIEW_ADAPTER);

        writeMethods(javaWriter);

        javaWriter.endType();
    }

    private void writeMethods(JavaWriter javaWriter) throws IOException {
        mCursorWriter.write(javaWriter);
        mExistenceWriter.write(javaWriter);

        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return \"%1s\"", query);
            }
        }, "String" , "getCreationQuery", FlowManagerHandler.METHOD_MODIFIERS);

        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return \"%1s\"", name);
            }
        }, "String" , "getViewName", FlowManagerHandler.METHOD_MODIFIERS);

    }

}
