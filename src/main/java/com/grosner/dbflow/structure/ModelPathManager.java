package com.grosner.dbflow.structure;

import java.util.ArrayList;

/**
 * Description: This class allows you to Specify what paths you want to use scan for classes,
 * significantly cutting down load time.
 */
public class ModelPathManager {

    /**
     * The list of paths we will search for
     */
    private static ArrayList<String> PATHS;

    /**
     * Returns the list of paths
     *
     * @return
     */
    public static ArrayList<String> getPaths() {
        if (PATHS == null) {
            PATHS = new ArrayList<String>();
        }
        return PATHS;
    }

    /**
     * Adds a path to the list of Paths we check to ignore on launch.
     * This method will check to see if the class name starts with the path, so be careful of your exclusions.
     * Also call this method before the
     * {@link com.grosner.dbflow.config.FlowManager#initialize(android.content.Context, com.grosner.dbflow.config.DBConfiguration, com.grosner.dbflow.DatabaseHelperListener)}
     *
     * @param path - the starting part of the class. E.g: com.example.somepackage
     */
    public static void addPath(String path) {
        getPaths().add(path);
    }


}
