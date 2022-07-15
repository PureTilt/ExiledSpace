package data.scripts.mapEffects;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.world.systems.ptes_baseSystemScript;

public class ptes_omegaIncursion implements ptes_baseEffectPlugin {


    WeightedRandomPicker<String> variants = new WeightedRandomPicker<>();

    {
        variants.add("facet_Armorbreaker");
        variants.add("facet_Attack");
        variants.add("facet_Attack2");
        variants.add("facet_Defense");
        variants.add("facet_Missile");
        variants.add("facet_Shieldbreaker");
    }

    @Override
    public void beforeGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {
    }

    @Override
    public void afterGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {
        for (CampaignFleetAPI fleet : genScript.spawnedFleets) {
            fleet.getFleetData().addFleetMember(variants.pick());
        }
    }
}
