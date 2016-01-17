package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.CustomTypeConverterPropertyMethod;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ElementUtility;
import com.raizlabs.android.dbflow.processor.validator.ColumnValidator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description:
 */
public class QueryModelDefinition extends BaseTableDefinition {

    private static final String DBFLOW_QUERY_MODEL_TAG = "QueryModel";

    public static final String DBFLOW_TABLE_ADAPTER = "QueryModelAdapter";

    public TypeName databaseTypeName;

    public boolean allFields;

    public String adapterName;

    public boolean implementsLoadFromCursorListener = false;

    MethodDefinition[] methods;

    public QueryModelDefinition(Element typeElement,
                                ProcessorManager processorManager) {
        super(typeElement, processorManager);

        ModelContainer containerKey = element.getAnnotation(ModelContainer.class);
        boolean putDefaultValue = containerKey != null && containerKey.putDefault();

        QueryModel queryModel = typeElement.getAnnotation(QueryModel.class);
        if (queryModel != null) {
            try {
                queryModel.database();
            } catch (MirroredTypeException mte) {
                databaseTypeName = TypeName.get(mte.getTypeMirror());
            }

            databaseDefinition = manager.getDatabaseWriter(databaseTypeName);
            setOutputClassName(databaseDefinition.classSeparator + DBFLOW_QUERY_MODEL_TAG);
            allFields = queryModel.allFields();
            adapterName = getModelClassName() + databaseDefinition.classSeparator + DBFLOW_TABLE_ADAPTER;

        }

        processorManager.addModelToDatabase(elementClassName, databaseTypeName);

        if (element instanceof TypeElement) {
            implementsLoadFromCursorListener = ProcessorUtils
                    .implementsClass(manager.getProcessingEnvironment(), ClassNames.LOAD_FROM_CURSOR_LISTENER.toString(),
                            (TypeElement) element);
        }


        methods = new MethodDefinition[]{
                new LoadFromCursorMethod(this, false, implementsLoadFromCursorListener, putDefaultValue)
        };

        if (typeElement instanceof TypeElement) {
            createColumnDefinitions(((TypeElement) typeElement));
        }
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columnDefinition.addPropertyDefinition(typeBuilder, elementClassName);
        }
    }

    @Override
    protected void createColumnDefinitions(TypeElement typeElement) {
        List<? extends Element> variableElements = ElementUtility.getAllElements(typeElement, manager);
        ColumnValidator columnValidator = new ColumnValidator();
        for (Element variableElement : variableElements) {

            // no private static or final fields
            boolean isValidColumn = allFields && (variableElement.getKind().isField() &&
                    !variableElement.getModifiers().contains(Modifier.STATIC) &&
                    !variableElement.getModifiers().contains(Modifier.PRIVATE) &&
                    !variableElement.getModifiers().contains(Modifier.FINAL));

            if (variableElement.getAnnotation(Column.class) != null || isValidColumn) {

                // package private, will generate helper
                boolean isPackagePrivate = ElementUtility.isPackagePrivate(element);
                boolean isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, element, this.element);

                ColumnDefinition columnDefinition = new ColumnDefinition(manager, variableElement, this, isPackagePrivateNotInSamePackage);
                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition);

                    if (isPackagePrivate) {
                        packagePrivateList.add(columnDefinition);
                    }
                }
            }
        }
    }

    @Override
    public List<ColumnDefinition> getPrimaryColumnDefinitions() {
        // Shouldn't include any
        return new ArrayList<>();
    }

    @Override
    public ClassName getPropertyClassName() {
        return outputClassName;
    }

    public ClassName getAdapterClassName() {
        return ClassName.get(packageName, adapterName);
    }

    public void writeAdapter(ProcessingEnvironment processingEnvironment) throws IOException {

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(adapterName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassNames.QUERY_MODEL_ADAPTER, elementClassName));

        CustomTypeConverterPropertyMethod customTypeConverterPropertyMethod = new CustomTypeConverterPropertyMethod(this);
        customTypeConverterPropertyMethod.addToType(typeBuilder);

        CodeBlock.Builder constructorCode = CodeBlock.builder();

        customTypeConverterPropertyMethod.addCode(constructorCode);

        InternalAdapterHelper.writeGetModelClass(typeBuilder, elementClassName);

        typeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(ClassNames.DATABASE_HOLDER, "holder")
                .addCode(constructorCode.build())
                .addModifiers(Modifier.PUBLIC).build());

        for (MethodDefinition method : methods) {
            MethodSpec methodSpec = method.getMethodSpec();
            if (methodSpec != null) {
                typeBuilder.addMethod(methodSpec);
            }
        }

        typeBuilder.addMethod(MethodSpec.methodBuilder("newInstance")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(elementClassName)
                .addStatement("return new $T()", elementClassName).build());

        JavaFile javaFile = JavaFile.builder(packageName, typeBuilder.build()).build();
        javaFile.writeTo(processingEnvironment.getFiler());
    }

}
