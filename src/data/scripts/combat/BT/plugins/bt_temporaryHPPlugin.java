package data.scripts.combat.BT.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class bt_temporaryHPPlugin implements AdvanceableListener, DamageTakenModifier {

    float maxDuration = 20f;
    float hullPerBuff = 2000f;

    ShipAPI target = null;
    float duration = 0f;
    float hullAmount = 0f;

    public bt_temporaryHPPlugin(ShipAPI target) {
        this.target = target;
        duration = maxDuration;
        hullAmount = hullPerBuff;
    }

    @Override
    public void advance(float amount) {
        duration -= amount;
        target.setJitterShields(false);
        target.setJitterUnder(target, new Color(245, 89, 22, 200), 1f, 25, 7);
        target.setJitter(target, new Color(245, 89, 22, 55), 1f, 2, 5);
        if (target == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip("bt_temporaryHPPluginTimer", null, "Remote Dumper", Math.round(duration * 10f) / 10f + " " + Math.round(hullAmount), false);
        }
        if (duration <= 0 || hullAmount <= 0) {
            target.setJitterShields(true);
            target.removeListener(this);
        }
    }

    @Override
    public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
        if (hullAmount <= 0) return null;
        if (target instanceof ShipAPI) {
            ShipAPI ship = ((ShipAPI) target);
            if (!shieldHit) {
                float actualDamage = damage.computeDamageDealt(Global.getCombatEngine().getElapsedInLastFrame());
                switch (damage.getType()) {
                    case KINETIC:
                        actualDamage *= 0.5f;
                        break;
                    case HIGH_EXPLOSIVE:
                        actualDamage *= 2f;
                        break;
                    case FRAGMENTATION:
                        actualDamage *= 0.25f;
                        break;
                }
                if (actualDamage >= hullAmount) {
                    float absorptionRate = hullAmount / damage.getDamage();
                    damage.getModifier().modifyMult("bt_temporaryHPPlugin", 1 - absorptionRate);
                    hullAmount = 0;
                } else {
                    hullAmount -= actualDamage;
                    damage.getModifier().modifyMult("bt_temporaryHPPlugin", 0);
                }
                return "bt_temporaryHPPlugin";
            }
        }
        return null;
    }

    public void reapllyBuff() {
        duration = maxDuration;
        hullAmount += hullPerBuff;
    }
}
