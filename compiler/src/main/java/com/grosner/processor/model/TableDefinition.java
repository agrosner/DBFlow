package com.grosner.processor.model;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class TableDefinition implements FlowWriter {

    private static final String DBFLOW_TABLE_TAG = "$Table";

    private static final String DBFLOW_TABLE_ADAPTER = "$Adapter";

    ProcessingEnvironment processingEnvironment;

    String packageName;

    String tableName;

    String tableSourceClassName;

    String modelClassName;

    String adapterName;

    Element element;

    ArrayList<ColumnDefinition> columnDefinitions;

    ArrayList<ColumnDefinition> primaryColumnDefinitions;

    ContentValuesWriter mContentValuesWriter;

    LoadCursorWriter mLoadCursorWriter;

    WhereQueryWriter mWhereQueryWriter;

    public TableDefinition(ProcessingEnvironment processingEnvironment, String packageName, Element element) {
        this.element = element;
        this.packageName = packageName;
        this.modelClassName = element.getSimpleName().toString();
        this.tableSourceClassName = modelClassName + DBFLOW_TABLE_TAG;
        this.adapterName = modelClassName + DBFLOW_TABLE_ADAPTER;
        this.tableName = element.getAnnotation(Table.class).name();
        this.processingEnvironment = processingEnvironment;
        columnDefinitions = new ArrayList<>();
        primaryColumnDefinitions = new ArrayList<>();
        getColumnDefinitions(element);

        mContentValuesWriter = new ContentValuesWriter(this);
        mWhereQueryWriter = new WhereQueryWriter(this);
        mLoadCursorWriter = new LoadCursorWriter(this);
    }

    private void getColumnDefinitions(Element element) {
        List<VariableElement> variableElements = ElementFilter.fieldsIn(element.getEnclosedElements());
        for(VariableElement variableElement: variableElements) {
            if(variableElement.getAnnotation(Column.class) != null) {
                ColumnDefinition columnDefinition = new ColumnDefinition(processingEnvironment, variableElement);
                columnDefinitions.add(columnDefinition);

                if(columnDefinition.columnType == Column.PRIMARY_KEY) {
                    primaryColumnDefinitions.add(columnDefinition);
                }
            }
        }
    }

    @Override
    public String getFQCN() {
        return packageName+"."+ tableSourceClassName;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitPackage(packageName);
        javaWriter.beginType(tableSourceClassName, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));
        javaWriter.emitEmptyLine();
        javaWriter.emitField("String", "TABLE_NAME", Sets.newHashSet(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL), "\"" + tableName + "\"");
        javaWriter.emitEmptyLine();
        for(ColumnDefinition columnDefinition: columnDefinitions) {
            columnDefinition.write(javaWriter);
        }
        javaWriter.endType();
    }

    public void writeAdapter(ProcessingEnvironment processingEnvironment) throws IOException {
        JavaWriter javaWriter = new JavaWriter(processingEnvironment.getFiler().createSourceFile(adapterName).openWriter());

        javaWriter.emitPackage(packageName);
        javaWriter.emitImports("com.grosner.dbflow.structure.ModelAdapter",
                "android.database.Cursor",
                "android.content.ContentValues",
                "com.grosner.dbflow.sql.SqlUtils");
        javaWriter.beginType(adapterName, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), null, "ModelAdapter<" + element.getSimpleName()  + ">");

        mContentValuesWriter.write(javaWriter);
        mLoadCursorWriter.write(javaWriter);
        mWhereQueryWriter.write(javaWriter);

        javaWriter.endType();
        javaWriter.close();
    }
}
