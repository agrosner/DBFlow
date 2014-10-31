package com.grosner.processor.handler;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Database;
import com.grosner.processor.Classes;
import com.grosner.processor.definition.TypeConverterDefinition;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.utils.ModelUtils;
import com.grosner.processor.utils.WriterUtils;
import com.grosner.processor.writer.FlowManagerWriter;
import com.grosner.processor.writer.FlowWriter;
import com.grosner.processor.writer.StaticFlowManagerWriter;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class FlowManagerHandler extends BaseContainerHandler<Database> {


    public static final Set<Modifier> FIELD_MODIFIERS = Sets.newHashSet(Modifier.PRIVATE, Modifier.FINAL);
    public static final Set<Modifier> FIELD_MODIFIERS_STATIC = Sets.newHashSet(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

    public static final Set<Modifier> METHOD_MODIFIERS = Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL);
    public static final Set<Modifier> METHOD_MODIFIERS_STATIC = Sets.newHashSet(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

    public static final String MODEL_FIELD_NAME = "mModels";
    public static final String MODEL_ADAPTER_MAP_FIELD_NAME = "mModelAdapters";
    public static final String MODEL_CONTAINER_ADAPTER_MAP_FIELD_NAME = "mModelContainerAdapters";
    public static final String TYPE_CONVERTER_MAP_FIELD_NAME = "mTypeConverters";
    public static final String MODEL_VIEW_MAP_FIELD_NAME = "mModelViews";
    public static final String FLOW_SQL_LITE_OPEN_HELPER_FIELD_NAME = "mHelper";
    public static final String IS_RESETTING = "isResetting";

    public FlowManagerHandler(RoundEnvironment roundEnvironment, ProcessorManager processorManager) {
        super(Database.class, roundEnvironment, processorManager);
        if (roundEnvironment.processingOver()) {
            try {
                JavaWriter staticFlowManager = new JavaWriter(processorManager.getProcessingEnvironment().getFiler()
                        .createSourceFile(Classes.FLOW_MANAGER_PACKAGE + "." + Classes.FLOW_MANAGER_STATIC_CLASS_NAME).openWriter());
                new StaticFlowManagerWriter(processorManager).write(staticFlowManager);

                staticFlowManager.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void onProcessElement(ProcessorManager processorManager, String packageName, Element element) {

        try {

            FlowManagerWriter managerWriter = new FlowManagerWriter(processorManager, packageName, element);
            JavaWriter javaWriter = new JavaWriter(processorManager.getProcessingEnvironment().getFiler()
                    .createSourceFile(managerWriter.getFQCN()).openWriter());
            managerWriter.write(javaWriter);
            javaWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
