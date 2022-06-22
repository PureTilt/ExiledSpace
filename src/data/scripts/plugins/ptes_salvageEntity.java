package data.scripts.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ptes_salvageEntity {

    public ptes_salvageEntity(String id, float weight, float cost , String factions){
        this.id = id;
        this.weight = weight;
        this.cost = cost;
        if (factions.equals("")){
            this.factions = new ArrayList<>();
        } else {
            this.factions = Arrays.asList(factions.split(","));
        }
    }
    public String id;
    public float weight;
    public float cost;
    public List<String> factions;
}
