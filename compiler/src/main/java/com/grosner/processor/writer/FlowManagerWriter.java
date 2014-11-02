package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Database;
import com.grosner.processor.Classes;
import com.grosner.processor.definition.ModelContainerDefinition;
import com.grosner.processor.definition.ModelViewDefinition;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.handler.FlowManagerHandler;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.utils.ModelUtils;
import com.grosner.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class FlowManagerWriter implements FlowWriter {

    private final ProcessorManager manager;

    public Element element;

    public String packageName;

    public String databaseName;

    public int databaseVersion;

    boolean foreignKeysSupported;

    public FlowManagerWriter(ProcessorManager manager, String packageName, Element element) {
        this.manager = manager;
        this.element = element;
        this.packageName = Classes.FLOW_MANAGER_PACKAGE;

        Database database = element.getAnnotation(Database.class);
        databaseName = database.name();
        if(databaseName == null || databaseName.isEmpty()) {
            databaseName = element.getSimpleName().toString();
        }

        databaseVersion = database.version();
        foreignKeysSupported = database.foreignKeysSupported();

        manager.addFlowManagerWriter(this);
    }

    public String getFQCN() {
        return packageName + "." + databaseName +"$Database";
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitPackage(packageName);

        writeImports(javaWriter);
        javaWriter.beginType(databaseName + "$Database", "class", FlowManagerHandler.METHOD_MODIFIERS, Classes.FLOW_MANAGER_INTERFACE);
        javaWriter.emitEmptyLine();

        writeFields(javaWriter);
        writeInitialization(javaWriter);
        writeConstructor(javaWriter);
        writeGetters(javaWriter);

        javaWriter.endType();
    }

    private void writeImports(JavaWriter javaWriter) throws IOException {
        Set<String> imports = Sets.newHashSet(Classes.MODEL_ADAPTER,
                Classes.BASE_FLOW_MANAGER,
                Classes.FLOW_MANAGER,
                Classes.MODEL_VIEW,
                Classes.MODEL_VIEW_ADAPTER,
                Classes.MODEL, Classes.CONTAINER_ADAPTER,
                Classes.TYPE_CONVERTER, Classes.MAP,
                Classes.HASH_MAP, Classes.LIST,
                Classes.ARRAY_LIST);

        javaWriter.emitImports(imports);
    }

    private void writeInitialization(JavaWriter javaWriter) throws IOException {
        javaWriter.beginInitializer(false);

        for(TableDefinition tableDefinition: manager.getTableDefinitions(databaseName)) {
            javaWriter.emitStatement(FlowManagerHandler.MODEL_FIELD_NAME +".add(%1s)", ModelUtils.getFieldClass(tableDefinition.getQualifiedModelClassName()));
            javaWriter.emitStatement(FlowManagerHandler.MODEL_ADAPTER_MAP_FIELD_NAME +".put(%1s, new %1s())", ModelUtils.getFieldClass(tableDefinition.getQualifiedModelClassName()),
                    tableDefinition.getQualifiedAdapterClassName());
        }

        for(ModelContainerDefinition modelContainerDefinition: manager.getModelContainers(databaseName)) {
            javaWriter.emitStatement(FlowManagerHandler.MODEL_CONTAINER_ADAPTER_MAP_FIELD_NAME + ".put(%1s, new %1s())", ModelUtils.getFieldClass(modelContainerDefinition.getModelClassQualifiedName()),
                    modelContainerDefinition.getFQCN());
        }

        for (ModelViewDefinition modelViewDefinition: manager.getModelViewDefinitions(databaseName)) {
            javaWriter.emitStatement(FlowManagerHandler.MODEL_VIEW_FIELD_NAME +".add(%1s)", ModelUtils.getFieldClass(modelViewDefinition.getFullyQualifiedModelClassName()));
            javaWriter.emitStatement(FlowManagerHandler.MODEL_VIEW_ADAPTER_MAP_FIELD_NAME + ".put(%1s, new %1s())",
                    ModelUtils.getFieldClass(modelViewDefinition.getFullyQualifiedModelClassName()), modelViewDefinition.getFQCN());
        }

        javaWriter.endInitializer();
    }

    private void writeConstructor(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();

        javaWriter.beginConstructor(Sets.newHashSet(Modifier.PUBLIC), "FlowManagerHolder", "holder");
        // Register this manager with classes if multitable is enabled.
        // Need to figure out how to
        for (TableDefinition tableDefinition: manager.getTableDefinitions(databaseName)) {
            javaWriter.emitStatement("holder.putFlowManagerForTable(%1s, this)", ModelUtils.getFieldClass(tableDefinition.getQualifiedModelClassName()));
        }

        for(ModelViewDefinition modelViewDefinition: manager.getModelViewDefinitions(databaseName)) {
            javaWriter.emitStatement("holder.putFlowManagerForTable(%1s, this)", ModelUtils.getFieldClass(modelViewDefinition.getFullyQualifiedModelClassName()));
        }

        javaWriter.endConstructor();
    }

    private void writeFields(JavaWriter javaWriter) throws IOException {
        // Model classes
        javaWriter.emitField("List<Class<? extends Model>>", FlowManagerHandler.MODEL_FIELD_NAME,
                FlowManagerHandler.FIELD_MODIFIERS, "new ArrayList<>()");
        javaWriter.emitEmptyLine();

        // Model Adapters
        javaWriter.emitField("Map<Class<? extends Model>, ModelAdapter>", FlowManagerHandler.MODEL_ADAPTER_MAP_FIELD_NAME,
                FlowManagerHandler.FIELD_MODIFIERS, "new HashMap<>()");
        javaWriter.emitEmptyLine();

        // Model Container Adapters
        javaWriter.emitField("Map<Class<? extends Model>, ContainerAdapter>", FlowManagerHandler.MODEL_CONTAINER_ADAPTER_MAP_FIELD_NAME,
                FlowManagerHandler.FIELD_MODIFIERS, "new HashMap<>()");
        javaWriter.emitEmptyLine();


        // model views
        javaWriter.emitField("List<Class<? extends BaseModelView>>", FlowManagerHandler.MODEL_VIEW_FIELD_NAME,
                FlowManagerHandler.FIELD_MODIFIERS, "new ArrayList<>()");
        javaWriter.emitEmptyLine();


        // Model View Adapters
        javaWriter.emitField("Map<Class<? extends BaseModelView>, ModelViewAdapter>", FlowManagerHandler.MODEL_VIEW_ADAPTER_MAP_FIELD_NAME,
                FlowManagerHandler.FIELD_MODIFIERS, "new HashMap<>()");
        javaWriter.emitEmptyLine();

    }

    private void writeGetters(JavaWriter javaWriter) throws IOException {
        // Get model Classes
        javaWriter.emitEmptyLine().emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s", FlowManagerHandler.MODEL_FIELD_NAME);
            }
        }, "List<Class<? extends Model>>", "getModelClasses", FlowManagerHandler.METHOD_MODIFIERS);

        // Get model Classes
        javaWriter.emitEmptyLine().emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s", FlowManagerHandler.MODEL_VIEW_FIELD_NAME);
            }
        }, "List<Class<? extends BaseModelView>>", "getModelViews", FlowManagerHandler.METHOD_MODIFIERS);

        // Get Model Adapter
        javaWriter.emitEmptyLine().emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return new ArrayList(%1s.values())", FlowManagerHandler.MODEL_ADAPTER_MAP_FIELD_NAME);
            }
        }, "List<ModelAdapter>", "getModelAdapters", FlowManagerHandler.METHOD_MODIFIERS);


        // Get Model Adapter
        javaWriter.emitEmptyLine().emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s.get(%1s)", FlowManagerHandler.MODEL_ADAPTER_MAP_FIELD_NAME, "table");
            }
        }, "ModelAdapter", "getModelAdapterForTable", FlowManagerHandler.METHOD_MODIFIERS, "Class<? extends Model>", "table");

        // Get Model Container
        javaWriter.emitEmptyLine().emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s.get(%1s)", FlowManagerHandler.MODEL_CONTAINER_ADAPTER_MAP_FIELD_NAME, "table");
            }
        }, "ContainerAdapter", "getModelContainerAdapterForTable", FlowManagerHandler.METHOD_MODIFIERS, "Class<? extends Model>", "table");

        // Get Model Container
        javaWriter.emitEmptyLine().emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s.get(%1s)", FlowManagerHandler.MODEL_VIEW_ADAPTER_MAP_FIELD_NAME, "table");
            }
        }, "ModelViewAdapter", "getModelViewAdapterForTable", FlowManagerHandler.METHOD_MODIFIERS, "Class<? extends BaseModelView>", "table");


        // Get Model View Adapters
        javaWriter.emitEmptyLine().emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return new ArrayList(%1s.values())", FlowManagerHandler.MODEL_VIEW_ADAPTER_MAP_FIELD_NAME);
            }
        }, "List<ModelViewAdapter>", "getModelViewAdapters", FlowManagerHandler.METHOD_MODIFIERS);


        // Get Model Container
        javaWriter.emitEmptyLine().emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s", foreignKeysSupported);
            }
        }, "boolean", "isForeignKeysSupported", FlowManagerHandler.METHOD_MODIFIERS);

        // Get Model Container
        javaWriter.emitEmptyLine().emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s", databaseVersion);
            }
        }, "int", "getDatabaseVersion", FlowManagerHandler.METHOD_MODIFIERS);

        // Get Model Container
        javaWriter.emitEmptyLine().emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return \"%1s\"", databaseName);
            }
        }, "String", "getDatabaseName", FlowManagerHandler.METHOD_MODIFIERS);


    }



}
