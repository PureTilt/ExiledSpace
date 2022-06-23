package data.scripts.items;

import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import data.scripts.plugins.ptes_baseEffectPlugin;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.Color;

import java.util.ArrayList;
import java.util.List;

public class ptes_mapItemInfo extends SpecialItemData {

    public final int
            FP,
            LP;
    public final String FactionId;
    public final StarSystemGenerator.StarSystemType systemType;
    public List<ptes_baseEffectPlugin> effects = new ArrayList<>();

    int dopID = MathUtils.getRandomNumberInRange(0, 99999999);

    public ptes_mapItemInfo(String id, String data, int FP, int lootPoints, String factionID) {
        super(id, data);
        this.LP = Math.round(lootPoints);
        this.FP = Math.round(FP);
        this.FactionId = factionID;
        systemType = null;
    }

    public ptes_mapItemInfo(String id, String data, int FP, int lootPoints, String factionID, StarSystemGenerator.StarSystemType systemType) {
        super(id, data);
        this.LP = Math.round(lootPoints);
        this.FP = Math.round(FP);
        this.FactionId = factionID;
        this.systemType = systemType;
    }

    public ptes_mapItemInfo(String id, String data, int FP, int lootPoints, String factionID, StarSystemGenerator.StarSystemType systemType, List<ptes_baseEffectPlugin> effects) {
        super(id, data);
        this.LP = Math.round(lootPoints);
        this.FP = Math.round(FP);
        this.FactionId = factionID;
        this.systemType = systemType;
        this.effects = effects;
    }

    @Override
    public int hashCode() {
        return (dopID + "").hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

}
