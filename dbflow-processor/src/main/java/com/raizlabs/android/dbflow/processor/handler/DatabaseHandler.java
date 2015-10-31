package com.raizlabs.android.dbflow.processor.handler;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

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

    public static final String MODEL_FIELD_NAME = "models";
    public static final String MODEL_ADAPTER_MAP_FIELD_NAME = "modelAdapters";
    public static final String MODEL_CONTAINER_ADAPTER_MAP_FIELD_NAME = "modelContainerAdapters";
    public static final String QUERY_MODEL_ADAPTER_MAP_FIELD_NAME = "queryModelAdapterMap";
    public static final String TYPE_CONVERTER_MAP_FIELD_NAME = "typeConverters";
    public static final String MODEL_VIEW_FIELD_NAME = "modelViews";
    public static final String MIGRATION_FIELD_NAME = "migrationMap";

    public static final String MODEL_VIEW_ADAPTER_MAP_FIELD_NAME = "modelViewAdapterMap";

    public static final String MODEL_NAME_MAP = "modelTableNames";

    @Override
    protected Class<Database> getAnnotationClass() {
        return Database.class;
    }


    @Override
    protected void onProcessElement(ProcessorManager processorManager, Element element) {
        DatabaseDefinition managerWriter = new DatabaseDefinition(processorManager, element);
        processorManager.addFlowManagerWriter(managerWriter);
    }
}
