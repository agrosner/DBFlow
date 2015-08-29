package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.InheritedColumn;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.BindToContentValuesMethod;
import com.raizlabs.android.dbflow.processor.definition.method.BindToStatementMethod;
import com.raizlabs.android.dbflow.processor.definition.method.CreationQueryMethod;
import com.raizlabs.android.dbflow.processor.definition.method.ExistenceMethod;
import com.raizlabs.android.dbflow.processor.definition.method.InsertStatementQueryMethod;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.OneToManyDeleteMethod;
import com.raizlabs.android.dbflow.processor.definition.method.PrimaryConditionClause;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.validator.ColumnValidator;
import com.raizlabs.android.dbflow.processor.validator.OneToManyValidator;
import com.raizlabs.android.dbflow.processor.writer.CreationQueryWriter;
import com.raizlabs.android.dbflow.processor.writer.ExistenceWriter;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.raizlabs.android.dbflow.processor.writer.LoadCursorWriter;
import com.raizlabs.android.dbflow.processor.writer.OneToManySaveMethod;
import com.raizlabs.android.dbflow.processor.writer.SQLiteStatementWriter;
import com.raizlabs.android.dbflow.processor.writer.WhereQueryWriter;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Description: Used in writing ModelAdapters
 */
public class TableDefinition extends BaseTableDefinition {

    public static final String DBFLOW_TABLE_TAG = "Table";

    public static final String DBFLOW_TABLE_ADAPTER = "Adapter";

    public String tableName;

    public String adapterName;

    public String databaseName;

    public String insertConflictActionName;

    public String updateConflicationActionName;

    public List<ColumnDefinition> primaryColumnDefinitions;
    public List<ForeignKeyColumnDefinition> foreignKeyDefinitions;
    public List<UniqueGroupsDefinition> uniqueGroupsDefinitions;


    public ColumnDefinition autoIncrementDefinition;

    public boolean hasAutoIncrement = false;


    public boolean implementsContentValuesListener = false;

    public boolean implementsSqlStatementListener = false;

    public boolean implementsLoadFromCursorListener = false;

    private final MethodDefinition[] methods = new MethodDefinition[]{
            new BindToContentValuesMethod(this, true, false, implementsContentValuesListener),
            new BindToContentValuesMethod(this, false, false, implementsContentValuesListener),
            new BindToStatementMethod(this, true, false),
            new BindToStatementMethod(this, false, false),
            new InsertStatementQueryMethod(this),
            new CreationQueryMethod(this),
            new LoadFromCursorMethod(this),
            new ExistenceMethod(this),
            new PrimaryConditionClause(this),
            new OneToManyDeleteMethod(this, false)
    };

    public boolean hasCachingId = false;

    public boolean allFields = false;

    public Map<String, ColumnDefinition> mColumnMap = Maps.newHashMap();

    public Map<Integer, List<ColumnDefinition>> columnUniqueMap = Maps.newHashMap();

    public List<OneToManyDefinition> oneToManyDefinitions = new ArrayList<>();

    public Map<String, InheritedColumn> inheritedColumnMap = new HashMap<>();

