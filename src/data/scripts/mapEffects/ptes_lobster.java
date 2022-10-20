package data.scripts.mapEffects;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.world.systems.ptes_baseSystemScript;
import org.lazywizard.lazylib.MathUtils;

public class ptes_lobster implements ptes_baseEffectPlugin {
    @Override
    public void beforeGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {

    }

    @Override
    public void afterGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {
        for (CampaignFleetAPI fleet : genScript.spawnedFleets) {
            fleet.getCargo().addCommodity("lobster", fleet.getFleetPoints() * MathUtils.getRandomNumberInRange(0.5f,1.5f));
        }
    }
}
