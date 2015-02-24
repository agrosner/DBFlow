package com.raizlabs.android.dbflow.processor.writer.provider;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.provider.Notify;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.NotifyDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.model.builder.SqlQueryBuilder;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class UpdateWriter implements FlowWriter {

    private final ContentProviderDefinition contentProviderDefinition;

    private final ProcessorManager manager;

    public UpdateWriter(ContentProviderDefinition contentProviderDefinition, ProcessorManager manager) {
        this.contentProviderDefinition = contentProviderDefinition;
        this.manager = manager;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                    @Override
                    public void write(JavaWriter javaWriter) throws IOException {

                        javaWriter.beginControlFlow("switch(MATCHER.match(uri))");

                        for (TableEndpointDefinition tableEndpointDefinition : contentProviderDefinition.endpointDefinitions) {
                            for (ContentUriDefinition uriDefinition : tableEndpointDefinition.contentUriDefinitions) {
                                if (uriDefinition.updateEnabled) {
                                    javaWriter.beginControlFlow("case %1s:", uriDefinition.name);
                                    javaWriter.emitStatement("ModelAdapter adapter = FlowManager.getModelAdapter(FlowManager.getTableClassForName(\"%1s\", \"%1s\"))", contentProviderDefinition.databaseName, tableEndpointDefinition.tableName);

                                    SqlQueryBuilder sqlQueryBuilder = new SqlQueryBuilder("final int count = (int) ")
                                            .appendUpdate().appendUpdateConflictAction().appendTable(contentProviderDefinition.databaseName, tableEndpointDefinition.tableName)
                                            .appendSet().appendWhere().appendPathSegments(manager, contentProviderDefinition.databaseName,
                                                    tableEndpointDefinition.tableName, uriDefinition.segments)
                                            .appendCount();
                                    javaWriter.emitStatement(sqlQueryBuilder.getQuery());

                                    new NotifyWriter(tableEndpointDefinition, uriDefinition,
                                            Notify.Method.UPDATE).write(javaWriter);

                                    javaWriter.emitStatement("return count");
                                    javaWriter.endControlFlow();
                                }
                            }

                        }

                        javaWriter.beginControlFlow("default:")
                                .emitStatement("throw new IllegalArgumentException(\"Unknown URI \" + uri)")
                                .endControlFlow();

                        javaWriter.endControlFlow();
                    }
                }, "int", "update", Sets.newHashSet(Modifier.PUBLIC), "Uri", "uri", "ContentValues",
                "values", "String", "selection", "String[]", "selectionArgs");
    }
}
