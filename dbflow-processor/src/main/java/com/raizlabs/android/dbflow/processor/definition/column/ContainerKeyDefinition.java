package com.raizlabs.android.dbflow.processor.definition.column;

import com.raizlabs.android.dbflow.annotation.ContainerKey;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.processor.SQLiteHelper;
import com.raizlabs.android.dbflow.processor.definition.BaseDefinition;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Description: Marking a field with {@link ContainerKey} alone will generate its {@link ModelContainer}
 * methods only for storage and toModel access.
 */
public class ContainerKeyDefinition extends BaseDefinition {

    private BaseColumnAccess columnAccess;
    public String containerKeyName;

    public ContainerKeyDefinition(Element element, ProcessorManager processorManager,
                                  BaseTableDefinition baseTableDefinition, boolean isPackagePrivate) {
        super(element, processorManager);

        ContainerKey containerKey = element.getAnnotation(ContainerKey.class);
        if (containerKey != null) {
            containerKeyName = containerKey.value();
            if (StringUtils.isNullOrEmpty(containerKeyName)) {
                containerKeyName = elementName;
            }
        } else {
            containerKeyName = elementName;
        }

        if (isPackagePrivate) {
            columnAccess = PackagePrivateAccess.from(processorManager, element, baseTableDefinition.databaseDefinition.fieldRefSeparator);

            // register to ensure we only generate methods that are referenced by these columns.
            PackagePrivateAccess.putElement(((PackagePrivateAccess) columnAccess).helperClassName, this.containerKeyName);
        } else {
            boolean isPrivate = element.getModifiers()
                    .contains(Modifier.PRIVATE);
            if (isPrivate) {
                boolean useIs = elementTypeName.box().equals(TypeName.BOOLEAN.box())
                        && (baseTableDefinition instanceof TableDefinition) && ((TableDefinition) baseTableDefinition).useIsForPrivateBooleans;
                columnAccess = new PrivateColumnAccess(useIs);
            } else {
                columnAccess = new SimpleColumnAccess();
            }
        }


    }

    public CodeBlock getToModelMethod() {
        String method = SQLiteHelper.getModelContainerMethod(elementTypeName);
        if (method == null) {
            if (columnAccess instanceof EnumColumnAccess) {
                method = SQLiteHelper.getModelContainerMethod(ClassName.get(String.class));
            } else {
                if (columnAccess instanceof TypeConverterAccess) {
                    method = SQLiteHelper.getModelContainerMethod(((TypeConverterAccess) columnAccess).typeConverterDefinition.getDbTypeName());
                }
                if (method == null) {
                    manager.logError("ToModel typename: %1s", elementTypeName);
                    method = "get";
                }
            }
        }
        CodeBlock.Builder codeBuilder = CodeBlock.builder()
                .add("$L.$LValue($S)", ModelUtils.getVariable(true), method, containerKeyName);

        BaseColumnAccess columnAccessToUse = columnAccess;
        if (columnAccess instanceof BooleanColumnAccess ||
                (columnAccess instanceof TypeConverterAccess && ((TypeConverterAccess) columnAccess)
                        .typeConverterDefinition.getModelTypeName().equals(TypeName.BOOLEAN.box()))) {
            columnAccessToUse = ((TypeConverterAccess) columnAccess).existingColumnAccess;
        }
        return CodeBlock.builder()
                .addStatement(columnAccessToUse.setColumnAccessString(elementTypeName, containerKeyName, elementName,
                        false, ModelUtils.getVariable(false), codeBuilder.build(), true))
                .build();
    }

}
