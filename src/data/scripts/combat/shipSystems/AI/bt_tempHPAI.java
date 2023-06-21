package data.scripts.combat.shipSystems.AI;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

import static data.scripts.combat.shipSystems.bt_temporaryHP.maxRange;
import static org.lazywizard.lazylib.combat.AIUtils.getNearbyAllies;

public class bt_tempHPAI implements ShipSystemAIScript {

    IntervalUtil timer = new IntervalUtil(0.5f,0.5f);
    ShipAPI ship;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        timer.advance(amount);
        if (timer.intervalElapsed()){
            for (ShipAPI allyShip : getNearbyAllies(ship, maxRange)) {
                if (allyShip.areSignificantEnemiesInRange()) {
                    ship.useSystem();
                    break;
                }
            }
        }
    }
}
