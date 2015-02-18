package com.raizlabs.android.dbflow.processor.writer.provider;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.provider.Notify;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.model.builder.SqlQueryBuilder;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class DeleteWriter implements FlowWriter {

    private final ContentProviderDefinition contentProviderDefinition;

    public DeleteWriter(ContentProviderDefinition contentProviderDefinition) {
        this.contentProviderDefinition = contentProviderDefinition;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                    @Override
                    public void write(JavaWriter javaWriter) throws IOException {
                        javaWriter.beginControlFlow("switch(MATCHER.match(uri))");

                        for (TableEndpointDefinition tableEndpointDefinition : contentProviderDefinition.endpointDefinitions) {
                            for (ContentUriDefinition uriDefinition : tableEndpointDefinition.contentUriDefinitions) {
                                if (uriDefinition.deleteEnabled) {

                                    javaWriter.beginControlFlow("case %1s:", uriDefinition.name);

                                    SqlQueryBuilder queryBuilder = new SqlQueryBuilder("long count = ")
                                            .appendDelete()
                                            .appendFromTable(contentProviderDefinition.databaseName, tableEndpointDefinition.tableName)
                                            .appendWhere()
                                            .appendPathSegments(uriDefinition.segments)
                                            .appendCount();
                                    javaWriter.emitStatement(queryBuilder.getQuery());
                                    javaWriter.emitStatement("return (int) count");
                                    javaWriter.endControlFlow();

                                    new NotifyWriter(tableEndpointDefinition, uriDefinition,
                                            Notify.Method.DELETE).write(javaWriter);

                                }
                            }

                            javaWriter.beginControlFlow("default:")
                                    .emitStatement("throw new IllegalArgumentException(\"Unknown URI \" + uri)")
                                    .endControlFlow();
                        }


                        javaWriter.endControlFlow();
                    }
                }, "int", "delete", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL),
                "Uri", "uri", "String", "selection", "String[]", "selectionArgs");
    }
}
