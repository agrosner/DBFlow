package com.grosner.processor.handler;

import com.grosner.dbflow.annotation.Table;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.validator.TableValidator;
import com.grosner.processor.validator.Validator;
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

    private Validator<TableDefinition> definitionValidator;

    public TableHandler() {
        definitionValidator = new TableValidator();
    }

    @Override
    protected Class<Table> getAnnotationClass() {
        return Table.class;
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        try {
            TableDefinition tableDefinition = new TableDefinition(processorManager, element);
            if(definitionValidator.validate(processorManager, tableDefinition)) {
                JavaWriter javaWriter = new JavaWriter(processorManager.getProcessingEnvironment().getFiler().createSourceFile(tableDefinition.getSourceFileName()).openWriter());
                tableDefinition.write(javaWriter);
                javaWriter.close();

                tableDefinition.writeAdapter(processorManager.getProcessingEnvironment());
                processorManager.addTableDefinition(tableDefinition);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
