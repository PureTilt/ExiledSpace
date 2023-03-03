package data.scripts.combat.shipSystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class bt_phaseInterdictionField extends BaseShipSystemScript {

    //List<ShipAPI> affectedShips = new ArrayList<>();
    IntervalUtil timer = new IntervalUtil(0.5f,0.5f);
    float range = 1000f;
    float costMulty = 2;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        timer.advance(amount);
        if (timer.intervalElapsed()){
            Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
            Map<ShipAPI, Float> affectedShips = new HashMap<>();

            if (customCombatData.get("bt_phaseInterdictionField") instanceof HashMap)
                affectedShips = (Map<ShipAPI, Float>) customCombatData.get("bt_phaseInterdictionField");

            for (ShipAPI ship : AIUtils.getNearbyEnemies(stats.getEntity(),range)){
                affectedShips.put(ship, 0.5f);
            }

            customCombatData.put("bt_phaseInterdictionField", affectedShips);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {

    }
}
