package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.annotation.ModelViewQuery;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.CustomTypeConverterPropertyMethod;
import com.raizlabs.android.dbflow.processor.definition.method.ExistenceMethod;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.PrimaryConditionMethod;
import com.raizlabs.android.dbflow.processor.handler.DatabaseHandler;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ElementUtility;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.raizlabs.android.dbflow.processor.validator.ColumnValidator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * Description: Used in writing ModelViewAdapters
 */
public class ModelViewDefinition extends BaseTableDefinition {

    private static final String DBFLOW_MODEL_VIEW_TAG = "View";

    private static final String TABLE_VIEW_TAG = "ViewTable";

    final boolean implementsLoadFromCursorListener;

    public TypeName databaseName;

    private String queryFieldName;

    private String name;

    private ClassName modelReferenceClass;

    private MethodDefinition[] methods;

    public String viewTableName;

    public boolean allFields;

    public ModelViewDefinition(ProcessorManager manager, Element element) {
        super(element, manager);

        ModelContainer containerKey = element.getAnnotation(ModelContainer.class);
        boolean putDefaultValue = containerKey != null && containerKey.putDefault();

        ModelView modelView = element.getAnnotation(ModelView.class);
        if (modelView != null) {
            try {
                modelView.database();
            } catch (MirroredTypeException mte) {
                this.databaseName = TypeName.get(mte.getTypeMirror());
            }
            allFields = modelView.allFields();

            this.name = modelView.name();
            if (name == null || name.isEmpty()) {
                name = getModelClassName();
            }
        }

        DeclaredType typeAdapterInterface = null;
        final DeclaredType modelViewType = manager.getTypeUtils().getDeclaredType(
            manager.getElements().getTypeElement(ClassNames.MODEL_VIEW.toString()),
            manager.getTypeUtils().getWildcardType(manager.getElements().getTypeElement(ClassNames.MODEL.toString()).asType(), null)
        );


        for (TypeMirror superType : manager.getTypeUtils().directSupertypes(element.asType())) {
            if (manager.getTypeUtils().isAssignable(superType, modelViewType)) {
                typeAdapterInterface = (DeclaredType) superType;
                break;
            }
        }

        if (typeAdapterInterface != null) {
            final List<? extends TypeMirror> typeArguments = typeAdapterInterface.getTypeArguments();
            modelReferenceClass = ClassName.get(manager.getElements().getTypeElement(typeArguments.get(0).toString()));
        }

        if (element instanceof TypeElement) {
            implementsLoadFromCursorListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                ClassNames.LOAD_FROM_CURSOR_LISTENER.toString(), (TypeElement) element);
        } else {
            implementsLoadFromCursorListener = false;
        }

