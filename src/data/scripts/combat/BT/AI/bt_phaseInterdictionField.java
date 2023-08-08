package data.scripts.combat.BT.AI;

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
    public static float range = 1000f;
    float costMulty = 2;
    boolean doOnce = true;


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();

        if (doOnce){
            List<ShipAPI> shipsWithSystem = new ArrayList<>();
            if (customCombatData.get("bt_phaseInterdictionFieldRender") instanceof List)
                shipsWithSystem = (List<ShipAPI>) customCombatData.get("bt_phaseInterdictionFieldRender");

            shipsWithSystem.add((ShipAPI) (stats.getEntity()));

            customCombatData.put("bt_phaseInterdictionFieldRender", shipsWithSystem);
            doOnce = false;
        }

        timer.advance(amount);
        if (timer.intervalElapsed()){
            Map<ShipAPI, Float> affectedShips = new HashMap<>();


            if (customCombatData.get("bt_phaseInterdictionField") instanceof HashMap)
                affectedShips = (Map<ShipAPI, Float>) customCombatData.get("bt_phaseInterdictionField");


            for (ShipAPI ship : AIUtils.getNearbyEnemies(stats.getEntity(),range - stats.getEntity().getCollisionRadius())){
                affectedShips.put(ship, 0.5f);
            }

            customCombatData.put("bt_phaseInterdictionField", affectedShips);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {

    }
}
