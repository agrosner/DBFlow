package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.DBFlowProcessor;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.validator.ColumnValidator;
import com.raizlabs.android.dbflow.processor.writer.CreationQueryWriter;
import com.raizlabs.android.dbflow.processor.writer.DeleteWriter;
import com.raizlabs.android.dbflow.processor.writer.ExistenceWriter;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.raizlabs.android.dbflow.processor.writer.LoadCursorWriter;
import com.raizlabs.android.dbflow.processor.writer.SQLiteStatementWriter;
import com.raizlabs.android.dbflow.processor.writer.WhereQueryWriter;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Description: Used in writing ModelAdapters
 */
public class TableDefinition extends BaseTableDefinition implements FlowWriter {

    public static final String DBFLOW_TABLE_TAG = "$Table";

    public static final String DBFLOW_TABLE_ADAPTER = "$Adapter";

    public String tableName;

    public String adapterName;

    public String databaseName;

    public String insertConflictActionName;

    public String updateConflicationActionName;

    public ArrayList<ColumnDefinition> primaryColumnDefinitions;

    public ColumnDefinition autoIncrementDefinition;

    public ArrayList<ColumnDefinition> foreignKeyDefinitions;

    public boolean implementsContentValuesListener = false;

    public boolean implementsSqlStatementListener = false;

    public boolean implementsLoadFromCursorListener = false;

    FlowWriter[] mMethodWriters;

    public TableDefinition(ProcessorManager manager, Element element) {
        super(element, manager);
        setDefinitionClassName(DBFLOW_TABLE_TAG);
        this.adapterName = getModelClassName() + DBFLOW_TABLE_ADAPTER;

        Table table = element.getAnnotation(Table.class);
        this.tableName = table.value();
        databaseName = table.databaseName();
        if (databaseName == null || databaseName.isEmpty()) {
            databaseName = DBFlowProcessor.DEFAULT_DB_NAME;
        }
        insertConflictActionName = table.insertConflict().equals(ConflictAction.NONE) ? ""
                : table.insertConflict().name();
        updateConflicationActionName = table.updateConflict().equals(ConflictAction.NONE) ? ""
                : table.insertConflict().name();

        manager.addModelToDatabase(getModelClassName(), databaseName);

        if (tableName == null || tableName.isEmpty()) {
            tableName = element.getSimpleName().toString();
        }
        primaryColumnDefinitions = new ArrayList<>();
        foreignKeyDefinitions = new ArrayList<>();

        createColumnDefinitions((TypeElement) element);

        implementsLoadFromCursorListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                Classes.LOAD_FROM_CURSOR_LISTENER, (TypeElement) element);

        implementsContentValuesListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                Classes.CONTENT_VALUES_LISTENER, (TypeElement) element);

        implementsSqlStatementListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                Classes.SQLITE_STATEMENT_LISTENER, ((TypeElement) element));

        mMethodWriters = new FlowWriter[]{
                new SQLiteStatementWriter(this, false, implementsSqlStatementListener, implementsContentValuesListener),
                new ExistenceWriter(this, false),
                new LoadCursorWriter(this, false, implementsLoadFromCursorListener),
                new WhereQueryWriter(this, false),
                new CreationQueryWriter(manager, this),
                new DeleteWriter(this, false)
        };


    }

    @Override
    public String getTableSourceClassName() {
        return definitionClassName;
    }

    @Override
    protected void createColumnDefinitions(TypeElement element) {
        List<? extends Element> variableElements = manager.getElements().getAllMembers(element);
        ColumnValidator columnValidator = new ColumnValidator();
        for (Element variableElement : variableElements) {
            if (variableElement.getAnnotation(Column.class) != null) {
                ColumnDefinition columnDefinition = new ColumnDefinition(manager, (VariableElement) variableElement);
                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition);
                    if (columnDefinition.columnType == Column.PRIMARY_KEY) {
                        primaryColumnDefinitions.add(columnDefinition);
                    } else if (columnDefinition.columnType == Column.FOREIGN_KEY) {
                        foreignKeyDefinitions.add(columnDefinition);
                    } else if (columnDefinition.columnType == Column.PRIMARY_KEY_AUTO_INCREMENT) {
                        autoIncrementDefinition = columnDefinition;
                    }
                }
            }
        }
    }

    public ColumnDefinition getAutoIncrementPrimaryKey() {
        return autoIncrementDefinition;
    }

    @Override
    public List<ColumnDefinition> getPrimaryColumnDefinitions() {
        return primaryColumnDefinitions;
    }

    public String getQualifiedAdapterClassName() {
        return packageName + "." + adapterName;
    }

    public String getQualifiedModelClassName() {
        return packageName + "." + getModelClassName();
    }

    @Override
    public void onWriteDefinition(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitField("String", "TABLE_NAME", Sets.newHashSet(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL), "\"" + tableName + "\"");
        javaWriter.emitEmptyLine();
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columnDefinition.write(javaWriter);
        }
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
                Classes.CONDITION
        );
        javaWriter.emitSingleLineComment("This table belongs to the %1s database", databaseName);
        javaWriter.beginType(adapterName, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), "ModelAdapter<" + element.getSimpleName() + ">");
        InternalAdapterHelper.writeGetModelClass(javaWriter, getModelClassName());
        InternalAdapterHelper.writeGetTableName(javaWriter, getSourceFileName());

        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                String insertConflictName = insertConflictActionName;
                if (!insertConflictName.isEmpty()) {
                    insertConflictName = String.format(" OR %1s ", insertConflictName);
                }
                QueryBuilder stringBuilder = new QueryBuilder("return \"INSERT%1sINTO %1s (");

                List<String> columnNames = new ArrayList<String>();
                List<String> bindings = new ArrayList<String>();
                for (int i = 0; i < getColumnDefinitions().size(); i++) {
                    ColumnDefinition columnDefinition = getColumnDefinitions().get(i);

                    if (columnDefinition.columnType == Column.FOREIGN_KEY) {
                        for (ForeignKeyReference reference : columnDefinition.foreignKeyReferences) {
                            columnNames.add(QueryBuilder.quote(reference.columnName()));
                            bindings.add("?");
                        }
                    } else if (columnDefinition.columnType != Column.PRIMARY_KEY_AUTO_INCREMENT) {
                        columnNames.add(QueryBuilder.quote(columnDefinition.columnName.toUpperCase()));
                        bindings.add("?");
                    }
                }

                stringBuilder.appendList(columnNames).append(") VALUES (");
                stringBuilder.appendList(bindings).append(")\"");
                javaWriter.emitStatement(stringBuilder.toString(), insertConflictName, QueryBuilder.quote(tableName));
            }
        }, "String", "getInsertStatementQuery", Sets.newHashSet(Modifier.PROTECTED, Modifier.FINAL));

        for (FlowWriter writer : mMethodWriters) {
            writer.write(javaWriter);
        }

        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return new %1s()", getQualifiedModelClassName());
            }
        }, getQualifiedModelClassName(), "newInstance", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));

        if (!updateConflicationActionName.isEmpty()) {
            WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                @Override
                public void write(JavaWriter javaWriter) throws IOException {
                    javaWriter.emitStatement("return %1s.%1s", Classes.CONFLICT_ACTION, updateConflicationActionName);
                }
            }, Classes.CONFLICT_ACTION, "getUpdateOnConflictAction", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));
        }

        if (!insertConflictActionName.isEmpty()) {
            WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                @Override
                public void write(JavaWriter javaWriter) throws IOException {
                    javaWriter.emitStatement("return %1s.%1s", Classes.CONFLICT_ACTION, insertConflictActionName);
                }
            }, Classes.CONFLICT_ACTION, "getInsertOnConflictAction", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));
        }

        javaWriter.endType();
        javaWriter.close();
    }
}
