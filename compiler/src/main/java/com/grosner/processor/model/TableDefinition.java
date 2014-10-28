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

    private static final String DBFLOW_TABLE_TAG = "$Columns";

    private static final String DBFLOW_TABLE_ADAPTER = "$Adapter";

    String packageName;

    String tableName;

    String className;

    String adapterName;

    Element element;

    private ArrayList<ColumnDefinition> columnDefinitions;

    ContentValuesWriter mContentValuesWriter;

    public TableDefinition(String packageName, Element element) {
        this.element = element;
        this.packageName = packageName;
        this.className = element.getSimpleName() + DBFLOW_TABLE_TAG;
        this.adapterName = element.getSimpleName() + DBFLOW_TABLE_ADAPTER;
        this.tableName = element.getAnnotation(Table.class).name();
        columnDefinitions = getColumnDefinitions(element);

        mContentValuesWriter = new ContentValuesWriter(tableName, element.getSimpleName().toString(), columnDefinitions);
    }

    private static ArrayList<ColumnDefinition> getColumnDefinitions(Element element) {
        List<VariableElement> variableElements = ElementFilter.fieldsIn(element.getEnclosedElements());
        ArrayList<ColumnDefinition> columns = new ArrayList<>();
        for(VariableElement variableElement: variableElements) {
            if(variableElement.getAnnotation(Column.class) != null) {
                columns.add(new ColumnDefinition(variableElement));
            }
        }
        return columns;
    }

    @Override
    public String getFQCN() {
        return packageName+"."+className;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitPackage(packageName);
        javaWriter.beginType(className, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));
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

        javaWriter.endType();
        javaWriter.close();
    }
}