    public TableDefinition(ProcessorManager manager, Element element) {
        super(element, manager);

        Table table = element.getAnnotation(Table.class);
        this.tableName = table.tableName();
        databaseName = table.databaseName();

        databaseMethod = manager.getDatabaseWriter(databaseName);
        if (databaseMethod == null) {
            manager.logError("Databasewriter was null for : " + tableName);
        }

        setOutputClassName(databaseMethod.classSeparator + DBFLOW_TABLE_TAG);
        this.adapterName = getModelClassName() + databaseMethod.classSeparator + DBFLOW_TABLE_ADAPTER;


        // globular default
        ConflictAction insertConflict = table.insertConflict();
        if (insertConflict.equals(ConflictAction.NONE) && !databaseMethod.insertConflict.equals(ConflictAction.NONE)) {
            insertConflict = databaseMethod.insertConflict;
        }

        ConflictAction updateConflict = table.updateConflict();
        if (updateConflict.equals(ConflictAction.NONE) && !databaseMethod.updateConflict.equals(ConflictAction.NONE)) {
            updateConflict = databaseMethod.updateConflict;
        }

        insertConflictActionName = insertConflict.equals(ConflictAction.NONE) ? ""
                : insertConflict.name();
        updateConflicationActionName = updateConflict.equals(ConflictAction.NONE) ? ""
                : updateConflict.name();

        allFields = table.allFields();

        manager.addModelToDatabase(getModelClassName(), databaseName);

        if (tableName == null || tableName.isEmpty()) {
            tableName = element.getSimpleName().toString();
        }
        primaryColumnDefinitions = new ArrayList<>();
        foreignKeyDefinitions = new ArrayList<>();
        uniqueGroupsDefinitions = new ArrayList<>();

        InheritedColumn[] inheritedColumns = table.inheritedColumns();
        for (InheritedColumn inheritedColumn : inheritedColumns) {
            if (inheritedColumnMap.containsKey(inheritedColumn.fieldName())) {
                manager.logError("A duplicate inherited column with name %1s was found for %1s", inheritedColumn.fieldName(), tableName);
            }
            inheritedColumnMap.put(inheritedColumn.fieldName(), inheritedColumn);
        }

        createColumnDefinitions((TypeElement) element);

        UniqueGroup[] groups = table.uniqueColumnGroups();
        Set<Integer> uniqueNumbersSet = new HashSet<>();
        for (UniqueGroup uniqueGroup : groups) {
            if (uniqueNumbersSet.contains(uniqueGroup.groupNumber())) {
                manager.logError("A duplicate unique group with number %1s was found for %1s", uniqueGroup.groupNumber(), tableName);
            }
            UniqueGroupsDefinition definition = new UniqueGroupsDefinition(manager, uniqueGroup);
            for (ColumnDefinition columnDefinition : getColumnDefinitions()) {
                if (columnDefinition.uniqueGroups.contains(definition.number)) {
                    definition.addColumnDefinition(columnDefinition);
                }
            }
            uniqueGroupsDefinitions.add(definition);
            uniqueNumbersSet.add(uniqueGroup.groupNumber());
        }

        implementsLoadFromCursorListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                ClassNames.LOAD_FROM_CURSOR_LISTENER,
                (TypeElement) element);

        implementsContentValuesListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                ClassNames.CONTENT_VALUES_LISTENER, (TypeElement) element);

        implementsSqlStatementListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                ClassNames.SQLITE_STATEMENT_LISTENER, ((TypeElement) element));

        mMethodWriters = new FlowWriter[]{
                new SQLiteStatementWriter(this, false, implementsSqlStatementListener, implementsContentValuesListener),
                new ExistenceWriter(this, false),
                new LoadCursorWriter(this, false, implementsLoadFromCursorListener),
                new WhereQueryWriter(this, false),
                new CreationQueryWriter(manager, this),
                new OneToManyDeleteMethod(this, false),
                new OneToManySaveMethod(this, false)
        };

        // single primary key checking for a long or int valued column
        if (getPrimaryColumnDefinitions().size() == 1) {
            ColumnDefinition columnDefinition = getColumnDefinitions().get(0);
            if (columnDefinition.isPrimaryKey) {
                hasCachingId = !columnDefinition.hasTypeConverter;
            }
        }
    }

    @Override
    public String getTableSourceClassName() {
        return definitionClassName;
    }

    @Override
    protected void createColumnDefinitions(TypeElement typeElement) {
        List<? extends Element> elements = manager.getElements().getAllMembers(typeElement);
        ColumnValidator columnValidator = new ColumnValidator();
        OneToManyValidator oneToManyValidator = new OneToManyValidator();
        for (Element element : elements) {

            // no private static or final fields for all columns, or any inherited columns here.
            boolean isValidColumn = (allFields && (element.getKind().isField() &&
                    !element.getModifiers().contains(Modifier.STATIC) &&
                    !element.getModifiers().contains(Modifier.PRIVATE) &&
                    !element.getModifiers().contains(Modifier.FINAL)));
            inheritedColumnMap.containsKey(element.getSimpleName().toString());
            if (element.getAnnotation(Column.class) != null || isValidColumn) {
                ColumnDefinition columnDefinition;
                if (element.getAnnotation(ForeignKey.class) != null) {
                    columnDefinition = new ForeignKeyColumnDefinition(manager, element);
                } else {
                    columnDefinition = new ColumnDefinition(manager, element);
                }
                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition);
                    mColumnMap.put(columnDefinition.columnName, columnDefinition);
                    if (columnDefinition.isPrimaryKey) {
                        primaryColumnDefinitions.add(columnDefinition);
                    } else if (columnDefinition instanceof ForeignKeyColumnDefinition) {
                        foreignKeyDefinitions.add((ForeignKeyColumnDefinition) columnDefinition);
                    } else if (columnDefinition.isPrimaryKeyAutoIncrement) {
                        autoIncrementDefinition = columnDefinition;
                        hasAutoIncrement = true;
                    }

                    if (!columnDefinition.uniqueGroups.isEmpty()) {
                        List<Integer> groups = columnDefinition.uniqueGroups;
                        for (int group : groups) {
                            List<ColumnDefinition> groupList = columnUniqueMap.get(group);
                            if (groupList == null) {
                                groupList = new ArrayList<>();
                                columnUniqueMap.put(group, groupList);
                            }
                            if (!groupList.contains(columnDefinition)) {
                                groupList.add(columnDefinition);
                            }
                        }
                    }
                }
            } else if (element.getAnnotation(OneToMany.class) != null) {
                OneToManyDefinition oneToManyDefinition = new OneToManyDefinition(element, manager);
                if (oneToManyValidator.validate(manager, oneToManyDefinition)) {
                    oneToManyDefinitions.add(oneToManyDefinition);
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
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columnDefinition.addPropertyDefinition(typeBuilder);
        }

        // TODO: create index groups definition and write it here
    }

    public void writeAdapter(ProcessingEnvironment processingEnvironment) throws IOException {

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(adapterName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassNames.MODEL_ADAPTER, elementClassName));
        InternalAdapterHelper.writeGetModelClass(typeBuilder, elementClassName);
        InternalAdapterHelper.writeGetTableName(typeBuilder, tableName);

        String insertConflictName = insertConflictActionName;
        if (!insertConflictName.isEmpty()) {
            insertConflictName = String.format(" OR %1s ", insertConflictName);
        }
        QueryBuilder stringBuilder = new QueryBuilder("return \"INSERT%1sINTO ")
                .appendQuoted(tableName).appendSpace().append("(");

        List<String> columnNames = new ArrayList<>();
        List<String> bindings = new ArrayList<>();
        for (int i = 0; i < getColumnDefinitions().size(); i++) {
            ColumnDefinition columnDefinition = getColumnDefinitions().get(i);

            if (columnDefinition.isForeignKey) {
                for (ForeignKeyReference reference : columnDefinition.foreignKeyReferences) {
                    columnNames.add(reference.columnName());
                    bindings.add("?");
                }
            } else if (!columnDefinition.isPrimaryKeyAutoIncrement) {
                columnNames.add(columnDefinition.columnName.toUpperCase());
                bindings.add("?");
            }
        }

        stringBuilder.appendQuotedList(columnNames).append(") VALUES (");
        stringBuilder.appendList(bindings).append(")\"");


        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {

                javaWriter.emitStatement(stringBuilder.toString(), insertConflictName);
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
                    javaWriter.emitStatement("return %1s.%1s", ClassNames.CONFLICT_ACTION, updateConflicationActionName);
                }
            }, ClassNames.CONFLICT_ACTION, "getUpdateOnConflictAction", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));
        }

        if (!insertConflictActionName.isEmpty()) {
            WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                @Override
                public void write(JavaWriter javaWriter) throws IOException {
                    javaWriter.emitStatement("return %1s.%1s", ClassNames.CONFLICT_ACTION, insertConflictActionName);
                }
            }, ClassNames.CONFLICT_ACTION, "getInsertOnConflictAction", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));
        }

        javaWriter.endType();
        javaWriter.close();

        JavaFile.Builder javaFileBuilder = JavaFile.builder(packageName, adapterName);

    }
}
