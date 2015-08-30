package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.ExistenceMethod;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.PrimaryConditionClause;
import com.raizlabs.android.dbflow.processor.handler.DatabaseHandler;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ClassName;
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
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Description: Used in writing ModelViewAdapters
 */
public class ModelViewDefinition extends BaseTableDefinition {

    private static final String DBFLOW_MODEL_VIEW_TAG = "View";

    private static final String TABLE_VIEW_TAG = "ViewTable";

    final boolean implementsLoadFromCursorListener;

    public String databaseName;

    private String query;

    private String name;

    private ClassName modelReferenceClass;

    private MethodDefinition[] methods;

    private String viewTableName;

    public ModelViewDefinition(ProcessorManager manager, Element element) {
        super(element, manager);

        ModelView modelView = element.getAnnotation(ModelView.class);
        this.query = modelView.query();
        this.databaseName = modelView.databaseName();

        databaseDefinition = manager.getDatabaseWriter(databaseName);
        this.viewTableName = getModelClassName() + databaseDefinition.classSeparator + TABLE_VIEW_TAG;

        setOutputClassName(databaseDefinition.classSeparator + DBFLOW_MODEL_VIEW_TAG);

        this.name = modelView.name();
        if (name == null || name.isEmpty()) {
            name = getModelClassName();
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

        createColumnDefinitions((TypeElement) element);

        implementsLoadFromCursorListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                ClassNames.LOAD_FROM_CURSOR_LISTENER.toString(), (TypeElement) element);

        methods = new MethodDefinition[]{
                new LoadFromCursorMethod(this, false, implementsLoadFromCursorListener),
                new ExistenceMethod(this, false),
                new PrimaryConditionClause(this, false)
        };
    }

    @Override
    protected void createColumnDefinitions(TypeElement typeElement) {
        List<? extends Element> variableElements = manager.getElements().getAllMembers(typeElement);
        for (Element variableElement : variableElements) {
            if (variableElement.getAnnotation(Column.class) != null) {
                ColumnDefinition columnDefinition = new ColumnDefinition(manager, variableElement);
                columnDefinitions.add(columnDefinition);

                if (columnDefinition.isPrimaryKey || columnDefinition instanceof ForeignKeyColumnDefinition || columnDefinition.isPrimaryKeyAutoIncrement) {
                    manager.getMessager().printMessage(Diagnostic.Kind.ERROR, "ModelViews cannot have primary or foreign keys");
                }
            }
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
            columnDefinition.addPropertyDefinition(typeBuilder);
        }

        JavaFile file = JavaFile.builder(packageName, typeBuilder.build()).build();
        file.writeTo(manager.getProcessingEnvironment().getFiler());
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {
        for (MethodDefinition method : methods) {
            MethodSpec methodSpec = method.getMethodSpec();
            if (methodSpec != null) {
                typeBuilder.addMethod(methodSpec);
            }
        }

        typeBuilder.addMethod(MethodSpec.methodBuilder("getCreationQuery")
                .addAnnotation(Override.class)
                .addModifiers(DatabaseHandler.METHOD_MODIFIERS)
                .addStatement("return $S", query)
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
