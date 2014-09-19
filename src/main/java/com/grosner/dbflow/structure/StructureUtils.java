package com.grosner.dbflow.structure;

import com.grosner.dbflow.ReflectionUtils;
import com.grosner.dbflow.StringUtils;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.converter.ForeignKeyConverter;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.runtime.observer.ModelObserver;

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
 * Description: Provides some basic database structure utility methods.
 */
public class StructureUtils {

    /**
     * Checks to see if field is a {@link com.grosner.dbflow.structure.ColumnType#PRIMARY_KEY}
     *
     * @param field
     * @return
     */
    public static boolean isPrimaryKey(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if (isPrimary) {
            isPrimary = column.value().value() != ColumnType.FOREIGN_KEY;
        }
        return isPrimary;
    }

    /**
     * Checks to see if field is a {@link com.grosner.dbflow.structure.ColumnType#FOREIGN_KEY}
     *
     * @param field
     * @return
     */
    public static boolean isForeignKey(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isForeign = column != null;
        if (isForeign) {
            isForeign = column.value().value() == ColumnType.FOREIGN_KEY;
        }
        return isForeign;
    }

    /**
     * Checks to see if field is not {@link com.grosner.dbflow.structure.ColumnType#PRIMARY_KEY_AUTO_INCREMENT}
     *
     * @param field
     * @return
     */
    public static boolean isPrimaryKeyNoIncrement(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if (isPrimary) {
            isPrimary = column.value().value() == ColumnType.PRIMARY_KEY;
        }
        return isPrimary;
    }

    /**
     * Returns true if the field is {@link com.grosner.dbflow.structure.ColumnType#PRIMARY_KEY_AUTO_INCREMENT}
     *
     * @param field
     * @return
     */
    public static boolean isPrimaryKeyAutoIncrement(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if (isPrimary) {
            isPrimary = column.value().value() == ColumnType.PRIMARY_KEY_AUTO_INCREMENT;
        }
        return isPrimary;
    }

    /**
     * Scours source code for {@link com.grosner.dbflow.structure.Model}, {@link com.grosner.dbflow.structure.ModelView},
     * {@link com.grosner.dbflow.converter.TypeConverter}, and {@link com.grosner.dbflow.converter.ForeignKeyConverter}
     *
     * @param flowManager The database manager
     * @return
     * @throws IOException
     */
    static List<Class<? extends Model>> generateModelFromSource(FlowManager flowManager) throws IOException {
        String packageName = flowManager.getContext().getPackageName();

        String sourcePath = flowManager.getContext().getApplicationInfo().sourceDir;

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
            addModelClassesFromSource(flowManager, modelFile, packageName, modelClasses);
        }

        return modelClasses;
    }

    /**
     * Adds model classes from source
     *
     * @param flowManager  The database manager
     * @param modelFile    The file we search in
     * @param packageName  The package name of the directory
     * @param modelClasses The classes that we are adding to
     */
    private static void addModelClassesFromSource(FlowManager flowManager, File modelFile, String packageName,
                                                  List<Class<? extends Model>> modelClasses) {
        ClassLoader classLoader = flowManager.getContext().getClassLoader();

        if (modelFile.isDirectory()) {
            File[] modelFiles = modelFile.listFiles();
            for (File file : modelFiles) {
                addModelClassesFromSource(flowManager, file, packageName, modelClasses);
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

                if (!discoveredClass.isAnnotationPresent(Ignore.class)) {
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
                        flowManager.putTypeConverterForClass(typeConverterClass);
                    } else if (ReflectionUtils.implementsForeignKeyConverter(discoveredClass)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends ForeignKeyConverter> foreignKeyConverterClass = ((Class<? extends ForeignKeyConverter>) discoveredClass);
                        flowManager.getStructure().putForeignKeyConverterForClass(foreignKeyConverterClass);
                    } else if(ReflectionUtils.implementsModelObserver(discoveredClass)) {
                        try {
                            @SuppressWarnings("unchecked")
                            ModelObserver<? extends ModelObserver> modelObserver = (ModelObserver<? extends ModelObserver>) discoveredClass.newInstance();
                            flowManager.getStructure().addModelObserverForClass(modelObserver);
                        } catch (Throwable e) {
                            FlowLog.logError(e);
                        }
                    }

                }
            } catch (ClassNotFoundException e) {
                FlowLog.log(FlowLog.Level.E, "Couldn't create class with name: " + className, e);
            }
        }


    }
}
