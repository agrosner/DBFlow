package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.processor.Classes;
import com.grosner.processor.definition.TypeConverterDefinition;
import com.grosner.processor.handler.FlowManagerHandler;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.utils.ModelUtils;
import com.grosner.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class StaticFlowManagerWriter implements FlowWriter {

    private final ProcessorManager processorManager;

    public StaticFlowManagerWriter(ProcessorManager processorManager){
        this.processorManager = processorManager;
    }

    @Override
    public void write(JavaWriter staticFlowManager) throws IOException {
        staticFlowManager.emitPackage(Classes.FLOW_MANAGER_PACKAGE);
        staticFlowManager.emitImports(Classes.MAP, Classes.HASH_MAP, Classes.TYPE_CONVERTER, Classes.FLOW_MANAGER, Classes.FLOW_MANAGER_STATIC_INTERFACE);
        staticFlowManager.beginType(Classes.FLOW_MANAGER_STATIC_CLASS_NAME, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), null, Classes.FLOW_MANAGER_STATIC_INTERFACE);

        // type converters
        staticFlowManager.emitField("Map<Class<?>, TypeConverter>", FlowManagerHandler.TYPE_CONVERTER_MAP_FIELD_NAME,
                FlowManagerHandler.FIELD_MODIFIERS_STATIC, "new HashMap<>()");
        staticFlowManager.emitEmptyLine();

        staticFlowManager.beginConstructor(Sets.newHashSet(Modifier.PUBLIC));

        staticFlowManager.emitSingleLineComment("Registering with FlowManager");
        staticFlowManager.emitStatement("FlowManager.setStaticManagerInterface(%1s)", "this");
        staticFlowManager.emitEmptyLine();

        staticFlowManager.endConstructor();

        staticFlowManager.beginInitializer(true);

        for (TypeConverterDefinition typeConverterDefinition: processorManager.getTypeConverters()) {
            staticFlowManager.emitStatement(FlowManagerHandler.TYPE_CONVERTER_MAP_FIELD_NAME + ".put(%1s, new %1s())", ModelUtils.getFieldClass(typeConverterDefinition.getModelClassQualifiedName()),
                    typeConverterDefinition.getQualifiedName());
        }

        staticFlowManager.endInitializer();

        // Get TypeConverter
        staticFlowManager.emitEmptyLine().emitAnnotation(Override.class);
        WriterUtils.emitMethod(staticFlowManager, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return %1s.get(%1s)", FlowManagerHandler.TYPE_CONVERTER_MAP_FIELD_NAME, "clazz");
            }
        }, "TypeConverter", "getTypeConverterForClass", FlowManagerHandler.METHOD_MODIFIERS_STATIC, "Class<?>", "clazz");

        staticFlowManager.endType();
    }
}
