package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.core.AppContext;
import com.raizlabs.android.core.StringUtils;
import com.raizlabs.android.dbflow.ReflectionUtils;
import com.raizlabs.android.dbflow.config.DBConfiguration;
import com.raizlabs.android.dbflow.sql.builder.AbstractWhereQueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.PrimaryWhereQueryBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class defines the structure of the DB. It contains tables, type converters,
 * and other information pertaining to this DB.
 */
public class DBStructure {

    private Map<Class<? extends Model>, TableStructure> mTableStructure;

    private Map<Class<? extends Model>, PrimaryWhereQueryBuilder> mPrimaryWhereQueryBuilderMap;


    public DBStructure(DBConfiguration dbConfiguration) {
        mTableStructure = new HashMap<Class<? extends Model>, TableStructure>();
        mPrimaryWhereQueryBuilderMap = new HashMap<Class<? extends Model>, PrimaryWhereQueryBuilder>();
    }

    /**
     * This will construct the runtime structure of our DB for reference while the app is running.
     *
     * @param dbConfiguration
     */
    private void initializeStructure(DBConfiguration dbConfiguration) {
        List<Class<? extends Model> modelList = null;
        if (dbConfiguration.hasModelClasses()) {
            modelList = dbConfiguration.getModelClasses();
        } else {
            try {
                modelList = generateModelFromSource();
            } catch (IOException e) {
                //TODO: add logging
                e.printStackTrace();
            }
        }

        if (modelList != null) {
            for (Class<? extends Model> modelClass : modelList) {
                @SuppressWarnings("unchecked")
                TableStructure tableStructure = new TableStructure(modelClass);
                mTableStructure.put(modelClass, tableStructure);
            }
        }
    }

    public TableStructure getTableStructureForClass(Class<? extends Model> modelClass) {
        return getTableStructure().get(modelClass);
    }

    /**
     * Returns the Where Primary key query string from the cache for a specific model.
     * @param modelTable
     * @return
     */
    @SuppressWarnings("unchecked")
    public <ModelClass extends Model> AbstractWhereQueryBuilder<ModelClass> getPrimaryWhereQuery(Class<ModelClass> modelTable) {
        AbstractWhereQueryBuilder<ModelClass> abstractWhereQueryBuilder = getWhereQueryBuilderMap().get(modelTable);
        if(abstractWhereQueryBuilder == null ){
            abstractWhereQueryBuilder = new PrimaryWhereQueryBuilder<ModelClass>(modelTable);
            getWhereQueryBuilderMap().put(modelTable, abstractWhereQueryBuilder);
        }
        return abstractWhereQueryBuilder;
    }

    private List<Class<? extends Model>> generateModelFromSource() throws IOException {
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

    private void addModelClassesFromSource(File modelFile, String packageName,
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

                // First checks if it implements model, then if its not abstract class,
                // then if its not the Model class itself, and then if its not ignored
                if (ReflectionUtils.implementsModel(discoveredClass) &&
                        !Modifier.isAbstract(discoveredClass.getModifiers())
                        && !discoveredClass.equals(Model.class)
                        && !discoveredClass.isAnnotationPresent(Ignore.class)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Model> modelClass = (Class<? extends Model>) discoveredClass;
                    modelClasses.add(modelClass);
                }

                // TODO: add TypeConverters
                /*else if (ReflectionUtils.isTypeSerializer(discoveredClass) && !discoveredClass.isAnnotationPresent(Ignore.class)) {
                    TypeSerializer instance = (TypeSerializer) discoveredClass.newInstance();
                    mTypeSerializers.put(instance.getDeserializedType(), instance);
                }*/
            } catch (ClassNotFoundException e) {
                //AALog.e("Couldn't create class.", e);
            } catch (InstantiationException e) {
                //AALog.e("Couldn't instantiate TypeSerializer.", e);
            } catch (IllegalAccessException e) {
                //AALog.e("IllegalAccessException", e);
            }
        }


    }

    public Map<Class<? extends Model>, TableStructure> getTableStructure() {
        return mTableStructure;
    }

    public Map<Class<? extends Model>, AbstractWhereQueryBuilder> getWhereQueryBuilderMap() {
        return mPrimaryWhereQueryBuilderMap;
    }
}
