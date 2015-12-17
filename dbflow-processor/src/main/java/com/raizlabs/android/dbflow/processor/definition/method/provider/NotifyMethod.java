package com.raizlabs.android.dbflow.processor.definition.method.provider;

import com.raizlabs.android.dbflow.annotation.provider.Notify;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.CodeAdder;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.NotifyDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.squareup.javapoet.CodeBlock;

import java.util.List;
import java.util.Map;

/**
 * Description:
 */
public class NotifyMethod implements CodeAdder {

    private final TableEndpointDefinition tableEndpointDefinition;
    private final ContentUriDefinition uriDefinition;
    private final Notify.Method method;

    public NotifyMethod(TableEndpointDefinition tableEndpointDefinition, ContentUriDefinition uriDefinition, Notify.Method method) {
        this.tableEndpointDefinition = tableEndpointDefinition;
        this.uriDefinition = uriDefinition;
        this.method = method;
    }

    @Override
    public void addCode(CodeBlock.Builder code) {
        boolean hasListener = false;
        Map<Notify.Method, List<NotifyDefinition>> notifyDefinitionMap = tableEndpointDefinition.notifyDefinitionPathMap.get(uriDefinition.path);
        if (notifyDefinitionMap != null) {
            List<NotifyDefinition> notifyDefinitionList = notifyDefinitionMap.get(method);
            if (notifyDefinitionList != null) {
                for (int i = 0; i < notifyDefinitionList.size(); i++) {
                    NotifyDefinition notifyDefinition = notifyDefinitionList.get(i);
                    if (notifyDefinition.returnsArray) {
                        code.addStatement("$T[] notifyUris$L = $L.$L($L)", ClassNames.URI,
                                notifyDefinition.methodName, notifyDefinition.parent,
                                notifyDefinition.methodName, notifyDefinition.params);
                        code.beginControlFlow("for ($T notifyUri: notifyUris$L)", ClassNames.URI, notifyDefinition.methodName);
                    } else {
                        code.addStatement("$T notifyUri$L = $L.$L($L)", ClassNames.URI,
                                notifyDefinition.methodName, notifyDefinition.parent,
                                notifyDefinition.methodName, notifyDefinition.params);
                    }
                    code.addStatement("getContext().getContentResolver().notifyChange(notifyUri$L, null)",
                            notifyDefinition.returnsArray ? "" : notifyDefinition.methodName);
                    if (notifyDefinition.returnsArray) {
                        code.endControlFlow();
                    }

                    hasListener = true;
                }
            }
        }

        if (!hasListener) {

            boolean isUpdateDelete = method.equals(Notify.Method.UPDATE) || method.equals(Notify.Method.DELETE);
            if (isUpdateDelete) {
                code.beginControlFlow("if (count > 0)");
            }

            code.addStatement("getContext().getContentResolver().notifyChange(uri, null)");

            if (isUpdateDelete) {
                code.endControlFlow();
            }
        }
    }

}
