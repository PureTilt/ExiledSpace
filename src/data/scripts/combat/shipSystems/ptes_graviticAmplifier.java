package data.scripts.combat.shipSystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ptes_graviticAmplifier extends BaseShipSystemScript {

    float damageBonus = 2f,
    speedMulti = 0.5f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getEnergyDamageTakenMult().modifyMult(id, damageBonus);
        stats.getEnergyProjectileSpeedMult().modifyMult(id, speedMulti);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getEnergyDamageTakenMult().unmodify(id);
        stats.getEnergyProjectileSpeedMult().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        return new StatusData("Weapon systems enhanced", false);
    }
}
