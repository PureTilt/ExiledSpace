package data.scripts.plugins;

public class ptes_mapEffectEntry {

    public ptes_mapEffectEntry(String id, String name, float cost, float weight, float order, String description, String iconPath, Class<?> genClass) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.weight = weight;
        this.description = description;
        this.iconPath = iconPath;
        this.order = Math.round(order);
        try {
            this.effectClass = (ptes_baseEffectPlugin) genClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public String id;
    public String name;
    public float weight;
    public float cost;
    public int order;
    public String description;
    public String iconPath;
    public ptes_baseEffectPlugin effectClass;

}
