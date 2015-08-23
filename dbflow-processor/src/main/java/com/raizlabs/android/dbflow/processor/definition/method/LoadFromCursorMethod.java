package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class LoadFromCursorMethod implements MethodDefinition {

    private static final Map<TypeName, String> sMethodMap = new HashMap<TypeName, String>() {{
        put(ArrayTypeName.of(TypeName.BYTE), "getBlob");
        put(ArrayTypeName.of(TypeName.BYTE.box()), "getBlob");
        put(TypeName.DOUBLE, "getDouble");
        put(TypeName.DOUBLE.box(), "getDouble");
        put(TypeName.FLOAT, "getFloat");
        put(TypeName.FLOAT.box(), "getFloat");
        put(TypeName.INT, "getInt");
        put(TypeName.INT.box(), "getInt");
        put(TypeName.LONG, "getLong");
        put(TypeName.LONG.box(), "getLong");
        put(TypeName.SHORT, "getShort");
        put(TypeName.SHORT.box(), "getShort");
        put(ClassName.get(String.class), "getString");
        put(ClassName.get(Blob.class), "getBlob");
    }};

    public static boolean containsMethod(TypeName typeName) {
        return sMethodMap.containsKey(typeName);
    }

    public static String getMethod(TypeName typeName) {
        return sMethodMap.get(typeName);
    }

    public static final String PARAM_MODEL = "model";
    public static final String PARAM_CURSOR = "cursor";

    private BaseTableDefinition baseTableDefinition;

    public LoadFromCursorMethod(BaseTableDefinition baseTableDefinition) {

        this.baseTableDefinition = baseTableDefinition;
    }

    @Override
    public MethodSpec getMethodSpec() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("loadFromCursor")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(baseTableDefinition.elementClassName, PARAM_MODEL)
                .addParameter(ClassNames.CURSOR, PARAM_CURSOR)
                .returns(TypeName.VOID);

        List<ColumnDefinition> columnDefinitionList = baseTableDefinition.getColumnDefinitions();
        for (ColumnDefinition columnDefinition : columnDefinitionList) {
            methodBuilder.addCode(columnDefinition.getLoadFromCursorMethod());
        }

        return methodBuilder.build();
    }
}
