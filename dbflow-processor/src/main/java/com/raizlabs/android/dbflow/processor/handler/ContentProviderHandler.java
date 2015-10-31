package com.raizlabs.android.dbflow.processor.handler;

import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.validator.ContentProviderValidator;

import javax.lang.model.element.Element;

/**
 * Description:
 */
public class ContentProviderHandler extends BaseContainerHandler<ContentProvider> {

    @Override
    protected Class<ContentProvider> getAnnotationClass() {
        return ContentProvider.class;
    }

    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        ContentProviderDefinition contentProviderDefinition = new ContentProviderDefinition(element, processorManager);
        if (contentProviderDefinition.elementClassName != null) {
            processorManager.addContentProviderDefinition(contentProviderDefinition);
        }
    }
}
