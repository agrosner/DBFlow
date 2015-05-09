package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.processor.definition.QueryModelDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;

import java.io.IOException;

import javax.lang.model.element.Element;

/**
 * Description: Handles {@link QueryModel} annotations, writing QueryModelAdapter, and
 * adding them to the {@link ProcessorManager}.
 */
public class QueryModelHandler extends BaseContainerHandler<QueryModel> {

    @Override
    protected Class<QueryModel> getAnnotationClass() {
        return QueryModel.class;
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        QueryModelDefinition queryModelDefinition = new QueryModelDefinition(element, processorManager);

        processorManager.addQueryModelDefinition(queryModelDefinition);

        WriterUtils.writeBaseDefinition(queryModelDefinition, processorManager);

        try {
            queryModelDefinition.writeAdapter(processorManager.getProcessingEnvironment());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
