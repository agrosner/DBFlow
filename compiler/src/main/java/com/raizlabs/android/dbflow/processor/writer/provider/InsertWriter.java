package com.raizlabs.android.dbflow.processor.writer.provider;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.provider.Notify;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.NotifyDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
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
public class InsertWriter implements FlowWriter {

    private final ContentProviderDefinition contentProviderDefinition;

    public InsertWriter(ContentProviderDefinition contentProviderDefinition) {
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
                                if (uriDefinition.insertEnabled) {
                                    javaWriter.beginControlFlow("case %1s:", uriDefinition.name);
                                    javaWriter.emitStatement("ModelAdapter adapter = FlowManager.getModelAdapter(FlowManager.getTableClassForName(\"%1s\", \"%1s\"))", contentProviderDefinition.databaseName, tableEndpointDefinition.tableName);

                                    SqlQueryBuilder queryBuilder = new SqlQueryBuilder("final long id = ")
                                            .appendGetDatabase(contentProviderDefinition.databaseName)
                                            .appendInsertWithOnConflict(tableEndpointDefinition.tableName);

                                    javaWriter.emitStatement(queryBuilder.getQuery());

                                    new NotifyWriter(tableEndpointDefinition, uriDefinition,
                                            Notify.Method.INSERT).write(javaWriter);

                                    javaWriter.emitStatement("return ContentUris.withAppendedId(uri, id)");
                                    javaWriter.endControlFlow();
                                }
                            }

                            javaWriter.beginControlFlow("default:")
                                    .emitStatement("throw new IllegalArgumentException(\"Unknown URI \" + uri)")
                                    .endControlFlow();
                        }

                        javaWriter.endControlFlow();
                    }
                }, "Uri", "insert", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL),
                "Uri", "uri", "ContentValues", "values");
    }
}
