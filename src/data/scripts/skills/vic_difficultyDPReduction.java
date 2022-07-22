package data.scripts.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class vic_difficultyDPReduction implements ShipSkillEffect {


    @Override
    public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
        float DPMulty = 1;
        if (stats.getFleetMember().getFleetData() != null && stats.getFleetMember().getFleetData().getFleet().getMemoryWithoutUpdate().contains("$difficultyDPMulty")){
            DPMulty = (float) stats.getFleetMember().getFleetData().getFleet().getMemoryWithoutUpdate().get("$difficultyDPMulty");
        }


        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult("ptes_difficultyDPMulty", DPMulty);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodifyFlat("ptes_difficultyDPMulty");
    }

    @Override
    public String getEffectDescription(float level) {
        return "Reduces Dp depending on maps fleet power";
    }

    @Override
    public String getEffectPerLevelDescription() {
        return null;
    }

    @Override
    public ScopeDescription getScopeDescription() {
        return ScopeDescription.ALL_SHIPS;
    }
}
