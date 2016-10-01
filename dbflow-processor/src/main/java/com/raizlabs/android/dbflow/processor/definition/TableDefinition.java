package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.IndexGroup;
import com.raizlabs.android.dbflow.annotation.InheritedColumn;
import com.raizlabs.android.dbflow.annotation.InheritedPrimaryKey;
import com.raizlabs.android.dbflow.annotation.ModelCacheField;
import com.raizlabs.android.dbflow.annotation.MultiCacheField;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
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
import com.raizlabs.android.dbflow.processor.utils.ElementUtility;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.raizlabs.android.dbflow.processor.validator.ColumnValidator;
import com.raizlabs.android.dbflow.processor.validator.OneToManyValidator;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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

    public String tableName;

    public TypeName databaseTypeName;

    public String insertConflictActionName;

    public String updateConflictActionName;

    public String primaryKeyConflictActionName;

    public List<ColumnDefinition> primaryColumnDefinitions;
    public List<ForeignKeyColumnDefinition> foreignKeyDefinitions;
    public List<UniqueGroupsDefinition> uniqueGroupsDefinitions;
    public List<IndexGroupsDefinition> indexGroupsDefinitions;

    public ColumnDefinition autoIncrementDefinition;

    public boolean hasAutoIncrement = false;
    public boolean hasRowID = false;

    public boolean implementsContentValuesListener = false;

    public boolean implementsSqlStatementListener = false;

    public boolean implementsLoadFromCursorListener = false;

    private final MethodDefinition[] methods;

    public boolean cachingEnabled = false;
    public int cacheSize;
    public String customCacheFieldName;
    public String customMultiCacheFieldName;

    public boolean allFields = false;
    public boolean useIsForPrivateBooleans;

    public final Map<String, ColumnDefinition> mColumnMap = Maps.newHashMap();

    public Map<Integer, List<ColumnDefinition>> columnUniqueMap = Maps.newHashMap();

    public List<OneToManyDefinition> oneToManyDefinitions = new ArrayList<>();

    public Map<String, InheritedColumn> inheritedColumnMap = new HashMap<>();
    public List<String> inheritedFieldNameList = new ArrayList<>();
    public Map<String, InheritedPrimaryKey> inheritedPrimaryKeyMap = new HashMap<>();

    public TableDefinition(ProcessorManager manager, TypeElement element) {
        super(element, manager);

        primaryColumnDefinitions = new ArrayList<>();
        foreignKeyDefinitions = new ArrayList<>();
        uniqueGroupsDefinitions = new ArrayList<>();
        indexGroupsDefinitions = new ArrayList<>();

        Table table = element.getAnnotation(Table.class);
        if (table != null) {
            this.tableName = table.name();

            if (tableName == null || tableName.isEmpty()) {
                tableName = element.getSimpleName().toString();
            }

            try {
                table.database();
            } catch (MirroredTypeException mte) {
                databaseTypeName = TypeName.get(mte.getTypeMirror());
            }

            cachingEnabled = table.cachingEnabled();
            cacheSize = table.cacheSize();

            orderedCursorLookUp = table.orderedCursorLookUp();
            assignDefaultValuesFromCursor = table.assignDefaultValuesFromCursor();

            allFields = table.allFields();
            useIsForPrivateBooleans = table.useBooleanGetterSetters();

            manager.addModelToDatabase(elementClassName, databaseTypeName);


            InheritedColumn[] inheritedColumns = table.inheritedColumns();
            for (InheritedColumn inheritedColumn : inheritedColumns) {
                if (inheritedFieldNameList.contains(inheritedColumn.fieldName())) {
                    manager.logError("A duplicate inherited column with name %1s was found for %1s", inheritedColumn.fieldName(), tableName);
                }
                inheritedFieldNameList.add(inheritedColumn.fieldName());
                inheritedColumnMap.put(inheritedColumn.fieldName(), inheritedColumn);
            }

            InheritedPrimaryKey[] inheritedPrimaryKeys = table.inheritedPrimaryKeys();
            for (InheritedPrimaryKey inheritedColumn : inheritedPrimaryKeys) {
                if (inheritedFieldNameList.contains(inheritedColumn.fieldName())) {
                    manager.logError("A duplicate inherited column with name %1s was found for %1s", inheritedColumn.fieldName(), tableName);
                }
                inheritedFieldNameList.add(inheritedColumn.fieldName());
                inheritedPrimaryKeyMap.put(inheritedColumn.fieldName(), inheritedColumn);
            }

            implementsLoadFromCursorListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                    ClassNames.LOAD_FROM_CURSOR_LISTENER.toString(), element);

            implementsContentValuesListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                    ClassNames.CONTENT_VALUES_LISTENER.toString(), element);

            implementsSqlStatementListener = ProcessorUtils.implementsClass(manager.getProcessingEnvironment(),
                    ClassNames.SQLITE_STATEMENT_LISTENER.toString(), element);
        }

        methods = new MethodDefinition[]
                {
                        new BindToContentValuesMethod(this, true, false, implementsContentValuesListener),
                        new BindToContentValuesMethod(this, false, false, implementsContentValuesListener),
                        new BindToStatementMethod(this, true, false),
                        new BindToStatementMethod(this, false, false),
                        new InsertStatementQueryMethod(this, true),
                        new InsertStatementQueryMethod(this, false),
                        new CreationQueryMethod(this),
                        new LoadFromCursorMethod(this, implementsLoadFromCursorListener),
                        new ExistenceMethod(this, false),
                        new PrimaryConditionMethod(this, false),
                        new OneToManyDeleteMethod(this, false, false),
                        new OneToManyDeleteMethod(this, false, true),
                        new OneToManySaveMethod(this, false, OneToManySaveMethod.METHOD_SAVE, false),
                        new OneToManySaveMethod(this, false, OneToManySaveMethod.METHOD_INSERT, false),
                        new OneToManySaveMethod(this, false, OneToManySaveMethod.METHOD_UPDATE, false),
                        new OneToManySaveMethod(this, false, OneToManySaveMethod.METHOD_SAVE, true),
                        new OneToManySaveMethod(this, false, OneToManySaveMethod.METHOD_INSERT, true),
                        new OneToManySaveMethod(this, false, OneToManySaveMethod.METHOD_UPDATE, true)
                };
    }

    @Override
    public void prepareForWrite() {
        columnDefinitions = new ArrayList<>();
        mColumnMap.clear();
        classElementLookUpMap.clear();
        autoIncrementDefinition = null;
        primaryColumnDefinitions.clear();
        uniqueGroupsDefinitions.clear();
        indexGroupsDefinitions.clear();
        foreignKeyDefinitions.clear();
        columnUniqueMap.clear();
        oneToManyDefinitions.clear();
        customCacheFieldName = null;
        customMultiCacheFieldName = null;

        Table table = element.getAnnotation(Table.class);
        if (table != null) {
            databaseDefinition = manager.getDatabaseHolderDefinition(databaseTypeName).getDatabaseDefinition();
            if (databaseDefinition == null) {
                manager.logError("DatabaseDefinition was null for : " + tableName + " for db type: " + databaseTypeName);
            }

            setOutputClassName(databaseDefinition.classSeparator + DBFLOW_TABLE_TAG);

            // globular default
            ConflictAction insertConflict = table.insertConflict();
            if (insertConflict.equals(ConflictAction.NONE) && !databaseDefinition.insertConflict.equals(ConflictAction.NONE)) {
                insertConflict = databaseDefinition.insertConflict;
            }

            ConflictAction updateConflict = table.updateConflict();
            if (updateConflict.equals(ConflictAction.NONE) && !databaseDefinition.updateConflict.equals(ConflictAction.NONE)) {
                updateConflict = databaseDefinition.updateConflict;
            }

            ConflictAction primaryKeyConflict = table.primaryKeyConflict();

            insertConflictActionName = insertConflict.equals(ConflictAction.NONE) ? ""
                    : insertConflict.name();
            updateConflictActionName = updateConflict.equals(ConflictAction.NONE) ? ""
                    : updateConflict.name();
            primaryKeyConflictActionName = primaryKeyConflict.equals(ConflictAction.NONE) ? ""
                    : primaryKeyConflict.name();


            createColumnDefinitions(typeElement);

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
        }

    }

    @Override
    public boolean hasAutoIncrement() {
        return hasAutoIncrement;
    }

    @Override
    public boolean hasRowID() {
        return hasRowID;
    }

    @Override
    public ColumnDefinition getAutoIncrementColumn() {
        return autoIncrementDefinition;
    }

    @Override
    protected void createColumnDefinitions(TypeElement typeElement) {
        List<? extends Element> elements = ElementUtility.getAllElements(typeElement, manager);

        for (Element element : elements) {
            classElementLookUpMap.put(element.getSimpleName().toString(), element);
        }

        ColumnValidator columnValidator = new ColumnValidator();
        OneToManyValidator oneToManyValidator = new OneToManyValidator();
        AtomicInteger integer = new AtomicInteger(0);
        for (Element element : elements) {
            // no private static or final fields for all columns, or any inherited columns here.
            boolean isAllFields = ElementUtility.isValidAllFields(allFields, element);

            // package private, will generate helper
            boolean isPackagePrivate = ElementUtility.isPackagePrivate(element);
            boolean isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, element, this.element);

            boolean isForeign = element.getAnnotation(ForeignKey.class) != null;
            boolean isPrimary = element.getAnnotation(PrimaryKey.class) != null;
            boolean isInherited = inheritedColumnMap.containsKey(element.getSimpleName().toString());
            boolean isInheritedPrimaryKey = inheritedPrimaryKeyMap.containsKey(element.getSimpleName().toString());
            if (element.getAnnotation(Column.class) != null || isForeign || isPrimary
                    || isAllFields || isInherited || isInheritedPrimaryKey) {

                ColumnDefinition columnDefinition;
                if (isInheritedPrimaryKey) {
                    InheritedPrimaryKey inherited = inheritedPrimaryKeyMap.get(element.getSimpleName().toString());
                    columnDefinition = new ColumnDefinition(manager, element, this, isPackagePrivateNotInSamePackage,
                            inherited.column(), inherited.primaryKey());
                } else if (isInherited) {
                    InheritedColumn inherited = inheritedColumnMap.get(element.getSimpleName().toString());
                    columnDefinition = new ColumnDefinition(manager, element, this, isPackagePrivateNotInSamePackage,
                            inherited.column(), null);
                } else if (isForeign) {
                    columnDefinition = new ForeignKeyColumnDefinition(manager, this,
                            element, isPackagePrivateNotInSamePackage);
                } else {
                    columnDefinition = new ColumnDefinition(manager, element,
                            this, isPackagePrivateNotInSamePackage);
                }

                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition);
                    mColumnMap.put(columnDefinition.columnName, columnDefinition);
                    if (columnDefinition.isPrimaryKey) {
                        primaryColumnDefinitions.add(columnDefinition);
                    } else if (columnDefinition.isPrimaryKeyAutoIncrement()) {
                        autoIncrementDefinition = columnDefinition;
                        hasAutoIncrement = true;
                    } else if (columnDefinition.isRowId) {
                        autoIncrementDefinition = columnDefinition;
                        hasRowID = true;
                    }

                    if (columnDefinition instanceof ForeignKeyColumnDefinition) {
                        foreignKeyDefinitions.add((ForeignKeyColumnDefinition) columnDefinition);
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

                    if (isPackagePrivate) {
                        packagePrivateList.add(columnDefinition);
                    }
                }
            } else if (element.getAnnotation(OneToMany.class) != null) {
                OneToManyDefinition oneToManyDefinition = new OneToManyDefinition((ExecutableElement) element, manager);
                if (oneToManyValidator.validate(manager, oneToManyDefinition)) {
                    oneToManyDefinitions.add(oneToManyDefinition);
                }
            } else if (element.getAnnotation(ModelCacheField.class) != null) {
                if (!element.getModifiers().contains(Modifier.PUBLIC)) {
                    manager.logError("ModelCacheField must be public from: " + typeElement);
                }
                if (!element.getModifiers().contains(Modifier.STATIC)) {
                    manager.logError("ModelCacheField must be static from: " + typeElement);
                }
                if (!StringUtils.isNullOrEmpty(customCacheFieldName)) {
                    manager.logError("ModelCacheField can only be declared once from: " + typeElement);
                } else {
                    customCacheFieldName = element.getSimpleName().toString();
                }
            } else if (element.getAnnotation(MultiCacheField.class) != null) {
                if (!element.getModifiers().contains(Modifier.PUBLIC)) {
                    manager.logError("MultiCacheField must be public from: " + typeElement);
                }
                if (!element.getModifiers().contains(Modifier.STATIC)) {
                    manager.logError("MultiCacheField must be static from: " + typeElement);
                }
                if (!StringUtils.isNullOrEmpty(customMultiCacheFieldName)) {
                    manager.logError("MultiCacheField can only be declared once from: " + typeElement);
                } else {
                    customMultiCacheFieldName = element.getSimpleName().toString();
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

    @Override
    public ClassName getPropertyClassName() {
        return outputClassName;
    }

    @Override
    protected TypeName getExtendsClass() {
        return ParameterizedTypeName.get(ClassNames.MODEL_ADAPTER, elementClassName);
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {

        InternalAdapterHelper.writeGetModelClass(typeBuilder, elementClassName);
        InternalAdapterHelper.writeGetTableName(typeBuilder, tableName);

        FieldSpec.Builder getAllColumnPropertiesMethod = FieldSpec.builder(
                ArrayTypeName.of(ClassNames.IPROPERTY), "ALL_COLUMN_PROPERTIES",
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        CodeBlock.Builder getPropertiesBuilder = CodeBlock.builder();

        String paramColumnName = "columnName";
        MethodSpec.Builder getPropertyForNameMethod = MethodSpec.methodBuilder("getProperty")
                .addAnnotation(Override.class)
                .addParameter(ClassName.get(String.class), paramColumnName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(ClassNames.BASE_PROPERTY);

        getPropertyForNameMethod.addStatement("$L = $T.quoteIfNeeded($L)", paramColumnName,
                ClassName.get(QueryBuilder.class), paramColumnName);

        getPropertyForNameMethod.beginControlFlow("switch ($L) ", paramColumnName);
        for (int i = 0; i < columnDefinitions.size(); i++) {
            if (i > 0) {
                getPropertiesBuilder.add(",");
            }
            ColumnDefinition columnDefinition = columnDefinitions.get(i);
            columnDefinition.addPropertyDefinition(typeBuilder, elementClassName);
            columnDefinition.addPropertyCase(getPropertyForNameMethod);
            columnDefinition.addColumnName(getPropertiesBuilder);

        }
        getPropertyForNameMethod.beginControlFlow("default: ");
        getPropertyForNameMethod.addStatement("throw new $T($S)", IllegalArgumentException.class,
                "Invalid column name passed. Ensure you are calling the correct table's column");
        getPropertyForNameMethod.endControlFlow();
        getPropertyForNameMethod.endControlFlow();

        getAllColumnPropertiesMethod.initializer("new $T[]{$L}", ClassNames.IPROPERTY, getPropertiesBuilder.build().toString());
        typeBuilder.addField(getAllColumnPropertiesMethod.build());

        // add index properties here
        for (IndexGroupsDefinition indexGroupsDefinition : indexGroupsDefinitions) {
            typeBuilder.addField(indexGroupsDefinition.getFieldSpec());
        }

        typeBuilder.addMethod(getPropertyForNameMethod.build());

        if (hasAutoIncrement || hasRowID) {
            InternalAdapterHelper.writeUpdateAutoIncrement(typeBuilder, elementClassName,
                    autoIncrementDefinition);

            typeBuilder.addMethod(MethodSpec.methodBuilder("getAutoIncrementingId")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(elementClassName, ModelUtils.getVariable())
                    .addStatement("return $L", autoIncrementDefinition.getColumnAccessString(false))
                    .returns(ClassName.get(Number.class)).build());

            typeBuilder.addMethod(MethodSpec.methodBuilder("getAutoIncrementingColumnName")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return $S", QueryBuilder.stripQuotes(autoIncrementDefinition.columnName))
                    .returns(ClassName.get(String.class)).build());
        }

        typeBuilder.addMethod(MethodSpec.methodBuilder("getAllColumnProperties")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return ALL_COLUMN_PROPERTIES", outputClassName)
                .returns(ArrayTypeName.of(ClassNames.IPROPERTY)).build());

        if (cachingEnabled) {

            // TODO: pass in model cache loaders.

            boolean singlePrimaryKey = getPrimaryColumnDefinitions().size() == 1;
            typeBuilder.addMethod(MethodSpec.methodBuilder("createSingleModelLoader")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return new $T<>(getModelClass())",
                            singlePrimaryKey ? ClassNames.SINGLE_KEY_CACHEABLE_MODEL_LOADER :
                                    ClassNames.CACHEABLE_MODEL_LOADER)
                    .returns(ClassNames.SINGLE_MODEL_LOADER).build());

            typeBuilder.addMethod(MethodSpec.methodBuilder("createListModelLoader")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return new $T<>(getModelClass())",
                            singlePrimaryKey ? ClassNames.SINGLE_KEY_CACHEABLE_LIST_MODEL_LOADER :
                                    ClassNames.CACHEABLE_LIST_MODEL_LOADER)
                    .returns(ClassNames.LIST_MODEL_LOADER).build());

            typeBuilder.addMethod(MethodSpec.methodBuilder("createListModelSaver")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return new $T<>(getModelSaver())", ClassNames.CACHEABLE_LIST_MODEL_SAVER)
                    .returns(ParameterizedTypeName.get(ClassNames.CACHEABLE_LIST_MODEL_SAVER,
                            elementClassName)).build());

            typeBuilder.addMethod(MethodSpec.methodBuilder("cachingEnabled")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return $L", true)
                    .returns(TypeName.BOOLEAN).build());

            List<ColumnDefinition> primaries = primaryColumnDefinitions;
            if (primaries == null || primaries.isEmpty()) {
                primaries = Lists.newArrayList(autoIncrementDefinition);
            }
            InternalAdapterHelper.writeGetCachingId(typeBuilder, elementClassName, primaries);

            MethodSpec.Builder cachingbuilder = MethodSpec.methodBuilder("createCachingColumns")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            String columns = "return new String[]{";
            for (int i = 0; i < primaries.size(); i++) {
                ColumnDefinition column = primaries.get(i);
                if (i > 0) {
                    columns += ",";
                }
                columns += "\"" + QueryBuilder.quoteIfNeeded(column.columnName) + "\"";
            }
            columns += "}";

            cachingbuilder.addStatement(columns)
                    .returns(ArrayTypeName.of(ClassName.get(String.class)));

            typeBuilder.addMethod(cachingbuilder.build());

            if (cacheSize != Table.DEFAULT_CACHE_SIZE) {
                typeBuilder.addMethod(MethodSpec.methodBuilder("getCacheSize")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addStatement("return $L", cacheSize)
                        .returns(TypeName.INT).build());
            }

            if (!StringUtils.isNullOrEmpty(customCacheFieldName)) {
                typeBuilder.addMethod(MethodSpec.methodBuilder("createModelCache")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addStatement("return $T.$L", elementClassName, customCacheFieldName)
                        .returns(ParameterizedTypeName.get(ClassNames.MODEL_CACHE, elementClassName, WildcardTypeName.subtypeOf(Object.class))).build());
            }

            if (!StringUtils.isNullOrEmpty(customMultiCacheFieldName)) {
                typeBuilder.addMethod(MethodSpec.methodBuilder("getCacheConverter")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addStatement("return $T.$L", elementClassName, customMultiCacheFieldName)
                        .returns(ParameterizedTypeName.get(ClassNames.MULTI_KEY_CACHE_CONVERTER, WildcardTypeName.subtypeOf(Object.class))).build());
            }

            MethodSpec.Builder reloadMethod = MethodSpec.methodBuilder("reloadRelationships")
                    .addAnnotation(Override.class)
                    .addParameter(elementClassName, ModelUtils.getVariable())
                    .addParameter(ClassNames.CURSOR, LoadFromCursorMethod.PARAM_CURSOR)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            CodeBlock.Builder loadStatements = CodeBlock.builder();
            AtomicInteger noIndex = new AtomicInteger(-1);
            for (ColumnDefinition foreignColumn : foreignKeyDefinitions) {
                CodeBlock.Builder codeBuilder = foreignColumn.getLoadFromCursorMethod(
                        false, noIndex).toBuilder();
                if (!foreignColumn.elementTypeName.isPrimitive()) {
                    codeBuilder.nextControlFlow("else");
                    codeBuilder.add(foreignColumn.setColumnAccessString(CodeBlock.builder()
                            .add("null").build(), false)
                            .toBuilder().add(";\n").build());
                    codeBuilder.endControlFlow();
                }
                loadStatements.add(codeBuilder.build());
            }
            reloadMethod.addCode(loadStatements.build());
            typeBuilder.addMethod(reloadMethod.build());
        }

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
    }
}
