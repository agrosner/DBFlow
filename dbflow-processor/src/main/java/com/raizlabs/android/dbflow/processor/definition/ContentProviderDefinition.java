package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.provider.DeleteMethod;
import com.raizlabs.android.dbflow.processor.definition.method.provider.InsertMethod;
import com.raizlabs.android.dbflow.processor.definition.method.provider.QueryMethod;
import com.raizlabs.android.dbflow.processor.definition.method.provider.UpdateMethod;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.validator.TableEndpointValidator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description:
 */
public class ContentProviderDefinition extends BaseDefinition {

    static final String DEFINITION_NAME = "Provider";

    static final String DATABASE_FIELD = "database";

    public static final String URI_MATCHER = "MATCHER";

    private static final String AUTHORITY = "AUTHORITY";

    public TypeName databaseName;
    public String databaseNameString;
    public boolean useSafeQueryChecking;

    public String authority;

    public List<TableEndpointDefinition> endpointDefinitions = Lists.newArrayList();

    private MethodDefinition[] methods;

    public ContentProviderDefinition(Element typeElement, ProcessorManager processorManager) {
        super(typeElement, processorManager);

        ContentProvider provider = element.getAnnotation(ContentProvider.class);
        if (provider != null) {
            try {
                provider.database();
            } catch (MirroredTypeException mte) {
                databaseName = TypeName.get(mte.getTypeMirror());
            }
            useSafeQueryChecking = provider.useSafeQueryChecking();
            DatabaseDefinition databaseDefinition = manager.getDatabaseWriter(databaseName);
            databaseNameString = databaseDefinition.databaseName;
            setOutputClassName(databaseDefinition.classSeparator + DEFINITION_NAME);

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

        }

        methods = new MethodDefinition[]{
                new QueryMethod(this, manager),
                new InsertMethod(this, false),
                new InsertMethod(this, true),
                new DeleteMethod(this, manager),
                new UpdateMethod(this, manager)
        };
    }

    @Override
    protected TypeName getExtendsClass() {
        return ClassNames.BASE_CONTENT_PROVIDER;
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {

        typeBuilder.addField(FieldSpec.builder(ClassName.get(String.class), AUTHORITY, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", authority).build());

        int code = 0;
        for (TableEndpointDefinition endpointDefinition : endpointDefinitions) {
            for (ContentUriDefinition contentUriDefinition : endpointDefinition.contentUriDefinitions) {
                typeBuilder.addField(FieldSpec.builder(TypeName.INT, contentUriDefinition.name,
                        Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(String.valueOf(code)).build());
                code++;
            }
        }

        FieldSpec.Builder uriField = FieldSpec.builder(ClassNames.URI_MATCHER, URI_MATCHER,
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

        CodeBlock.Builder initializer = CodeBlock.builder().addStatement("new $T($T.NO_MATCH)", ClassNames.URI_MATCHER, ClassNames.URI_MATCHER)
                .add("static {\n");

        for (TableEndpointDefinition endpointDefinition : endpointDefinitions) {
            for (ContentUriDefinition contentUriDefinition : endpointDefinition.contentUriDefinitions) {
                String path;
                if (contentUriDefinition.path != null) {
                    path = "\"" + contentUriDefinition.path + "\"";
                } else {
                    path = CodeBlock.builder().add("$L.$L.getPath()", contentUriDefinition.elementClassName, contentUriDefinition.name).build().toString();
                }
                initializer.addStatement("$L.addURI($L, $L, $L)", URI_MATCHER, AUTHORITY, path, contentUriDefinition.name);
            }
        }
        initializer.add("}\n");
        typeBuilder.addField(uriField.initializer(initializer.build()).build());

        typeBuilder.addMethod(MethodSpec.methodBuilder("getDatabaseName")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return $S", databaseNameString)
                .returns(ClassName.get(String.class)).build());

        MethodSpec.Builder getTypeBuilder = MethodSpec.methodBuilder("getType")
                .addAnnotation(Override.class)
                .addParameter(ClassNames.URI, "uri")
                .returns(ClassName.get(String.class))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        CodeBlock.Builder getTypeCode = CodeBlock.builder()
                .addStatement("$T type = null", ClassName.get(String.class))
                .beginControlFlow("switch($L.match(uri))", URI_MATCHER);

        for (TableEndpointDefinition tableEndpointDefinition : endpointDefinitions) {
            for (ContentUriDefinition uriDefinition : tableEndpointDefinition.contentUriDefinitions) {
                getTypeCode.beginControlFlow("case $L:", uriDefinition.name)
                        .addStatement("type = $S", uriDefinition.type)
                        .addStatement("break")
                        .endControlFlow();
            }
        }
        getTypeCode.beginControlFlow("default:")
                .addStatement("throw new $T($S + $L)", ClassName.get(IllegalArgumentException.class), "Unknown URI", "uri")
                .endControlFlow();
        getTypeCode.endControlFlow();
        getTypeCode.addStatement("return type");

        getTypeBuilder.addCode(getTypeCode.build());
        typeBuilder.addMethod(getTypeBuilder.build());

        for (MethodDefinition method : methods) {
            MethodSpec methodSpec = method.getMethodSpec();
            if (methodSpec != null) {
                typeBuilder.addMethod(methodSpec);
            }
        }


    }
}
