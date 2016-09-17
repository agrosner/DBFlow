package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.PackagePrivateAccess;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ElementUtility;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Description: Used to write Models and ModelViews
 */
public abstract class BaseTableDefinition extends BaseDefinition {

    protected List<ColumnDefinition> columnDefinitions;
    protected Map<ClassName, List<ColumnDefinition>> associatedTypeConverters = new HashMap<>();
    protected Map<ClassName, List<ColumnDefinition>> globalTypeConverters = new HashMap<>();
    protected final List<ColumnDefinition> packagePrivateList = Lists.newArrayList();

    public boolean orderedCursorLookUp;
    public boolean assignDefaultValuesFromCursor = true;

    public Map<String, Element> classElementLookUpMap = new HashMap<>();

    private String modelClassName;
    public DatabaseDefinition databaseDefinition;

    public BaseTableDefinition(Element typeElement, ProcessorManager processorManager) {
        super(typeElement, processorManager);
        this.modelClassName = typeElement.getSimpleName().toString();
        columnDefinitions = new ArrayList<>();
    }

    protected abstract void createColumnDefinitions(TypeElement typeElement);

    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public abstract List<ColumnDefinition> getPrimaryColumnDefinitions();

    public abstract ClassName getPropertyClassName();

    public abstract void prepareForWrite();

    public TypeName getParameterClassName() {
        return elementClassName;
    }

    public String addColumnForCustomTypeConverter(ColumnDefinition columnDefinition, ClassName typeConverterName) {
        List<ColumnDefinition> columnDefinitions = associatedTypeConverters.get(typeConverterName);
        if (columnDefinitions == null) {
            columnDefinitions = new ArrayList<>();
            associatedTypeConverters.put(typeConverterName, columnDefinitions);
        }
        columnDefinitions.add(columnDefinition);

        return "typeConverter" + typeConverterName.simpleName();
    }

    public String addColumnForTypeConverter(ColumnDefinition columnDefinition, ClassName typeConverterName) {
        List<ColumnDefinition> columnDefinitions = globalTypeConverters.get(typeConverterName);
        if (columnDefinitions == null) {
            columnDefinitions = new ArrayList<>();
            globalTypeConverters.put(typeConverterName, columnDefinitions);
        }
        columnDefinitions.add(columnDefinition);

        return "global_typeConverter" + typeConverterName.simpleName();
    }


    public void writePackageHelper(ProcessingEnvironment processingEnvironment) throws IOException {
        int count = 0;

        if (!packagePrivateList.isEmpty()) {
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(elementClassName.simpleName() + databaseDefinition.classSeparator + "Helper")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            for (ColumnDefinition columnDefinition : packagePrivateList) {
                String helperClassName = manager.getElements().getPackageOf(columnDefinition.element).toString() + "." + ClassName.get((TypeElement) columnDefinition.element.getEnclosingElement()).simpleName()
                        + databaseDefinition.classSeparator + "Helper";
                if (columnDefinition instanceof ForeignKeyColumnDefinition) {
                    TableDefinition tableDefinition = databaseDefinition.getHolderDefinition()
                            .tableDefinitionMap
                            .get(((ForeignKeyColumnDefinition) columnDefinition).referencedTableClassName);
                    if (tableDefinition != null) {
                        helperClassName = manager.getElements().getPackageOf(tableDefinition.element).toString() + "." + ClassName.get((TypeElement) tableDefinition.element).simpleName()
                                + databaseDefinition.classSeparator + "Helper";
                    }
                }
                ClassName className = ClassName.bestGuess(helperClassName);

                if (PackagePrivateAccess.containsColumn(className, columnDefinition.columnName)) {

                    MethodSpec.Builder method = MethodSpec.methodBuilder("get" + StringUtils.capitalize(columnDefinition.columnName))
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .addParameter(elementTypeName, ModelUtils.getVariable())
                            .returns(columnDefinition.elementTypeName);
                    boolean samePackage = ElementUtility.isInSamePackage(manager, columnDefinition.element, this.element);

                    if (samePackage) {
                        method.addStatement("return $L.$L", ModelUtils.getVariable(), columnDefinition.elementName);
                    } else {
                        method.addStatement("return $T.get$L($L)", className, StringUtils.capitalize(columnDefinition.columnName), ModelUtils.getVariable());
                    }

                    typeBuilder.addMethod(method.build());

                    method = MethodSpec.methodBuilder("set" + StringUtils.capitalize(columnDefinition.columnName))
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .addParameter(elementTypeName, ModelUtils.getVariable())
                            .addParameter(columnDefinition.elementTypeName, "var");

                    if (samePackage) {
                        method.addStatement("$L.$L = $L", ModelUtils.getVariable(), columnDefinition.elementName, "var");
                    } else {

                        method.addStatement("$T.set$L($L, $L)", className, StringUtils.capitalize(columnDefinition.columnName), ModelUtils.getVariable(), "var");
                    }
                    typeBuilder.addMethod(method.build());
                    count++;
                }
            }

            // only write class if we have referenced fields.
            if (count > 0) {
                JavaFile.Builder javaFileBuilder = JavaFile.builder(packageName, typeBuilder.build());
                javaFileBuilder.build().writeTo(processingEnvironment.getFiler());
            }
        }
    }


    public Map<ClassName, List<ColumnDefinition>> getAssociatedTypeConverters() {
        return associatedTypeConverters;
    }

    public Map<ClassName, List<ColumnDefinition>> getGlobalTypeConverters() {
        return globalTypeConverters;
    }

    public boolean hasAutoIncrement() {
        return false;
    }

    public boolean hasRowID() {
        return false;
    }

    public ColumnDefinition getAutoIncrementColumn() {
        return null;
    }

    public String getModelClassName() {
        return modelClassName;
    }

}
