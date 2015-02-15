package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.DBFlowProcessor;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Description:
 */
public class ContentProviderDefinition extends BaseDefinition {

    static final String DEFINITION_NAME = "$Provider";

    static final String DATABASE_FIELD = "database";

    private static final String URI_MATCHER = "MATCHER";

    private static final String AUTHORITY = "AUTHORITY";

    public String databaseName;

    public String authority;

    public List<TableEndpointDefinition> endpointDefinitions = Lists.newArrayList();

    public ContentProviderDefinition(Element typeElement, ProcessorManager processorManager) {
        super(typeElement, processorManager);
        setDefinitionClassName(DEFINITION_NAME);

        ContentProvider provider = element.getAnnotation(ContentProvider.class);
        databaseName = provider.databaseName();
        if (databaseName == null || databaseName.isEmpty()) {
            databaseName = DBFlowProcessor.DEFAULT_DB_NAME;
        }

        authority = provider.authority();

        List<? extends Element> elements = manager.getElements().getAllMembers((TypeElement) typeElement);
        for(Element innerElement: elements) {
            if(innerElement.getAnnotation(TableEndpoint.class)!=null) {
                TableEndpointDefinition endpointDefinition = new TableEndpointDefinition(innerElement, manager);

                endpointDefinitions.add(endpointDefinition);
            }
        }
    }

    @Override
    protected String getExtendsClass() {
        return "ContentProvider";
    }

    @Override
    protected String[] getImports() {
        return new String[] {
                Classes.CONTENT_PROVIDER,
                Classes.FLOW_MANAGER,
                Classes.SQLITE_DATABASE,
                Classes.BASE_DATABASE_DEFINITION_NAME,
                Classes.CURSOR,
                Classes.URI,
                Classes.URI_MATCHER
        };
    }

    @Override
    public void onWriteDefinition(JavaWriter javaWriter) throws IOException {

        javaWriter.emitEmptyLine();
        javaWriter.emitField("BaseDatabaseDefinition", DATABASE_FIELD, Sets.newHashSet(Modifier.PRIVATE));
        javaWriter.emitEmptyLine();

        javaWriter.emitField("String", AUTHORITY, Sets.newHashSet(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
                "\"" + authority + "\"");
        javaWriter.emitEmptyLine();
        int code = 0;
        for(TableEndpointDefinition endpointDefinition: endpointDefinitions) {
            for(ContentUriDefinition contentUriDefinition: endpointDefinition.contentUriDefinitions) {
                javaWriter.emitField("int", contentUriDefinition.name,
                        EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL), String.valueOf(code));
                code++;
            }
        }

        javaWriter.emitEmptyLine();
        javaWriter.emitField("UriMatcher", URI_MATCHER, Sets.newHashSet(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
                "new UriMatcher(UriMatcher.NO_MATCH)").beginInitializer(true);
        for(TableEndpointDefinition endpointDefinition: endpointDefinitions) {
            for(ContentUriDefinition contentUriDefinition: endpointDefinition.contentUriDefinitions) {
                String path;
                if (contentUriDefinition.endpoint != null) {
                    path = "\"" + contentUriDefinition.endpoint + "\"";
                } else {
                    path = String.format("%s.%s.getPath()", contentUriDefinition.classQualifiedName, contentUriDefinition.name);
                }
                javaWriter.emitStatement("MATCHER.addURI(%s, %s, %s)", AUTHORITY, path, contentUriDefinition.name);
            }
        }
        javaWriter.endInitializer();

        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("FlowManager.init(getContext())");
                javaWriter.emitStatement("%1s = FlowManager.getDatabase(\"%1s\")", DATABASE_FIELD, databaseName);
                javaWriter.emitStatement("return true");
            }
        }, "boolean", "onCreate", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));

        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {

                javaWriter.emitStatement("return cursor");
            }
        }, "Cursor", "cursor", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL),
                "Uri", "uri", "String[]", "projection", "String", "selection",
                "String[]", "selectionArgs", "String", "sortOrder");
    }
}
