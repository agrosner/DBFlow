package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.raizlabs.android.dbflow.processor.validator.ColumnValidator;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.raizlabs.android.dbflow.processor.writer.LoadCursorWriter;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Description:
 */
public class QueryModelDefinition extends BaseTableDefinition {

    private static final String DBFLOW_QUERY_MODEL_TAG = "QueryModel";

    public static final String DBFLOW_TABLE_ADAPTER = "QueryModelAdapter";

    public String databaseName;

    public boolean allFields;

    public String adapterName;

    public boolean implementsLoadFromCursorListener = false;

    FlowWriter[] mMethodWriters;

    public QueryModelDefinition(Element typeElement,
                                ProcessorManager processorManager) {
        super(typeElement, processorManager);


        QueryModel queryModel = typeElement.getAnnotation(QueryModel.class);
        databaseName = queryModel.databaseName();
        databaseWriter = manager.getDatabaseWriter(databaseName);
        allFields = queryModel.allFields();
        adapterName = getModelClassName() + databaseWriter.classSeparator + DBFLOW_TABLE_ADAPTER;

        processorManager.addModelToDatabase(getQualifiedModelClassName(), databaseName);

        implementsLoadFromCursorListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                                                                          Classes.LOAD_FROM_CURSOR_LISTENER,
                                                                          (TypeElement) element);

        setOutputClassName(databaseWriter.classSeparator + DBFLOW_QUERY_MODEL_TAG);

        mMethodWriters = new FlowWriter[]{
                new LoadCursorWriter(this, false, implementsLoadFromCursorListener)
        };
        
        createColumnDefinitions(((TypeElement) typeElement));
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {
        javaWriter.emitEmptyLine();
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columnDefinition.write(javaWriter);
        }
    }

    @Override
    protected void createColumnDefinitions(TypeElement typeElement) {
        List<? extends Element> variableElements = manager.getElements().getAllMembers(typeElement);
        ColumnValidator columnValidator = new ColumnValidator();
        for (Element variableElement : variableElements) {

            // no private static or final fields
            boolean isValidColumn = allFields && (variableElement.getKind().isField() &&
                                                  !variableElement.getModifiers().contains(Modifier.STATIC) &&
                                                  !variableElement.getModifiers().contains(Modifier.PRIVATE) &&
                                                  !variableElement.getModifiers().contains(Modifier.FINAL));

            if (variableElement.getAnnotation(Column.class) != null || isValidColumn) {
                ColumnDefinition columnDefinition = new ColumnDefinition(manager, (VariableElement) variableElement);
                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition);
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
    public String getTableSourceClassName() {
        return definitionClassName;
    }

    public String getQualifiedModelClassName() {
        return packageName + "." + getModelClassName();
    }

    public String getQualifiedAdapterName() {
        return packageName + "." + adapterName;
    }

    public void writeAdapter(ProcessingEnvironment processingEnvironment) throws IOException {
        JavaWriter javaWriter = new JavaWriter(
                processingEnvironment.getFiler().createSourceFile(getQualifiedAdapterName()).openWriter());

        javaWriter.emitPackage(packageName);
        javaWriter.emitImports(Classes.QUERY_MODEL_ADAPTER,
                               Classes.FLOW_MANAGER,
                               Classes.CURSOR
        );
        javaWriter.emitSingleLineComment("This table belongs to the %1s database", databaseName);
        javaWriter.beginType(adapterName, "class", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL),
                             "QueryModelAdapter<" + element.getSimpleName() + ">");

        for (FlowWriter writer : mMethodWriters) {
            writer.write(javaWriter);
        }

        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return new %1s()", getQualifiedModelClassName());
            }
        }, getQualifiedModelClassName(), "newInstance", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL));

        javaWriter.endType();
        javaWriter.close();
    }

}
