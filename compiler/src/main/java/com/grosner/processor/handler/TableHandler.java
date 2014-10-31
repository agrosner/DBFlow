package com.grosner.processor.handler;

import com.grosner.dbflow.annotation.Table;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.model.ProcessorManager;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class TableHandler extends BaseContainerHandler<Table> {

    public TableHandler(RoundEnvironment roundEnv, ProcessorManager manager) {
        super(Table.class, roundEnv, manager);
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, String packageName, Element element) {
        try {
            TableDefinition tableDefinition = new TableDefinition(processorManager, packageName, element);
            JavaWriter javaWriter = new JavaWriter(processorManager.getProcessingEnvironment().getFiler().createSourceFile(tableDefinition.getFQCN()).openWriter());
            tableDefinition.write(javaWriter);
            javaWriter.close();

            tableDefinition.writeAdapter(processorManager.getProcessingEnvironment());
            processorManager.addTableDefinition(tableDefinition);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
