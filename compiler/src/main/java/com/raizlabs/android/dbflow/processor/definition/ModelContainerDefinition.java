package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.writer.*;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;

/**
 * Description: Used in writing model container adapters
 */
public class ModelContainerDefinition extends BaseDefinition {

    public static final String DBFLOW_MODEL_CONTAINER_TAG = "Container";

    private FlowWriter[] mMethodWriters;

    private TableDefinition tableDefinition;

    public ModelContainerDefinition(TypeElement classElement, ProcessorManager manager) {
        super(classElement, manager);
        tableDefinition = manager.getTableDefinition(manager.getDatabase(classElement.getSimpleName().toString()), classElement);

        setDefinitionClassName(tableDefinition.databaseWriter.classSeparator + DBFLOW_MODEL_CONTAINER_TAG);

        mMethodWriters = new FlowWriter[]{
                new SQLiteStatementWriter(tableDefinition, true, tableDefinition.implementsSqlStatementListener,
                        tableDefinition.implementsContentValuesListener),
                new ExistenceWriter(tableDefinition, true),
                new WhereQueryWriter(tableDefinition, true),
                new ToModelWriter(tableDefinition, true),
                new LoadCursorWriter(tableDefinition, true, tableDefinition.implementsLoadFromCursorListener),
        };
    }

    @Override
    protected String[] getImports() {
        return new String[] {
                Classes.HASH_MAP,
                Classes.MAP,
                Classes.FLOW_MANAGER,
                Classes.CONDITION_QUERY_BUILDER,
                Classes.MODEL_CONTAINER,
                Classes.CONTAINER_ADAPTER,
                Classes.MODEL,
                Classes.CONTENT_VALUES,
                Classes.CURSOR,
                Classes.SQL_UTILS,
                Classes.SELECT,
                Classes.CONDITION
        };
    }

    @Override
    protected String getExtendsClass() {
        return "ContainerAdapter<" + elementClassName + ">";
    }

    @Override
    public void onWriteDefinition(JavaWriter javaWriter) throws IOException {

        javaWriter.emitField("Map<String, Class<?>>", "mColumnMap", Sets.newHashSet(Modifier.PRIVATE, Modifier.FINAL), "new HashMap<>()");
        javaWriter.emitEmptyLine();

        javaWriter.beginConstructor(Sets.newHashSet(Modifier.PUBLIC));

        for(ColumnDefinition columnDefinition: tableDefinition.columnDefinitions) {
            javaWriter.emitStatement("%1s.put(\"%1s\", %1s.class)", "mColumnMap", columnDefinition.columnName, columnDefinition.columnFieldType);
        }

        javaWriter.endConstructor();

        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s.get(%1s)", "mColumnMap", "columnName");
            }
        }, "Class<?>", "getClassForColumn", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), "String", "columnName");

        InternalAdapterHelper.writeGetModelClass(javaWriter, getModelClassQualifiedName());
        InternalAdapterHelper.writeGetTableName(javaWriter, elementClassName + tableDefinition.databaseWriter.classSeparator + TableDefinition.DBFLOW_TABLE_TAG);



        for (FlowWriter writer : mMethodWriters) {
            writer.write(javaWriter);
        }
    }

    public String getModelClassQualifiedName() {
        return ((TypeElement)element).getQualifiedName().toString();
    }
}
