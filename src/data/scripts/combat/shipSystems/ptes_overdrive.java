package data.scripts.combat.shipSystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.ArrayList;

public class ptes_overdrive extends BaseShipSystemScript {

    float timeActive = 0;

    float fastGrowThreshold = 2,
    slowGrowThreshold = fastGrowThreshold + 5,
    stableThreshold = slowGrowThreshold + 2,
    overchargeThreshold = stableThreshold + 5,
    overstressThreshold = overchargeThreshold + 5,

    fastEffectLevel = 0.75f,
    slowEffectLevel = 0.25f,
    overchargeLevel = 1f,
    malfunctionLevel = 0.1f,
    malfunctionPerSecond = 0.02f,

    RoFMulti = 0.5f,
    speedMulti = 0.3f,
    maneuverMulti = 0.5f;



    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        CombatEngineAPI engine = Global.getCombatEngine();
        float amount = engine.getElapsedInLastFrame();
        float bonusLevel = 0;
        float currentMalfunctionLevel = 0;

        timeActive += amount;
        if (timeActive <= fastGrowThreshold){
            bonusLevel = fastEffectLevel * (timeActive / fastGrowThreshold);
        } else if (timeActive <= slowGrowThreshold) {
            bonusLevel = fastEffectLevel + slowEffectLevel * ((timeActive - fastGrowThreshold) / (slowGrowThreshold - fastGrowThreshold));
        } else if (timeActive <= stableThreshold){
            bonusLevel = fastEffectLevel + slowEffectLevel;
        } else if (timeActive <= overchargeThreshold){
            bonusLevel = fastEffectLevel + slowEffectLevel + overchargeLevel * ((timeActive - stableThreshold) / (overchargeThreshold - stableThreshold));
            currentMalfunctionLevel = malfunctionLevel * ((timeActive - stableThreshold) / (overchargeThreshold - stableThreshold));
        } else if (timeActive <= overstressThreshold){
            bonusLevel = fastEffectLevel + slowEffectLevel + overchargeLevel;
            currentMalfunctionLevel = malfunctionLevel;
        } else {
            bonusLevel = fastEffectLevel + slowEffectLevel + overchargeLevel;
            currentMalfunctionLevel = malfunctionLevel + malfunctionPerSecond * (timeActive - overstressThreshold);
        }
        bonusLevel *= effectLevel;
        if (effectLevel < 1) currentMalfunctionLevel = 0;

        stats.getBallisticRoFMult().modifyMult(id, 1 + RoFMulti * bonusLevel);
        stats.getEnergyRoFMult().modifyMult(id, 1 + RoFMulti * bonusLevel);

        stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1 / (1 + RoFMulti * bonusLevel));
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1 / (1 + RoFMulti * bonusLevel));

        stats.getBallisticAmmoRegenMult().modifyMult(id, 1 + RoFMulti * bonusLevel);
        stats.getEnergyAmmoRegenMult().modifyMult(id, 1 + RoFMulti * bonusLevel);


        stats.getMaxSpeed().modifyPercent(id, 1 + speedMulti * bonusLevel);
        stats.getAcceleration().modifyPercent(id, 1 + speedMulti * 2 * bonusLevel);
        stats.getMaxTurnRate().modifyPercent(id, 1 + maneuverMulti * bonusLevel);
        stats.getTurnAcceleration().modifyPercent(id, 1 + maneuverMulti * 2 * bonusLevel);

        stats.getWeaponMalfunctionChance().modifyFlat(id,currentMalfunctionLevel);
        stats.getEngineMalfunctionChance().modifyFlat(id,currentMalfunctionLevel * 0.35f);

        ShipAPI ship = (ShipAPI) stats.getEntity();
        ship.getEngineController().extendFlame(id,0.35f * bonusLevel, 0.15f * bonusLevel, 0.3f * bonusLevel);
        if (currentMalfunctionLevel > 0) {
            ship.getEngineController().fadeToOtherColor(id,new Color(227, 52, 22,255),new Color(72, 24, 24, 187),currentMalfunctionLevel * 20f, 1f);
        }

        //engine.maintainStatusForPlayerShip("ptes_overdrive", null, "Overcharge", Misc.getRoundedValueOneAfterDecimalIfNotWhole(timeActive) + "/" + Misc.getRoundedValueOneAfterDecimalIfNotWhole(bonusLevel) + "/" + Misc.getRoundedValueOneAfterDecimalIfNotWhole(currentMalfunctionLevel), false);
        if (engine.getPlayerShip() == ship){
            engine.maintainStatusForPlayerShip("ptes_overdrive", null, "Overcharge", "boost level: " + Misc.getRoundedValueOneAfterDecimalIfNotWhole(bonusLevel * 100) + "%", false);
            if (currentMalfunctionLevel > 0){
                engine.maintainStatusForPlayerShip("ptes_overdriveMalfunction", null, "Overcharge", "Malfunction chance: " + Misc.getRoundedValueOneAfterDecimalIfNotWhole(currentMalfunctionLevel * effectLevel * 100) + "%", true);
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        timeActive = 0;

        massUnmod(id, new ArrayList<MutableStat>(){
            {
                stats.getBallisticRoFMult();
                stats.getMaxSpeed();
                stats.getAcceleration();
                stats.getAcceleration();
                stats.getWeaponMalfunctionChance();
                stats.getEngineMalfunctionChance();
            }
        });
    }

    void massUnmod(String id, ArrayList<MutableStat> stats){
        for (MutableStat stat : stats){
            stat.unmodify(id);
        }
    }
}
