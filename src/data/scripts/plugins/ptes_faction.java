package data.scripts.plugins;

import data.world.systems.ptes_baseSystemScript;

public class ptes_faction {

    public ptes_faction(String id, float weight, float FPMulti, Class<?> genClass, float lootMulti){
        this.faction = id;
        this.weight = weight;
        this.FPMulti = FPMulti;
        this.lootMulti = lootMulti;
        try {
            this.genClass = (ptes_baseSystemScript) genClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public String faction;
    public float weight;
    public float FPMulti;
    public ptes_baseSystemScript genClass;
    public float lootMulti;

}