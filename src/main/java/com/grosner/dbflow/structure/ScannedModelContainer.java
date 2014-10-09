package com.grosner.dbflow.structure;

import com.grosner.dbflow.ReflectionUtils;
import com.grosner.dbflow.StringUtils;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.runtime.observer.ModelObserver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dalvik.system.DexFile;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ScannedModelContainer {

    private static ScannedModelContainer container;

    public static ScannedModelContainer getInstance() {
        if(container == null) {
            container = new ScannedModelContainer();
            try {
                container.generateModelFromSource();
            } catch (IOException e) {
                FlowLog.logError(e);
            }
        }
        return container;
    }

    public static void addModelClassesToManager(FlowManager flowManager, List<Class<? extends Model>> modelClasses) {
        if(FlowManager.isMultipleDatabases()) {
            for(Class<? extends Model> modelClass : modelClasses) {
                FlowManager.putManagerForTable(modelClass, flowManager);
            }
        }
    }

    private List<Class<? extends Model>> mModelClasses = new ArrayList<Class<? extends Model>>();

    private HashMap<Class<? extends Model>, ModelObserver<? extends Model>> mModelObserversFound = new HashMap<Class<? extends Model>, ModelObserver<? extends Model>>();

    private HashMap<Class<? extends BaseModelView>, ModelViewDefinition> mModelViewDefinitions = new HashMap<Class<? extends BaseModelView>, ModelViewDefinition>();

    /**
     * Scours source code for {@link com.grosner.dbflow.structure.Model}, {@link com.grosner.dbflow.structure.BaseModelView}, and
     * {@link com.grosner.dbflow.converter.TypeConverter}.
     *
     * @return
     * @throws java.io.IOException
     */
    public void generateModelFromSource() throws IOException {
        String packageName = FlowManager.getInstance().getContext().getPackageName();

        String sourcePath = FlowManager.getInstance().getContext().getApplicationInfo().sourceDir;

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

        for (String path : paths) {
            File modelFile = new File(path);
            addModelClassesFromSource(modelFile, packageName);
        }

    }

    /**
     * Adds model classes from source
     *
     * @param modelFile    The file we search in
     * @param packageName  The package name of the directory
     */
    private void addModelClassesFromSource(File modelFile, String packageName) {
        ClassLoader classLoader = FlowManager.getContext().getClassLoader();

        if (modelFile.isDirectory()) {
            File[] modelFiles = modelFile.listFiles();
            for (File file : modelFiles) {
                addModelClassesFromSource(file, packageName);
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
                            && !ReflectionUtils.implementsModelViewDefinition(discoveredClass)) {

                        @SuppressWarnings("unchecked")
                        Class<? extends Model> modelClass = (Class<? extends Model>) discoveredClass;
                        mModelClasses.add(modelClass);
                    } else if (ReflectionUtils.implementsTypeConverter(discoveredClass)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends TypeConverter> typeConverterClass = (Class<? extends TypeConverter>) discoveredClass;
                        FlowManager.putTypeConverterForClass(typeConverterClass);
                    } else if (ReflectionUtils.implementsModelObserver(discoveredClass)) {
                        try {
                            @SuppressWarnings("unchecked")
                            ModelObserver<? extends ModelObserver> modelObserver = (ModelObserver<? extends ModelObserver>) discoveredClass.newInstance();
                            mModelObserversFound.put(modelObserver.getModelClass(), modelObserver);
                        } catch (Throwable e) {
                            FlowLog.logError(e);
                        }
                    } else if (ReflectionUtils.implementsModelViewDefinition(discoveredClass)) {
                        try {
                            @SuppressWarnings("unchecked")
                            ModelViewDefinition<?, ? extends Model> modelViewDefinition = (ModelViewDefinition) discoveredClass.newInstance();
                            mModelViewDefinitions.put(modelViewDefinition.getModelViewClass(), modelViewDefinition);
                        } catch (Exception e) {
                            FlowLog.logError(e);
                        }
                    }

                }
            } catch (ClassNotFoundException e) {
                FlowLog.log(FlowLog.Level.E, "Couldn't create class with name: " + className, e);
            }
        }


    }

    /**
     * Applies the Model list specified in the {@link com.grosner.dbflow.config.DBConfiguration} or the scanned list
     * by using only certain
     * @param modelList
     * @param dbStructure
     */
    public void applyModelListToFoundData(List<Class<? extends Model>> modelList, DBStructure dbStructure) {
        for(Class<? extends Model> model : modelList) {
            ModelObserver<? extends Model> modelObserver = mModelObserversFound.get(model);
            if(modelObserver != null) {
                dbStructure.getManager().addModelObserverForClass(modelObserver);
            }

            ModelViewDefinition modelViewDefinition = mModelViewDefinitions.get(model);
            if(modelViewDefinition != null) {
                dbStructure.putModelViewDefinition(modelViewDefinition);
            }
        }
    }

    public List<Class<? extends Model>> getModelClasses() {
        return mModelClasses;
    }

    public Set<Map.Entry<Class<? extends Model>, ModelObserver<? extends Model>>> getModelObserversFound() {
        return mModelObserversFound.entrySet();
    }

    public Set<Map.Entry<Class<? extends BaseModelView>, ModelViewDefinition>> getModelViewDefinitions() {
        return mModelViewDefinitions.entrySet();
    }
}
