package com.raizlabs.android.dbflow.processor.writer.provider;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.model.builder.SqlQueryBuilder;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class QueryWriter implements FlowWriter {

    private final ContentProviderDefinition contentProviderDefinition;

    private final ProcessorManager manager;

    public QueryWriter(ContentProviderDefinition contentProviderDefinition, ProcessorManager manager) {
        this.contentProviderDefinition = contentProviderDefinition;
        this.manager = manager;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                    @Override
                    public void write(JavaWriter javaWriter) throws IOException {
                        javaWriter.emitStatement("Cursor cursor = null");

                        javaWriter.beginControlFlow("switch(%1s.match(uri))", ContentProviderDefinition.URI_MATCHER);
                        for (TableEndpointDefinition tableEndpointDefinition : contentProviderDefinition.endpointDefinitions) {
                            for (ContentUriDefinition uriDefinition : tableEndpointDefinition.contentUriDefinitions) {
                                if (uriDefinition.queryEnabled) {
                                    javaWriter.beginControlFlow("case %1s:", uriDefinition.name);

                                    SqlQueryBuilder where = new SqlQueryBuilder("Where where = ")
                                            .appendSelect()
                                            .appendFromTable(contentProviderDefinition.databaseName, tableEndpointDefinition.tableName)
                                            .appendWhere().appendPathSegments(manager, contentProviderDefinition.databaseName,
                                                    tableEndpointDefinition.tableName, uriDefinition.segments);
                                    javaWriter.emitStatement(where.getQuery());

                                    javaWriter.beginControlFlow("if (sortOrder != null && !sortOrder.isEmpty())");
                                    SqlQueryBuilder orderBy = new SqlQueryBuilder("where = ");
                                    javaWriter.emitStatement(orderBy.append("where").appendOrderBy().getQuery());
                                    javaWriter.endControlFlow();

                                    SqlQueryBuilder select = new SqlQueryBuilder("cursor = ")
                                            .append("where").appendQuery();
                                    javaWriter.emitStatement(select.getQuery());
                                    javaWriter.emitStatement("break");
                                    javaWriter.endControlFlow();
                                }
                            }
                        }

                        javaWriter.endControlFlow();

                        javaWriter.beginControlFlow("if (cursor != null)");
                        javaWriter.emitStatement("cursor.setNotificationUri(getContext().getContentResolver(), uri)");
                        javaWriter.endControlFlow();
                        javaWriter.emitEmptyLine();
                        javaWriter.emitStatement("return cursor");
                    }
                }, "Cursor", "query", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL),
                "Uri", "uri", "String[]", "projection", "String", "selection",
                "String[]", "selectionArgs", "String", "sortOrder");
    }
}
