package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.DBFlowProcessor;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.handler.DatabaseHandler;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.writer.DatabaseWriter;
import com.raizlabs.android.dbflow.processor.writer.ExistenceWriter;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.raizlabs.android.dbflow.processor.writer.LoadCursorWriter;
import com.raizlabs.android.dbflow.processor.writer.WhereQueryWriter;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Description: Used in writing ModelViewAdapters
 */
public class ModelViewDefinition extends BaseTableDefinition implements FlowWriter {

    private static final String DBFLOW_MODEL_VIEW_TAG = "View";

    private static final String TABLE_VIEW_TAG = "ViewTable";

    final boolean implementsLoadFromCursorListener;

    public String databaseName;

    private String query;

    private String name;

    private TypeElement modelReferenceClass;

    private FlowWriter[] mMethodWriters;

    private String viewTableName;

    public ModelViewDefinition(ProcessorManager manager, Element element) {
        super(element, manager);

        ModelView modelView = element.getAnnotation(ModelView.class);
        this.query = modelView.query();
        this.databaseName = modelView.databaseName();

        databaseWriter = manager.getDatabaseWriter(databaseName);
        this.viewTableName = getModelClassName() + databaseWriter.classSeparator + TABLE_VIEW_TAG;

        setDefinitionClassName(databaseWriter.classSeparator + DBFLOW_MODEL_VIEW_TAG);

        this.name = modelView.name();
        if (name == null || name.isEmpty()) {
            name = getModelClassName();
        }

        DeclaredType typeAdapterInterface = null;
        final DeclaredType modelViewType = manager.getTypeUtils().getDeclaredType(
                manager.getElements().getTypeElement(Classes.MODEL_VIEW),
                manager.getTypeUtils().getWildcardType(manager.getElements().getTypeElement(Classes.MODEL).asType(), null)
        );


        for (TypeMirror superType : manager.getTypeUtils().directSupertypes(element.asType())) {
            if (manager.getTypeUtils().isAssignable(superType, modelViewType)) {
                typeAdapterInterface = (DeclaredType) superType;
                break;
            }
        }

        if (typeAdapterInterface != null) {
            final List<? extends TypeMirror> typeArguments = typeAdapterInterface.getTypeArguments();
            modelReferenceClass = manager.getElements().getTypeElement(typeArguments.get(0).toString());
        }

        createColumnDefinitions((TypeElement) element);

        implementsLoadFromCursorListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                Classes.LOAD_FROM_CURSOR_LISTENER, (TypeElement) element);

        mMethodWriters = new FlowWriter[]{
                new LoadCursorWriter(this, false, implementsLoadFromCursorListener),
                new ExistenceWriter(this, false),
                new WhereQueryWriter(this, false)
        };
    }

    @Override
    protected void createColumnDefinitions(TypeElement typeElement) {
        List<? extends Element> variableElements = manager.getElements().getAllMembers(typeElement);
        for (Element variableElement : variableElements) {
            if (variableElement.getAnnotation(Column.class) != null) {
                ColumnDefinition columnDefinition = new ColumnDefinition(manager, (VariableElement) variableElement);
                columnDefinitions.add(columnDefinition);

                if (columnDefinition.isPrimaryKey || columnDefinition.isForeignKey || columnDefinition.isPrimaryKeyAutoIncrement) {
                    manager.getMessager().printMessage(Diagnostic.Kind.ERROR, "ModelViews cannot have primary or foreign keys");
                }
            }
        }
    }

    @Override
    public List<ColumnDefinition> getPrimaryColumnDefinitions() {
        return getColumnDefinitions();
    }

    public String getFullyQualifiedModelClassName() {
        return packageName + "." + getModelClassName();
    }

    @Override
    public String getTableSourceClassName() {
        return name + databaseWriter.classSeparator + TABLE_VIEW_TAG;
    }

    @Override
    protected String[] getImports() {
        return new String[]{
                Classes.CURSOR, Classes.SELECT, Classes.CONDITION_QUERY_BUILDER,
                Classes.CONDITION
        };
    }

    @Override
    protected String getExtendsClass() {
        return String.format("%1s<%1s,%1s>", Classes.MODEL_VIEW_ADAPTER, modelReferenceClass, getModelClassName());
    }

    public void writeViewTable() throws IOException {
        JavaWriter javaWriter = new JavaWriter(manager.getProcessingEnvironment().getFiler()
                .createSourceFile(packageName + "." + viewTableName).openWriter());
        javaWriter.emitPackage(packageName);
        javaWriter.beginType(viewTableName, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));
        javaWriter.emitEmptyLine();
        javaWriter.emitField("String", "VIEW_NAME", Sets.newHashSet(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL), "\"" + name + "\"");
        javaWriter.emitEmptyLine();
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columnDefinition.write(javaWriter);
        }
        javaWriter.endType();
        javaWriter.close();
    }

    @Override
    public void onWriteDefinition(JavaWriter javaWriter) throws IOException {
        for (FlowWriter writer : mMethodWriters) {
            writer.write(javaWriter);
        }

        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return \"%1s\"", query);
            }
        }, "String", "getCreationQuery", DatabaseHandler.METHOD_MODIFIERS);

        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return \"%1s\"", name);
            }
        }, "String", "getViewName", DatabaseHandler.METHOD_MODIFIERS);

        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return new %1s()", getFullyQualifiedModelClassName());
            }
        }, getFullyQualifiedModelClassName(), "newInstance", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));
    }

}
