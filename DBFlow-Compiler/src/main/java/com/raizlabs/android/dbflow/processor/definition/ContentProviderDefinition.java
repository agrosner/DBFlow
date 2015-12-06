package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.DBFlowProcessor;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.validator.TableEndpointValidator;
import com.raizlabs.android.dbflow.processor.writer.DatabaseWriter;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.raizlabs.android.dbflow.processor.writer.provider.DeleteWriter;
import com.raizlabs.android.dbflow.processor.writer.provider.InsertWriter;
import com.raizlabs.android.dbflow.processor.writer.provider.QueryWriter;
import com.raizlabs.android.dbflow.processor.writer.provider.UpdateWriter;
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

    static final String DEFINITION_NAME = "Provider";

    static final String DATABASE_FIELD = "database";

    public static final String URI_MATCHER = "MATCHER";

    private static final String AUTHORITY = "AUTHORITY";

    public String databaseName;

    public String authority;

    public List<TableEndpointDefinition> endpointDefinitions = Lists.newArrayList();

    private FlowWriter[] mWriters;

    public ContentProviderDefinition(Element typeElement, ProcessorManager processorManager) {
        super(typeElement, processorManager);

        ContentProvider provider = element.getAnnotation(ContentProvider.class);
        databaseName = provider.databaseName();
        DatabaseWriter databaseWriter = manager.getDatabaseWriter(databaseName);
        setDefinitionClassName(databaseWriter.classSeparator + DEFINITION_NAME);

        authority = provider.authority();

        TableEndpointValidator validator = new TableEndpointValidator();
        List<? extends Element> elements = manager.getElements().getAllMembers((TypeElement) typeElement);
        for (Element innerElement : elements) {
            if (innerElement.getAnnotation(TableEndpoint.class) != null) {
                TableEndpointDefinition endpointDefinition = new TableEndpointDefinition(innerElement, manager);
                if (validator.validate(processorManager, endpointDefinition)) {
                    endpointDefinitions.add(endpointDefinition);
                }
            }
        }

        mWriters = new FlowWriter[]{
                new QueryWriter(this, manager),
                new InsertWriter(this),
                new DeleteWriter(this, manager),
                new UpdateWriter(this, manager)
        };
    }

    @Override
    protected String getExtendsClass() {
        return "BaseContentProvider";
    }

    @Override
    protected String[] getImports() {
        return new String[]{
                Classes.CONTENT_PROVIDER,
                Classes.FLOW_MANAGER,
                Classes.SQLITE_DATABASE,
                Classes.BASE_DATABASE_DEFINITION_NAME,
                Classes.CURSOR,
                Classes.URI,
                Classes.URI_MATCHER,
                Classes.BASE_CONTENT_PROVIDER,
                Classes.SELECT,
                Classes.WHERE,
                Classes.CONTENT_VALUES,
                Classes.CONTENT_URIS,
                Classes.MODEL_ADAPTER,
                Classes.CONFLICT_ACTION,
                Classes.CONDITION,
                Classes.DELETE,
                Classes.UPDATE
        };
    }

    @Override
    public void onWriteDefinition(JavaWriter javaWriter) throws IOException {

        javaWriter.emitEmptyLine();
        javaWriter.emitField("String", AUTHORITY, Sets.newHashSet(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
                "\"" + authority + "\"");
        javaWriter.emitEmptyLine();
        int code = 0;
        for (TableEndpointDefinition endpointDefinition : endpointDefinitions) {
            for (ContentUriDefinition contentUriDefinition : endpointDefinition.contentUriDefinitions) {
                javaWriter.emitField("int", contentUriDefinition.name,
                        EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL), String.valueOf(code));
                code++;
            }
        }

        javaWriter.emitEmptyLine();
        javaWriter.emitField("UriMatcher", URI_MATCHER, Sets.newHashSet(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
                "new UriMatcher(UriMatcher.NO_MATCH)").beginInitializer(true);
        for (TableEndpointDefinition endpointDefinition : endpointDefinitions) {
            for (ContentUriDefinition contentUriDefinition : endpointDefinition.contentUriDefinitions) {
                String path;
                if (contentUriDefinition.path != null) {
                    path = "\"" + contentUriDefinition.path + "\"";
                } else {
                    path = String.format("%s.%s.getPath()", contentUriDefinition.classQualifiedName, contentUriDefinition.name);
                }
                javaWriter.emitStatement("%1s.addURI(%s, %s, %s)", URI_MATCHER, AUTHORITY, path, contentUriDefinition.name);
            }
        }
        javaWriter.endInitializer();

        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return \"%1s\"", databaseName);
            }
        }, "String", "getDatabaseName", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));

        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("String type = null");
                javaWriter.beginControlFlow("switch(MATCHER.match(uri))");

                for (TableEndpointDefinition tableEndpointDefinition : endpointDefinitions) {
                    for (ContentUriDefinition uriDefinition : tableEndpointDefinition.contentUriDefinitions) {
                        javaWriter.beginControlFlow("case " + uriDefinition.name + ":")
                                .emitStatement("type = \"%1s\"", uriDefinition.type)
                                .emitStatement("break")
                                .endControlFlow();
                    }
                }

                javaWriter.beginControlFlow("default:")
                        .emitStatement("throw new IllegalArgumentException(\"Unknown URI \" + uri)")
                        .endControlFlow();

                javaWriter.endControlFlow();
                javaWriter.emitStatement("return type");
            }
        }, "String", "getType", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), "Uri", "uri");

        for (FlowWriter writer : mWriters) {
            writer.write(javaWriter);
        }


    }
}
