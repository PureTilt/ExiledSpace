package data.scripts.mapEffects;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.world.systems.ptes_baseSystemScript;

public class ptes_stealthFleets implements ptes_baseEffectPlugin {
    @Override
    public void beforeGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {

    }

    @Override
    public void afterGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {
        for (CampaignFleetAPI fleet : genScript.spawnedFleets){
            fleet.getStats().getSensorProfileMod().modifyMult("ptes_steathFleets", 0.5f);
        }
    }
}
