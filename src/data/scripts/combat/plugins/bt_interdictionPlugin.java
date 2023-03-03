package data.scripts.combat.plugins;

import com.fs.graphics.LayeredRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import jdk.internal.joptsimple.util.KeyValuePair;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class bt_interdictionPlugin extends BaseEveryFrameCombatPlugin {

    public void advance(float amount, List<InputEventAPI> events) {
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        Map<ShipAPI, Float> affectedShips = new HashMap<>();

        if (customCombatData.get("bt_phaseInterdictionField") instanceof HashMap)
            affectedShips = (Map<ShipAPI, Float>) customCombatData.get("bt_phaseInterdictionField");

        for (Map.Entry<ShipAPI, Float> entry : new HashMap<>(affectedShips).entrySet()){
            if (entry.getValue() <= 0){
                entry.getKey().getMutableStats().getPhaseCloakUpkeepCostBonus().unmodify("bt_phaseInterdictionField");
                affectedShips.remove(entry.getKey());
            } else {
                affectedShips.put(entry.getKey(), affectedShips.get(entry.getKey()) - amount);
                entry.getKey().getMutableStats().getPhaseCloakUpkeepCostBonus().modifyMult("bt_phaseInterdictionField",2);
                entry.getKey().setJitterUnder(entry.getKey(), Color.CYAN,1,10,10);
            }
        }

        customCombatData.put("bt_phaseInterdictionField", affectedShips);
    }

}
