package com.raizlabs.android.dbflow.processor.writer;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.handler.FlowManagerHandler;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Description: Top-level writer that handles writing all {@link com.raizlabs.android.dbflow.processor.writer.DatabaseWriter}
 * and {@link com.raizlabs.android.dbflow.annotation.TypeConverter}
 */
public class FlowManagerHolderWriter implements FlowWriter {

    private final ProcessorManager processorManager;

    public FlowManagerHolderWriter(ProcessorManager processorManager){
        this.processorManager = processorManager;
    }

    @Override
    public void write(JavaWriter staticFlowManager) throws IOException {
        staticFlowManager.emitPackage(Classes.FLOW_MANAGER_PACKAGE);
        staticFlowManager.beginType(Classes.DATABASE_HOLDER_STATIC_CLASS_NAME, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), Classes.FLOW_MANAGER_STATIC_INTERFACE);

        staticFlowManager.beginConstructor(Sets.newHashSet(Modifier.PUBLIC));

        staticFlowManager.emitSingleLineComment("Registering with FlowManagerHolder");
        for(DatabaseWriter databaseWriter : processorManager.getManagerWriters()) {
            staticFlowManager.emitStatement("new %1s(this)", databaseWriter.getSourceFileName());
        }
        staticFlowManager.emitEmptyLine();

        staticFlowManager.endConstructor();

        staticFlowManager.beginInitializer(true);

        for (TypeConverterDefinition typeConverterDefinition: processorManager.getTypeConverters()) {
            staticFlowManager.emitStatement(FlowManagerHandler.TYPE_CONVERTER_MAP_FIELD_NAME + ".put(%1s, new %1s())", ModelUtils.getFieldClass(typeConverterDefinition.getModelClassQualifiedName()),
                    typeConverterDefinition.getQualifiedName());
        }

        staticFlowManager.endInitializer();

        staticFlowManager.endType();
    }
}
