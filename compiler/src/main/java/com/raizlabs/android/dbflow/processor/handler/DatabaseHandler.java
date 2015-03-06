package com.raizlabs.android.dbflow.processor.handler;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.writer.DatabaseWriter;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Description: Deals with writing database definitions
 */
public class DatabaseHandler extends BaseContainerHandler<Database> {


    public static final Set<Modifier> FIELD_MODIFIERS = Sets.newHashSet(Modifier.PRIVATE, Modifier.FINAL);
    public static final Set<Modifier> FIELD_MODIFIERS_STATIC = Sets.newHashSet(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

    public static final Set<Modifier> METHOD_MODIFIERS = Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL);
    public static final Set<Modifier> METHOD_MODIFIERS_STATIC = Sets.newHashSet(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

    public static final String MODEL_FIELD_NAME = "mModels";
    public static final String MODEL_ADAPTER_MAP_FIELD_NAME = "mModelAdapters";
    public static final String MODEL_CONTAINER_ADAPTER_MAP_FIELD_NAME = "mModelContainerAdapters";
    public static final String TYPE_CONVERTER_MAP_FIELD_NAME = "mTypeConverters";
    public static final String MODEL_VIEW_FIELD_NAME = "mModelViews";
    public static final String MIGRATION_FIELD_NAME = "mMigrationMap";

    public static final String MODEL_VIEW_ADAPTER_MAP_FIELD_NAME = "mModelViewAdapterMap";

    public static final String MODEL_NAME_MAP = "mModelTableNames";

    @Override
    protected Class<Database> getAnnotationClass() {
        return Database.class;
    }


    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        DatabaseWriter managerWriter = new DatabaseWriter(processorManager, element);
        processorManager.addFlowManagerWriter(managerWriter);
    }
}
