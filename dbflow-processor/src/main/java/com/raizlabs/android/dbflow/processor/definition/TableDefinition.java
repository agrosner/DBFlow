package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.IndexGroup;
import com.raizlabs.android.dbflow.annotation.InheritedColumn;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.DefinitionUtils;
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.BindToContentValuesMethod;
import com.raizlabs.android.dbflow.processor.definition.method.BindToStatementMethod;
import com.raizlabs.android.dbflow.processor.definition.method.CreationQueryMethod;
import com.raizlabs.android.dbflow.processor.definition.method.CustomTypeConverterPropertyMethod;
import com.raizlabs.android.dbflow.processor.definition.method.ExistenceMethod;
import com.raizlabs.android.dbflow.processor.definition.method.InsertStatementQueryMethod;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.OneToManyDeleteMethod;
import com.raizlabs.android.dbflow.processor.definition.method.OneToManySaveMethod;
import com.raizlabs.android.dbflow.processor.definition.method.PrimaryConditionMethod;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.validator.ColumnValidator;
import com.raizlabs.android.dbflow.processor.validator.OneToManyValidator;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description: Used in writing ModelAdapters
 */
public class TableDefinition extends BaseTableDefinition {

    public static final String DBFLOW_TABLE_TAG = "Table";

    public static final String DBFLOW_TABLE_ADAPTER = "Adapter";

    public String tableName;

    public String adapterName;

    public TypeName databaseTypeName;

    public String insertConflictActionName;

    public String updateConflictActionName;

    public List<ColumnDefinition> primaryColumnDefinitions;
    public List<ForeignKeyColumnDefinition> foreignKeyDefinitions;
    public List<UniqueGroupsDefinition> uniqueGroupsDefinitions;
    public List<IndexGroupsDefinition> indexGroupsDefinitions;

    public ColumnDefinition autoIncrementDefinition;

    public boolean hasAutoIncrement = false;

    public boolean implementsContentValuesListener = false;

    public boolean implementsSqlStatementListener = false;

    public boolean implementsLoadFromCursorListener = false;

    private final MethodDefinition[] methods;

    public boolean hasCachingId = false;

    public boolean allFields = false;
    public boolean useIsForPrivateBooleans;

    public Map<String, ColumnDefinition> mColumnMap = Maps.newHashMap();

    public Map<Integer, List<ColumnDefinition>> columnUniqueMap = Maps.newHashMap();

    public List<OneToManyDefinition> oneToManyDefinitions = new ArrayList<>();

    public Map<String, InheritedColumn> inheritedColumnMap = new HashMap<>();

