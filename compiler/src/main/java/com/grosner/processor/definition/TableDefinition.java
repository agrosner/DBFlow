package com.grosner.processor.definition;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;
import com.grosner.processor.Classes;
import com.grosner.processor.DBFlowProcessor;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.utils.WriterUtils;
import com.grosner.processor.writer.*;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class TableDefinition extends BaseTableDefinition implements FlowWriter {

    public static final String DBFLOW_TABLE_TAG = "$Table";

    public static final String DBFLOW_TABLE_ADAPTER = "$Adapter";

    public String packageName;

    public String tableName;

    public String tableSourceClassName;

    public String adapterName;

    public Element element;

    public String databaseName;

    public ArrayList<ColumnDefinition> primaryColumnDefinitions;

    public ArrayList<ColumnDefinition> foreignKeyDefinitions;

    FlowWriter[] mMethodWriters;

    public TableDefinition(ProcessorManager manager, String packageName, Element element) {
        super(element);
        this.element = element;
        this.packageName = packageName;
        this.tableSourceClassName = getModelClassName() + DBFLOW_TABLE_TAG;
        this.adapterName = getModelClassName() + DBFLOW_TABLE_ADAPTER;

        Table table = element.getAnnotation(Table.class);
        this.tableName = table.value();
        databaseName = table.databaseName();
        if (databaseName == null || databaseName.isEmpty()) {
            databaseName = DBFlowProcessor.DEFAULT_DB_NAME;
        }

        manager.addModelToDatabase(getModelClassName(), databaseName);

        if (tableName == null || tableName.isEmpty()) {
            tableName = element.getSimpleName().toString();
        }
        this.manager = manager;

        primaryColumnDefinitions = new ArrayList<>();
        foreignKeyDefinitions = new ArrayList<>();

        createColumnDefinitions((TypeElement) element);

        mMethodWriters = new FlowWriter[]{
                new ContentValuesWriter(this, false),
                new ExistenceWriter(this, false),
                new LoadCursorWriter(this, false),
                new WhereQueryWriter(this, false),
                new CreationQueryWriter(manager, this),
                new DeleteWriter(this, false)
        };

    }

    @Override
    protected void createColumnDefinitions(TypeElement element) {
        List<? extends Element> variableElements = manager.getElements().getAllMembers(element);
        for (Element variableElement : variableElements) {
            if (variableElement.getAnnotation(Column.class) != null) {
                ColumnDefinition columnDefinition = new ColumnDefinition(manager, (VariableElement) variableElement);
                columnDefinitions.add(columnDefinition);

                if (columnDefinition.columnType == Column.PRIMARY_KEY) {
                    primaryColumnDefinitions.add(columnDefinition);
                } else if (columnDefinition.columnType == Column.FOREIGN_KEY) {
                    foreignKeyDefinitions.add(columnDefinition);
                }
            }
        }
    }

    @Override
    public List<ColumnDefinition> getPrimaryColumnDefinitions() {
        return primaryColumnDefinitions;
    }

    @Override
    public String getTableSourceClassName() {
        return tableSourceClassName;
    }

    public String getQualifiedAdapterClassName() {
        return packageName + "." + adapterName;
    }

    public String getFQCN() {
        return packageName + "." + tableSourceClassName;
    }

    public String getQualifiedModelClassName() {
        return packageName + "." + getModelClassName();
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitPackage(packageName);
        javaWriter.beginType(tableSourceClassName, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));
        javaWriter.emitEmptyLine();
        javaWriter.emitField("String", "TABLE_NAME", Sets.newHashSet(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL), "\"" + tableName + "\"");
        javaWriter.emitEmptyLine();
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columnDefinition.write(javaWriter);
        }
        javaWriter.endType();
    }

    public void writeAdapter(ProcessingEnvironment processingEnvironment) throws IOException {
        JavaWriter javaWriter = new JavaWriter(processingEnvironment.getFiler().createSourceFile(packageName + "." + adapterName).openWriter());

        javaWriter.emitPackage(packageName);
        javaWriter.emitImports(Classes.MODEL_ADAPTER,
                Classes.FLOW_MANAGER,
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
        javaWriter.emitSingleLineComment("This table belongs to the %1s database", databaseName);
        javaWriter.beginType(adapterName, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), "ModelAdapter<" + element.getSimpleName() + ">");
        InternalAdapterHelper.writeGetModelClass(javaWriter, getModelClassName());
        InternalAdapterHelper.writeGetTableName(javaWriter, tableSourceClassName);

        for (FlowWriter writer : mMethodWriters) {
            writer.write(javaWriter);
        }

        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return new %1s()", getQualifiedModelClassName());
            }
        }, getQualifiedModelClassName(), "newInstance", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));

        javaWriter.endType();
        javaWriter.close();
    }
}
