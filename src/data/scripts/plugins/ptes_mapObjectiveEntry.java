package data.scripts.plugins;

import data.scripts.mapObjectives.ptes_baseMapObjective;

import java.lang.reflect.InvocationTargetException;

public class ptes_mapObjectiveEntry {

    public ptes_mapObjectiveEntry(String id, String name, float weight, String description, String iconPath, Class<?> genClass) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.description = description;
        this.iconPath = iconPath;
        this.genClass = genClass;
    }

    public String id;
    public String name;
    public float weight;
    public String description;
    public String iconPath;
    public Class<?> genClass;


}