    public TableDefinition(ProcessorManager manager, TypeElement element) {
        super(element, manager);

        Table table = element.getAnnotation(Table.class);
        this.tableName = table.name();
        try {
            table.database();
        } catch (MirroredTypeException mte) {
            databaseTypeName = TypeName.get(mte.getTypeMirror());
        }

        databaseDefinition = manager.getDatabaseWriter(databaseTypeName);
        if (databaseDefinition == null) {
            manager.logError("Databasewriter was null for : " + tableName);
        }

        setOutputClassName(databaseDefinition.classSeparator + DBFLOW_TABLE_TAG);
        this.adapterName = getModelClassName() + databaseDefinition.classSeparator + DBFLOW_TABLE_ADAPTER;


        // globular default
        ConflictAction insertConflict = table.insertConflict();
        if (insertConflict.equals(ConflictAction.NONE) && !databaseDefinition.insertConflict.equals(ConflictAction.NONE)) {
            insertConflict = databaseDefinition.insertConflict;
        }

        ConflictAction updateConflict = table.updateConflict();
        if (updateConflict.equals(ConflictAction.NONE) && !databaseDefinition.updateConflict.equals(ConflictAction.NONE)) {
            updateConflict = databaseDefinition.updateConflict;
        }

        insertConflictActionName = insertConflict.equals(ConflictAction.NONE) ? ""
                : insertConflict.name();
        updateConflictActionName = updateConflict.equals(ConflictAction.NONE) ? ""
                : updateConflict.name();

        allFields = table.allFields();
        useIsForPrivateBooleans = table.useIsForPrivateBooleans();

        manager.addModelToDatabase(elementClassName, databaseTypeName);

        if (tableName == null || tableName.isEmpty()) {
            tableName = element.getSimpleName().toString();
        }
        primaryColumnDefinitions = new ArrayList<>();
        foreignKeyDefinitions = new ArrayList<>();
        uniqueGroupsDefinitions = new ArrayList<>();
        indexGroupsDefinitions = new ArrayList<>();

        InheritedColumn[] inheritedColumns = table.inheritedColumns();
        for (InheritedColumn inheritedColumn : inheritedColumns) {
            if (inheritedColumnMap.containsKey(inheritedColumn.fieldName())) {
                manager.logError("A duplicate inherited column with name %1s was found for %1s", inheritedColumn.fieldName(), tableName);
            }
            inheritedColumnMap.put(inheritedColumn.fieldName(), inheritedColumn);
        }

        createColumnDefinitions(element);

        UniqueGroup[] groups = table.uniqueColumnGroups();
        Set<Integer> uniqueNumbersSet = new HashSet<>();
        for (UniqueGroup uniqueGroup : groups) {
            if (uniqueNumbersSet.contains(uniqueGroup.groupNumber())) {
                manager.logError("A duplicate unique group with number %1s was found for %1s", uniqueGroup.groupNumber(), tableName);
            }
            UniqueGroupsDefinition definition = new UniqueGroupsDefinition(manager, uniqueGroup);
            for (ColumnDefinition columnDefinition : getColumnDefinitions()) {
                if (columnDefinition.uniqueGroups.contains(definition.number)) {
                    definition.addColumnDefinition(columnDefinition);
                }
            }
            uniqueGroupsDefinitions.add(definition);
            uniqueNumbersSet.add(uniqueGroup.groupNumber());
        }

        IndexGroup[] indexGroups = table.indexGroups();
        uniqueNumbersSet = new HashSet<>();
        for (IndexGroup indexGroup : indexGroups) {
            if (uniqueNumbersSet.contains(indexGroup.number())) {
                manager.logError(TableDefinition.class, "A duplicate unique index number %1s was found for %1s", indexGroup.number(), elementName);
            }
            IndexGroupsDefinition definition = new IndexGroupsDefinition(manager, this, indexGroup);
            for (ColumnDefinition columnDefinition : getColumnDefinitions()) {
                if (columnDefinition.indexGroups.contains(definition.indexNumber)) {
                    definition.columnDefinitionList.add(columnDefinition);
                }
            }
            indexGroupsDefinitions.add(definition);
            uniqueNumbersSet.add(indexGroup.number());
        }

        implementsLoadFromCursorListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                ClassNames.LOAD_FROM_CURSOR_LISTENER.toString(), element);

        implementsContentValuesListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                ClassNames.CONTENT_VALUES_LISTENER.toString(), element);

        implementsSqlStatementListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                ClassNames.SQLITE_STATEMENT_LISTENER.toString(), element);

        methods = new MethodDefinition[]{
                new BindToContentValuesMethod(this, true, false, implementsContentValuesListener),
                new BindToContentValuesMethod(this, false, false, implementsContentValuesListener),
                new BindToStatementMethod(this, true, false),
                new BindToStatementMethod(this, false, false),
                new InsertStatementQueryMethod(this, true),
                new InsertStatementQueryMethod(this, false),
                new CreationQueryMethod(this),
                new LoadFromCursorMethod(this, false, implementsLoadFromCursorListener, false),
                new ExistenceMethod(this, false),
                new PrimaryConditionMethod(this, false),
                new OneToManyDeleteMethod(this, false),
                new OneToManySaveMethod(this, false, OneToManySaveMethod.METHOD_SAVE),
                new OneToManySaveMethod(this, false, OneToManySaveMethod.METHOD_INSERT),
                new OneToManySaveMethod(this, false, OneToManySaveMethod.METHOD_UPDATE)
        };

        // single primary key checking for a long or int valued column
        if (getPrimaryColumnDefinitions().size() == 1) {
            ColumnDefinition columnDefinition = getPrimaryColumnDefinitions().get(0);
            if (columnDefinition.isPrimaryKey) {
                hasCachingId = !columnDefinition.hasTypeConverter;
            }
        }
    }

    @Override
    protected void createColumnDefinitions(TypeElement typeElement) {
        List<? extends Element> elements = manager.getElements().getAllMembers(typeElement);
        ColumnValidator columnValidator = new ColumnValidator();
        OneToManyValidator oneToManyValidator = new OneToManyValidator();
        for (Element element : elements) {

            // no private static or final fields for all columns, or any inherited columns here.
            boolean isValidColumn = (allFields && (element.getKind().isField() &&
                    !element.getModifiers().contains(Modifier.STATIC) &&
                    !element.getModifiers().contains(Modifier.PRIVATE) &&
                    !element.getModifiers().contains(Modifier.FINAL)));
            inheritedColumnMap.containsKey(element.getSimpleName().toString());
            if (element.getAnnotation(Column.class) != null || isValidColumn) {
                ColumnDefinition columnDefinition;
                if (element.getAnnotation(ForeignKey.class) != null) {
                    columnDefinition = new ForeignKeyColumnDefinition(manager, this, element);
                } else {
                    columnDefinition = new ColumnDefinition(manager, element, this);
                }
                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition);
                    mColumnMap.put(columnDefinition.columnName, columnDefinition);
                    if (columnDefinition.isPrimaryKey) {
                        primaryColumnDefinitions.add(columnDefinition);
                    } else if (columnDefinition instanceof ForeignKeyColumnDefinition) {
                        foreignKeyDefinitions.add((ForeignKeyColumnDefinition) columnDefinition);
                    } else if (columnDefinition.isPrimaryKeyAutoIncrement) {
                        autoIncrementDefinition = columnDefinition;
                        hasAutoIncrement = true;
                    }

                    if (!columnDefinition.uniqueGroups.isEmpty()) {
                        List<Integer> groups = columnDefinition.uniqueGroups;
                        for (int group : groups) {
                            List<ColumnDefinition> groupList = columnUniqueMap.get(group);
                            if (groupList == null) {
                                groupList = new ArrayList<>();
                                columnUniqueMap.put(group, groupList);
                            }
                            if (!groupList.contains(columnDefinition)) {
                                groupList.add(columnDefinition);
                            }
                        }
                    }
                }
            } else if (element.getAnnotation(OneToMany.class) != null) {
                OneToManyDefinition oneToManyDefinition = new OneToManyDefinition((ExecutableElement) element, manager);
                if (oneToManyValidator.validate(manager, oneToManyDefinition)) {
                    oneToManyDefinitions.add(oneToManyDefinition);
                }
            }
        }
    }

    public ColumnDefinition getAutoIncrementPrimaryKey() {
        return autoIncrementDefinition;
    }

    @Override
    public List<ColumnDefinition> getPrimaryColumnDefinitions() {
        if (getAutoIncrementPrimaryKey() != null) {
            return Lists.newArrayList(getAutoIncrementPrimaryKey());
        } else {
            return primaryColumnDefinitions;
        }
    }

    public ClassName getAdapterClassName() {
        return ClassName.get(packageName, adapterName);
    }

    @Override
    public ClassName getPropertyClassName() {
        return outputClassName;
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {
        MethodSpec.Builder getPropertyForNameMethod = MethodSpec.methodBuilder("getProperty")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "columnName")
                .returns(ClassNames.BASE_PROPERTY);

        getPropertyForNameMethod.addStatement("columnName = $T.quoteIfNeeded(columnName)", ClassName.get(QueryBuilder.class));

        getPropertyForNameMethod.beginControlFlow("switch ($L) ", "columnName");
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columnDefinition.addPropertyDefinition(typeBuilder, elementClassName);
            columnDefinition.addPropertyCase(getPropertyForNameMethod);
        }
        getPropertyForNameMethod.beginControlFlow("default: ");
        getPropertyForNameMethod.addStatement("throw new $T($S)", IllegalArgumentException.class,
                "Invalid column name passed. Ensure you are calling the correct table's column");
        getPropertyForNameMethod.endControlFlow();
        getPropertyForNameMethod.endControlFlow();

        // add index properties here
        for (IndexGroupsDefinition indexGroupsDefinition : indexGroupsDefinitions) {
            typeBuilder.addField(indexGroupsDefinition.getFieldSpec());
        }

        typeBuilder.addMethod(getPropertyForNameMethod.build());
    }

    public void writeAdapter(ProcessingEnvironment processingEnvironment) throws IOException {

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(adapterName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassNames.MODEL_ADAPTER, elementClassName));
        InternalAdapterHelper.writeGetModelClass(typeBuilder, elementClassName);
        InternalAdapterHelper.writeGetTableName(typeBuilder, tableName);

        if (hasAutoIncrement) {
            InternalAdapterHelper.writeUpdateAutoIncrement(typeBuilder, elementClassName,
                    autoIncrementDefinition, false);

            typeBuilder.addMethod(MethodSpec.methodBuilder("getAutoIncrementingId")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(elementClassName, ModelUtils.getVariable(false))
                    .addStatement("return $L", autoIncrementDefinition.getColumnAccessString(false))
                    .returns(ClassName.get(Number.class)).build());

            typeBuilder.addMethod(MethodSpec.methodBuilder("getAutoIncrementingColumnName")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return $S", QueryBuilder.stripQuotes(autoIncrementDefinition.columnName))
                    .returns(ClassName.get(String.class)).build());
        }

        if (hasCachingId) {
            ColumnDefinition cachingColumn = getPrimaryColumnDefinitions().get(0);

            typeBuilder.addMethod(MethodSpec.methodBuilder("hasCachingId")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return $L", true)
                    .returns(TypeName.BOOLEAN).build());

            InternalAdapterHelper.writeGetCachingId(typeBuilder, elementClassName, cachingColumn, false);

            typeBuilder.addMethod(MethodSpec.methodBuilder("getCachingColumnName")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return $S", QueryBuilder.stripQuotes(cachingColumn.columnName))
                    .returns(ClassName.get(String.class)).build());

            typeBuilder.addMethod(MethodSpec.methodBuilder("getCachingIdFromCursorIndex")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ClassNames.CURSOR, LoadFromCursorMethod.PARAM_CURSOR)
                    .addParameter(ClassName.INT, "index")
                    .addStatement("return $L.$L($L)", LoadFromCursorMethod.PARAM_CURSOR,
                            DefinitionUtils.getLoadFromCursorMethodString(cachingColumn.elementTypeName, cachingColumn.columnAccess),
                            "index")
                    .returns(cachingColumn.elementTypeName.box()).build());
        }

        CustomTypeConverterPropertyMethod customTypeConverterPropertyMethod = new CustomTypeConverterPropertyMethod(this);
        customTypeConverterPropertyMethod.addToType(typeBuilder);

        CodeBlock.Builder constructorCode = CodeBlock.builder();

        customTypeConverterPropertyMethod.addCode(constructorCode);

        typeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(ClassNames.DATABASE_HOLDER, "holder")
                .addCode(constructorCode.build())
                .addModifiers(Modifier.PUBLIC).build());

        for (MethodDefinition methodDefinition : methods) {
            MethodSpec spec = methodDefinition.getMethodSpec();
            if (spec != null) {
                typeBuilder.addMethod(spec);
            }
        }

        typeBuilder.addMethod(MethodSpec.methodBuilder("newInstance")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return new $T()", elementClassName)
                .returns(elementClassName)
                .build());

        typeBuilder.addMethod(MethodSpec.methodBuilder("getProperty")
                .addAnnotation(Override.class)
                .addParameter(ClassName.get(String.class), "name")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return $T.getProperty($L)", outputClassName, "name")
                .returns(ClassNames.BASE_PROPERTY)
                .build());

        if (!updateConflictActionName.isEmpty()) {
            typeBuilder.addMethod(MethodSpec.methodBuilder("getUpdateOnConflictAction")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return $T.$L", ClassNames.CONFLICT_ACTION, updateConflictActionName)
                    .returns(ClassNames.CONFLICT_ACTION)
                    .build());
        }

        if (!insertConflictActionName.isEmpty()) {
            typeBuilder.addMethod(MethodSpec.methodBuilder("getInsertOnConflictAction")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return $T.$L", ClassNames.CONFLICT_ACTION, insertConflictActionName)
                    .returns(ClassNames.CONFLICT_ACTION).build());
        }


        JavaFile.Builder javaFileBuilder = JavaFile.builder(packageName, typeBuilder.build());
        javaFileBuilder.build().writeTo(processingEnvironment.getFiler());

    }
}