        methods = new MethodDefinition[]{
            new LoadFromCursorMethod(this, false, implementsLoadFromCursorListener, putDefaultValue),
            new ExistenceMethod(this, false),
            new PrimaryConditionMethod(this, false)
        };
    }

    @Override
    public void prepareForWrite() {
        classElementLookUpMap.clear();
        columnDefinitions.clear();
        queryFieldName = null;

        ModelView modelView = element.getAnnotation(ModelView.class);
        if (modelView != null) {
            databaseDefinition = manager.getDatabaseWriter(databaseName);
            this.viewTableName = getModelClassName() + databaseDefinition.classSeparator + TABLE_VIEW_TAG;

            setOutputClassName(databaseDefinition.classSeparator + DBFLOW_MODEL_VIEW_TAG);

            if (typeElement != null) {
                createColumnDefinitions(typeElement);
            }
        }
    }

    @Override
    protected void createColumnDefinitions(TypeElement typeElement) {
        List<? extends Element> variableElements = ElementUtility.getAllElements(typeElement, manager);

        for (Element element : variableElements) {
            classElementLookUpMap.put(element.getSimpleName().toString(), element);
        }

        ColumnValidator columnValidator = new ColumnValidator();
        for (Element variableElement : variableElements) {

            boolean isValidAllFields = ElementUtility.isValidAllFields(allFields, element);

            if (variableElement.getAnnotation(Column.class) != null || isValidAllFields) {

                // package private, will generate helper
                boolean isPackagePrivate = ElementUtility.isPackagePrivate(element);
                boolean isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, element, this.element);

                ColumnDefinition columnDefinition = new ColumnDefinition(manager, variableElement, this, isPackagePrivateNotInSamePackage);
                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition);

                    if (isPackagePrivate) {
                        columnDefinitions.add(columnDefinition);
                    }
                }

                if (columnDefinition.isPrimaryKey || columnDefinition instanceof ForeignKeyColumnDefinition
                    || columnDefinition.isPrimaryKeyAutoIncrement() || columnDefinition.isRowId) {
                    manager.logError("ModelViews cannot have primary or foreign keys");
                }
            } else if (variableElement.getAnnotation(ModelViewQuery.class) != null) {
                if (!StringUtils.isNullOrEmpty(queryFieldName)) {
                    manager.logError("Found duplicate ");
                }
                if (!variableElement.getModifiers().contains(Modifier.PUBLIC)) {
                    manager.logError("The ModelViewQuery must be public");
                }
                if (!variableElement.getModifiers().contains(Modifier.STATIC)) {
                    manager.logError("The ModelViewQuery must be static");
                }

                if (!variableElement.getModifiers().contains(Modifier.FINAL)) {
                    manager.logError("The ModelViewQuery must be final");
                }

                TypeElement element = manager.getElements().getTypeElement(variableElement.asType().toString());
                if (!ProcessorUtils.implementsClass(manager.getProcessingEnvironment(), ClassNames.QUERY.toString(), element)) {
                    manager.logError("The field %1s must implement %1s", variableElement.getSimpleName().toString(), ClassNames.QUERY.toString());
                }

                queryFieldName = variableElement.getSimpleName().toString();
            }
        }

        if (StringUtils.isNullOrEmpty(queryFieldName)) {
            manager.logError("%1s is missing the @ModelViewQuery field.", elementClassName);
        }
    }

    @Override
    public List<ColumnDefinition> getPrimaryColumnDefinitions() {
        return getColumnDefinitions();
    }

    @Override
    public ClassName getPropertyClassName() {
        return ClassName.get(packageName, viewTableName);
    }

    @Override
    protected TypeName getExtendsClass() {
        return ParameterizedTypeName.get(ClassNames.MODEL_VIEW_ADAPTER, modelReferenceClass, elementClassName);
    }

    public void writeViewTable() throws IOException {

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(viewTableName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addField(FieldSpec.builder(ClassName.get(String.class), "VIEW_NAME", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", name).build());

        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columnDefinition.addPropertyDefinition(typeBuilder, elementClassName);
        }

        JavaFile file = JavaFile.builder(packageName, typeBuilder.build()).build();
        file.writeTo(manager.getProcessingEnvironment().getFiler());
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {

        CustomTypeConverterPropertyMethod customTypeConverterPropertyMethod = new CustomTypeConverterPropertyMethod(this);
        customTypeConverterPropertyMethod.addToType(typeBuilder);

        CodeBlock.Builder constructorCode = CodeBlock.builder();
        constructorCode.addStatement("super(databaseDefinition)");
        customTypeConverterPropertyMethod.addCode(constructorCode);

        typeBuilder.addMethod(MethodSpec.constructorBuilder()
            .addParameter(ClassNames.DATABASE_HOLDER, "holder")
            .addParameter(ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME, "databaseDefinition")
            .addCode(constructorCode.build())
            .addModifiers(Modifier.PUBLIC).build());

        for (MethodDefinition method : methods) {
            MethodSpec methodSpec = method.getMethodSpec();
            if (methodSpec != null) {
                typeBuilder.addMethod(methodSpec);
            }
        }

        InternalAdapterHelper.writeGetModelClass(typeBuilder, elementClassName);

        typeBuilder.addMethod(MethodSpec.methodBuilder("getCreationQuery")
            .addAnnotation(Override.class)
            .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
            .addStatement("return $T.$L.getQuery()", elementClassName, queryFieldName)
            .returns(ClassName.get(String.class)).build());

        typeBuilder.addMethod(MethodSpec.methodBuilder("getViewName")
            .addAnnotation(Override.class)
            .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
            .addStatement("return $S", name)
            .returns(ClassName.get(String.class)).build());

        typeBuilder.addMethod(MethodSpec.methodBuilder("newInstance")
            .addAnnotation(Override.class)
            .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
            .addStatement("return new $T()", elementClassName)
            .returns(elementClassName).build());
    }
}
