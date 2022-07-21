package data.scripts.mapEffects;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.world.systems.ptes_baseSystemScript;

public class ptes_qualityProduction implements ptes_baseEffectPlugin {
    @Override
    public void beforeGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {
        genScript.params.qualityMod = 0.25f;
    }

    @Override
    public void afterGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {

    }
}
