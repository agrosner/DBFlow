package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.data.Blob;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 */
public class LoadFromCursorMethod {

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
}
