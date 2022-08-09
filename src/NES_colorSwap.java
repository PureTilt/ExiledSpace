package data.scripts.weapons.decos;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NES_colorSwap implements EveryFrameWeaponEffectPlugin {

    boolean runOnce;

    String hmodPrefix = "NES_colorPick";

    final Map<String, Integer> framesToColors = new HashMap<>();

    {
        framesToColors.put("HollowEmerald", 0);
        framesToColors.put("Aquamarine", 1);
        framesToColors.put("Amber", 2);
        framesToColors.put("RoseQuartz", 3);
        framesToColors.put("Amethyst", 4);
    }


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!runOnce) {
            if (engine == null) return;

            ShipAPI ship = weapon.getShip();
            if (ship != null && ship.getFleetMember() != null) {
                int maxFrames = weapon.getAnimation().getNumFrames() - 1;
                String shipName = ship.getFleetMember().getShipName();
                if (shipName != null) {
                    weapon.getAnimation().setFrame(new Random(shipName.hashCode()).nextInt(maxFrames));
                } else {
                    weapon.getAnimation().setFrame(new Random(new Date().getHours()).nextInt(maxFrames));
                }
            }
            runOnce = true;
        }
    }
}