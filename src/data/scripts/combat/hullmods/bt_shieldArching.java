package data.scripts.combat.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Map;

public class bt_shieldArching extends BaseHullMod {

    //min and max distance between arcs, its angle so bigger ships will have bigger distance in SU
    float maxAngle = 40f,
            minAngle = 10f;

    public void advanceInCombat(ShipAPI ship, float amount) {
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        String id = ship.getId();

        //time between arcs first min second max
        IntervalUtil timer = new IntervalUtil(0.5f, 0.5f);

        if (customCombatData.get("bt_shieldArching" + id) instanceof IntervalUtil)
            timer = (IntervalUtil) customCombatData.get("bt_shieldArching" + id);

        if (ship.getShield().isOff()) return;

        timer.advance(amount);
        customCombatData.put("bt_shieldArching" + id, timer);
        if (timer.intervalElapsed()){
            if (ship.getShield().getActiveArc() < minAngle) return;

            float arcDistance = MathUtils.getRandomNumberInRange(minAngle, maxAngle);
            float StartAngle = MathUtils.getRandomNumberInRange(0, ship.getShield().getActiveArc() - arcDistance);
            float shieldStartAngle = ship.getShield().getFacing() + (ship.getShield().getActiveArc() * 0.5f);
            float arcStartAngle = shieldStartAngle - StartAngle;


            Vector2f arcStart = MathUtils.getPointOnCircumference(ship.getShieldCenterEvenIfNoShield(),ship.getShield().getRadius(),arcStartAngle);
            Vector2f arcEnd = MathUtils.getPointOnCircumference(ship.getShieldCenterEvenIfNoShield(),ship.getShield().getRadius(),arcStartAngle - arcDistance);

            Global.getCombatEngine().spawnEmpArcVisual(arcStart,ship,arcEnd,ship,10,
                    new Color(255, 0, 0,255),
                    new Color(0, 255, 207,255));

        }
    }

}
