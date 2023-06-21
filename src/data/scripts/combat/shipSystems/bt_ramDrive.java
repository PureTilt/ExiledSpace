package data.scripts.combat.shipSystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class bt_ramDrive extends BaseShipSystemScript {

    float damageReduction = 0.7f;
    float speedBoost = 500f;
    float accelerationMulti = 5f;
    float accelerationBonus = 25f;
    float speedReduction = 0.9f;
    float massMulti = 3;

    float massBeforeActivation = 0f;
    boolean doOnce = true;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (doOnce){
            massBeforeActivation = stats.getEntity().getMass();
            doOnce = false;
        }
        if (state == State.ACTIVE || state == State.IN){
            stats.getEntity().setMass(massBeforeActivation * massMulti);
            stats.getShieldDamageTakenMult().modifyMult("bt_ramDrive", 1 - damageReduction * effectLevel);
            stats.getHullDamageTakenMult().modifyMult("bt_ramDrive", 1 - damageReduction * effectLevel);
            stats.getArmorDamageTakenMult().modifyMult("bt_ramDrive", 1 - damageReduction * effectLevel);
            if (state == State.IN){
                stats.getMaxSpeed().modifyMult("bt_ramDrive", 1 - speedReduction * effectLevel);
            } else {
                stats.getMaxSpeed().unmodifyMult("bt_ramDrive");
                stats.getMaxSpeed().modifyFlat("bt_ramDrive", speedBoost);
                stats.getAcceleration().modifyMult("bt_ramDrive", accelerationMulti);
                stats.getAcceleration().modifyFlat("bt_ramDrive", accelerationBonus);
            }
        }
        if (state == State.OUT){
            stats.getMaxSpeed().unmodify("bt_ramDrive");
            stats.getShieldDamageTakenMult().unmodify("bt_ramDrive");
            stats.getHullDamageTakenMult().unmodify("bt_ramDrive");
            stats.getArmorDamageTakenMult().unmodify("bt_ramDrive");
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        if (!doOnce){
            stats.getEntity().setMass(massBeforeActivation);
            doOnce = true;
        }

        stats.getMaxSpeed().unmodify("bt_ramDrive");
        stats.getShieldDamageTakenMult().unmodify("bt_ramDrive");
        stats.getHullDamageTakenMult().unmodify("bt_ramDrive");
        stats.getArmorDamageTakenMult().unmodify("bt_ramDrive");
        stats.getAcceleration().unmodify("bt_ramDrive");
    }
}
