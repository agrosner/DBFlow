package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.core.AppContext;
import com.raizlabs.android.core.StringUtils;
import com.raizlabs.android.dbflow.ReflectionUtils;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.converter.ForeignKeyConverter;
import com.raizlabs.android.dbflow.converter.TypeConverter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class StructureUtils {

    public static boolean isPrimaryKey(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if (isPrimary) {
            isPrimary = column.columnType().type() != ColumnType.FOREIGN_KEY;
        }
        return isPrimary;
    }

    public static boolean isForeignKey(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isForeign = column != null;
        if (isForeign) {
            isForeign = column.columnType().type() == ColumnType.FOREIGN_KEY;
        }
        return isForeign;
    }

    public static boolean isPrimaryKeyNoIncrement(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if (isPrimary) {
            isPrimary = column.columnType().type() == ColumnType.PRIMARY_KEY;
        }
        return isPrimary;
    }

    public static boolean isPrimaryKeyAutoIncrement(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if(isPrimary) {
            isPrimary = column.columnType().type() == ColumnType.PRIMARY_KEY_AUTO_INCREMENT;
        }
        return isPrimary;
    }

    static List<Class<? extends Model>> generateModelFromSource() throws IOException {
        String packageName = AppContext.getInstance().getPackageName();

        String sourcePath = AppContext.getInstance().getApplicationInfo().sourceDir;

        List<String> paths = new ArrayList<String>();

        if (StringUtils.isNotNullOrEmpty(sourcePath)
                && !(new File(sourcePath).isDirectory())) {
            DexFile dexFile = new DexFile(sourcePath);
            Enumeration<String> entries = dexFile.entries();

            while (entries.hasMoreElements()) {
                String path = entries.nextElement();
                for (String modelPath : ModelPathManager.getPaths()) {
                    if (path.startsWith(modelPath)) {
                        paths.add(path);
                        break;
                    }
                }
            }

            dexFile.close();
        } else {

            // RoboElectric fallback
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("");

            while (resources.hasMoreElements()) {
                String path = resources.nextElement().getFile();
                if (path.contains("bin") || path.contains("classes")) {
                    paths.add(path);
                }
            }
        }

        List<Class<? extends Model>> modelClasses = new ArrayList<Class<? extends Model>>();
        for (String path : paths) {
            File modelFile = new File(path);
            addModelClassesFromSource(modelFile, packageName, modelClasses);
        }

        return modelClasses;
    }

    private static void addModelClassesFromSource(File modelFile, String packageName,
                                                  List<Class<? extends Model>> modelClasses) {
        ClassLoader classLoader = AppContext.getInstance().getClassLoader();

        if (modelFile.isDirectory()) {
            File[] modelFiles = modelFile.listFiles();
            for (File file : modelFiles) {
                addModelClassesFromSource(file, packageName, modelClasses);
            }
        } else {
            String className = modelFile.getName();

            // RoboElectric fallback
            if (!modelFile.getPath().equals(className)) {
                className = modelFile.getPath();

                if (className.endsWith(".class")) {
                    className = className.substring(0, className.length() - 6);
                } else {
                    return;
                }

                className = className.replace("/", "");

                int packageNameIndex = className.lastIndexOf(packageName);
                if (packageNameIndex < 0) {
                    return;
                }

                className = className.substring(packageNameIndex);
            }

            try {
                Class<?> discoveredClass = Class.forName(className, false, classLoader);

                if(!discoveredClass.isAnnotationPresent(Ignore.class)) {
                    // First checks if it implements model, then if its not abstract class,
                    // then if its not the Model class itself, and then if its not ignored
                    if (ReflectionUtils.implementsModel(discoveredClass) &&
                            !Modifier.isAbstract(discoveredClass.getModifiers())
                            && !discoveredClass.equals(Model.class)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends Model> modelClass = (Class<? extends Model>) discoveredClass;
                        modelClasses.add(modelClass);
                    } else if (ReflectionUtils.implementsTypeConverter(discoveredClass)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends TypeConverter> typeConverterClass = (Class<? extends TypeConverter>) discoveredClass;
                        FlowManager.getCache().getStructure().putTypeConverterForClass(typeConverterClass);
                    } else if (ReflectionUtils.implementsForeignKeyConverter(discoveredClass)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends ForeignKeyConverter> foreignKeyConverterClass = ((Class<? extends ForeignKeyConverter>) discoveredClass);
                        FlowManager.getCache().getStructure().putForeignKeyConverterForClass(foreignKeyConverterClass);
                    }

                }
            } catch (ClassNotFoundException e) {
                FlowLog.e(StructureUtils.class.getSimpleName(), "Couldn't create class.", e);
            }
        }


    }
}
