package com.grosner.processor.definition;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;
import com.grosner.processor.Classes;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.utils.ModelUtils;
import com.grosner.processor.utils.WriterUtils;
import com.grosner.processor.writer.*;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
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

    public static final String DBFLOW_TABLE_TAG = "$Table";

    public static final String DBFLOW_TABLE_ADAPTER = "$Adapter";

    public ProcessorManager manager;

    public String packageName;

    public String tableName;

    public String tableSourceClassName;

    public String modelClassName;

    public String adapterName;

    public Element element;

    public ArrayList<ColumnDefinition> columnDefinitions;

    public ArrayList<ColumnDefinition> primaryColumnDefinitions;

    public ArrayList<ColumnDefinition> foreignKeyDefinitions;

    ContentValuesWriter mContentValuesWriter;

    ExistenceWriter mExistenceWriter;

    LoadCursorWriter mLoadCursorWriter;

    WhereQueryWriter mWhereQueryWriter;

    CreationQueryWriter mCreationQueryWriter;

    DeleteWriter mDeleteWriter;

    public TableDefinition(ProcessorManager manager, String packageName, Element element) {
        this.element = element;
        this.packageName = packageName;
        this.modelClassName = element.getSimpleName().toString();
        this.tableSourceClassName = modelClassName + DBFLOW_TABLE_TAG;
        this.adapterName = modelClassName + DBFLOW_TABLE_ADAPTER;
        this.tableName = element.getAnnotation(Table.class).name();
        this.manager = manager;
        columnDefinitions = new ArrayList<>();
        primaryColumnDefinitions = new ArrayList<>();
        foreignKeyDefinitions = new ArrayList<>();
        getColumnDefinitions((TypeElement) element);

        mContentValuesWriter = new ContentValuesWriter(this, false);
        mWhereQueryWriter = new WhereQueryWriter(this, false);
        mLoadCursorWriter = new LoadCursorWriter(this, false);
        mExistenceWriter = new ExistenceWriter(this, false);
        mCreationQueryWriter = new CreationQueryWriter(manager, this);
        mDeleteWriter = new DeleteWriter(this, false);
    }

    private void getColumnDefinitions(TypeElement element) {
        List<? extends Element> variableElements = manager.getElements().getAllMembers(element);
        for(Element variableElement: variableElements) {
            if(variableElement.getAnnotation(Column.class) != null) {
                ColumnDefinition columnDefinition = new ColumnDefinition(manager, (VariableElement) variableElement);
                columnDefinitions.add(columnDefinition);

                if(columnDefinition.columnType == Column.PRIMARY_KEY) {
                    primaryColumnDefinitions.add(columnDefinition);
                } else if(columnDefinition.columnType == Column.FOREIGN_KEY) {
                    foreignKeyDefinitions.add(columnDefinition);
                }
            }
        }
    }

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
        javaWriter.emitImports(Classes.MODEL_ADAPTER,
                Classes.CONDITION_QUERY_BUILDER,
                Classes.CURSOR,
                Classes.CONTENT_VALUES,
                Classes.SQL_UTILS,
                Classes.SELECT,
                Classes.CONDITION,
                Classes.DELETE,
                Classes.TRANSACTION_MANAGER,
                Classes.PROCESS_MODEL_INFO,
                Classes.DBTRANSACTION_INFO
        );
        javaWriter.beginType(adapterName, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), "ModelAdapter<" + element.getSimpleName() + ">");
        InternalAdapterHelper.writeGetModelClass(javaWriter, modelClassName);
        InternalAdapterHelper.writeGetTableName(javaWriter, tableSourceClassName);

        mContentValuesWriter.write(javaWriter);
        mExistenceWriter.write(javaWriter);
        mLoadCursorWriter.write(javaWriter);
        mWhereQueryWriter.write(javaWriter);
        mCreationQueryWriter.write(javaWriter);
        mDeleteWriter.write(javaWriter);

        javaWriter.endType();
        javaWriter.close();
    }
}
