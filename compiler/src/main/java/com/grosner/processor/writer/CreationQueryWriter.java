package com.grosner.processor.writer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.dbflow.sql.SQLiteType;
import com.grosner.processor.Classes;
import com.grosner.processor.ProcessorUtils;
import com.grosner.processor.definition.ColumnDefinition;
import com.grosner.processor.definition.PrimaryKeyNotFoundException;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.definition.TypeConverterDefinition;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.model.ReflectionUtils;
import com.grosner.processor.model.builder.TableCreationQueryBuilder;
import com.grosner.processor.utils.ModelUtils;
import com.grosner.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class CreationQueryWriter implements FlowWriter{

    private final TableDefinition tableDefinition;
    private final ProcessorManager manager;

    public CreationQueryWriter(ProcessorManager manager, TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
        this.manager = manager;
    }


    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                TableCreationQueryBuilder tableCreationQuery = new TableCreationQueryBuilder();
                tableCreationQuery.appendCreateTableIfNotExists(tableDefinition.tableName);

                ArrayList<QueryBuilder> mColumnDefinitions = new ArrayList<QueryBuilder>();
                List<String> foreignColumnClasses = Lists.newArrayList();
                for(ColumnDefinition columnDefinition: tableDefinition.getColumnDefinitions()) {

                    TableCreationQueryBuilder queryBuilder = new TableCreationQueryBuilder();
                    if(columnDefinition.columnType == Column.FOREIGN_KEY) {
                        queryBuilder.appendSpace().appendForeignKeys(columnDefinition.foreignKeyReferences);
                    } else {

                        queryBuilder.append(columnDefinition.columnName)
                                .appendSpace();

                        if (columnDefinition.hasTypeConverter) {
                            TypeConverterDefinition typeConverterDefinition = manager.getTypeConverterDefinition(columnDefinition.modelType);
                            queryBuilder.appendType(typeConverterDefinition.getDbElement().asType().toString());
                        } else if (SQLiteType.containsClass(columnDefinition.columnFieldType)) {
                            queryBuilder.appendType(columnDefinition.columnFieldType);
                        } else if (ReflectionUtils.isSubclassOf(columnDefinition.columnFieldType, Enum.class)) {
                            queryBuilder.appendSQLiteType(SQLiteType.TEXT);
                        }
                    }

                    mColumnDefinitions.add(queryBuilder.appendColumn(columnDefinition.column));
                }

                boolean isModelView = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                        tableDefinition.packageName +"." + tableDefinition.getModelClassName(),
                        manager.getElements().getTypeElement(Classes.MODEL_VIEW));

                // Views do not have primary keys
                if(!isModelView) {
                    if(tableDefinition.primaryColumnDefinitions.isEmpty()) {
                        throw new PrimaryKeyNotFoundException("Table: " + tableDefinition.tableName + " must define a primary key");
                    }

                    QueryBuilder primaryKeyQueryBuilder = new QueryBuilder().append("PRIMARY KEY(");
                    int count = 0;
                    int index = 0;
                    for (ColumnDefinition field : tableDefinition.primaryColumnDefinitions) {
                        if (field.columnType == Column.PRIMARY_KEY) {
                            count++;
                            primaryKeyQueryBuilder.append(field.columnName);
                            if (index < tableDefinition.primaryColumnDefinitions.size() - 1) {
                                primaryKeyQueryBuilder.append(", ");
                            }
                        }
                        index++;
                    }

                    if (count > 0) {
                        primaryKeyQueryBuilder.append(")");
                        mColumnDefinitions.add(primaryKeyQueryBuilder);
                    }

                    QueryBuilder foreignKeyQueryBuilder;
                    for (ColumnDefinition foreignKeyField : tableDefinition.foreignKeyDefinitions) {
                        foreignKeyQueryBuilder = new QueryBuilder().append("FOREIGN KEY(");

                        String[] foreignColumns = new String[foreignKeyField.foreignKeyReferences.length];
                        for (int i = 0; i < foreignColumns.length; i++) {
                            foreignColumns[i] = foreignKeyField.foreignKeyReferences[i].foreignColumnName();
                        }

                        String[] columns = new String[foreignKeyField.foreignKeyReferences.length];
                        for (int i = 0; i < columns.length; i++) {
                            columns[i] = foreignKeyField.foreignKeyReferences[i].columnName();
                        }

                        foreignKeyQueryBuilder.appendArray(columns)
                                .append(")").appendSpaceSeparated("REFERENCES %1s");

                        foreignColumnClasses.add("FlowManager.getTableName(" + ModelUtils.getFieldClass(foreignKeyField.columnFieldType) + ")");

                        mColumnDefinitions.add(foreignKeyQueryBuilder);
                    }

                } else if (!tableDefinition.primaryColumnDefinitions.isEmpty() || !tableDefinition.foreignKeyDefinitions.isEmpty()) {
                    // We do not crash here as to interfere with instantiation. We will display log in error
                    manager.getMessager().printMessage(Diagnostic.Kind.ERROR, "MODEL VIEWS CANNOT HAVE PRIMARY KEYS OR FOREIGN KEYS");
                }

                tableCreationQuery.appendList(mColumnDefinitions).append("););");
                QueryBuilder returnQuery = new QueryBuilder();
                returnQuery.append("return ");
                if(!foreignColumnClasses.isEmpty()) {
                    returnQuery.append("String.format(");
                }
                returnQuery.append("\"%1s\"");
                if(!foreignColumnClasses.isEmpty()) {
                    returnQuery.append(",");
                    returnQuery.appendList(foreignColumnClasses).append(")");
                }
                javaWriter.emitStatement(returnQuery.getQuery(), tableCreationQuery.getQuery());
            }
        }, "String", "getCreationQuery", Sets.newHashSet(Modifier.PUBLIC));
    }
}
