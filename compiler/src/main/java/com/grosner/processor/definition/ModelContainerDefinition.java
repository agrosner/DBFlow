package com.grosner.processor.definition;

import com.google.common.collect.Sets;
import com.grosner.processor.Classes;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.utils.WriterUtils;
import com.grosner.processor.writer.*;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelContainerDefinition implements FlowWriter {

    public static final String DBFLOW_MODEL_CONTAINER_TAG = "$Container";

    public final TypeElement classElement;

    private final ProcessorManager manager;

    private String sourceFileName;

    private String packageName;

    private FlowWriter[] mMethodWriters;

    private TableDefinition tableDefinition;

    public ModelContainerDefinition(String packageName, TypeElement classElement, ProcessorManager manager) {
        this.classElement = classElement;
        this.manager = manager;
        this.sourceFileName = classElement.getSimpleName().toString() + DBFLOW_MODEL_CONTAINER_TAG;
        this.packageName = packageName;

        tableDefinition = manager.getTableDefinition(manager.getDatabase(classElement.getSimpleName().toString()), classElement);

        mMethodWriters = new FlowWriter[]{
                new ContentValuesWriter(tableDefinition, true),
                new ExistenceWriter(tableDefinition, true),
                new WhereQueryWriter(tableDefinition, true),
                new ToModelWriter(tableDefinition),
                new LoadCursorWriter(tableDefinition, true),
                new DeleteWriter(tableDefinition, true)
        };
    }


    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitPackage(packageName);
        javaWriter.emitImports(
                Classes.HASH_MAP,
                Classes.MAP,
                Classes.FLOW_MANAGER,
                Classes.CONDITION_QUERY_BUILDER,
                Classes.MODEL_CONTAINER,
                Classes.MODEL_CONTAINER_UTILS,
                Classes.CONTAINER_ADAPTER,
                Classes.MODEL,
                Classes.CONTENT_VALUES,
                Classes.CURSOR,
                Classes.SQL_UTILS,
                Classes.SELECT,
                Classes.CONDITION
        );
        javaWriter.beginType(sourceFileName, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), "ContainerAdapter<" + classElement.getSimpleName() + ">");

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
        InternalAdapterHelper.writeGetTableName(javaWriter, classElement.getSimpleName().toString() + TableDefinition.DBFLOW_TABLE_TAG);



        for (FlowWriter writer : mMethodWriters) {
            writer.write(javaWriter);
        }

        javaWriter.endType();
        javaWriter.close();
    }

    public String getModelClassQualifiedName() {
        return classElement.getQualifiedName().toString();
    }

    public String getFQCN() {
        return packageName + "." + sourceFileName;
    }

}
