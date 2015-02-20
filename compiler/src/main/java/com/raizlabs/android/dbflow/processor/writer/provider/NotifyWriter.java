package com.raizlabs.android.dbflow.processor.writer.provider;

import com.raizlabs.android.dbflow.annotation.provider.Notify;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.NotifyDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Description:
 */
public class NotifyWriter implements FlowWriter {

    private final TableEndpointDefinition tableEndpointDefinition;
    private final ContentUriDefinition uriDefinition;
    private final Notify.Method method;

    public NotifyWriter(TableEndpointDefinition tableEndpointDefinition, ContentUriDefinition uriDefinition, Notify.Method method) {
        this.tableEndpointDefinition = tableEndpointDefinition;
        this.uriDefinition = uriDefinition;
        this.method = method;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        boolean hasListener = false;
        Map<Notify.Method, List<NotifyDefinition>> notifyDefinitionMap = tableEndpointDefinition.notifyDefinitionPathMap.get(uriDefinition.path);
        if (notifyDefinitionMap != null) {
            List<NotifyDefinition> notifyDefinitionList = notifyDefinitionMap.get(method);
            if (notifyDefinitionList != null) {
                for (int i = 0; i < notifyDefinitionList.size(); i++) {
                    NotifyDefinition notifyDefinition = notifyDefinitionList.get(i);
                    if (i == 0) {
                        javaWriter.emitStatement("Uri[] notifyUris = %1s.%1s(%1s)", notifyDefinition.parent, notifyDefinition.methodName, notifyDefinition.params);
                        javaWriter.beginControlFlow("for (Uri notifyUri: notifyUris)");
                    }
                    javaWriter.emitStatement("getContext().getContentResolver().notifyChange(notifyUri, null)");

                    if (i == notifyDefinitionList.size() - 1) {
                        javaWriter.endControlFlow();
                    }

                    hasListener = true;
                }
            }
        }

        if (!hasListener) {

            boolean isUpdateDelete = method.equals(Notify.Method.UPDATE) || method.equals(Notify.Method.DELETE);
            if(isUpdateDelete) {
                javaWriter.beginControlFlow("if (count > 0)");
            }

            javaWriter.emitStatement("getContext().getContentResolver().notifyChange(uri, null)");

            if(isUpdateDelete) {
                javaWriter.endControlFlow();
            }
        }
    }
}
