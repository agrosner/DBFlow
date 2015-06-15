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
public class InsertWriter implements FlowWriter {

    private final ContentProviderDefinition contentProviderDefinition;

    public InsertWriter(ContentProviderDefinition contentProviderDefinition) {
        this.contentProviderDefinition = contentProviderDefinition;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        writeMethod(javaWriter, false);
        writeMethod(javaWriter, true);
    }

    protected void writeMethod(JavaWriter javaWriter, final boolean isBulk) {
        String returnType = isBulk ? "int" : "Uri";
        String methodName = isBulk ? "bulkInsert": "insert";
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

                                    if(!isBulk) {
                                        new NotifyWriter(tableEndpointDefinition, uriDefinition,
                                                Notify.Method.INSERT).write(javaWriter);

                                        javaWriter.emitStatement("return ContentUris.withAppendedId(uri, id)");
                                    } else {
                                        javaWriter.emitStatement("return id > 0 ? 1 : 0");
                                    }
                                    javaWriter.endControlFlow();
                                }
                            }

                        }

                        javaWriter.beginControlFlow("default:")
                                .emitStatement("throw new IllegalArgumentException(\"Unknown URI \" + uri)")
                                .endControlFlow();

                        javaWriter.endControlFlow();
                    }
                }, returnType, methodName, Sets.newHashSet(isBulk ? Modifier.PROTECTED : Modifier.PUBLIC, Modifier.FINAL),
                "Uri", "uri", "ContentValues", "values");
    }
}
