package data.scripts.mapEffects;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.world.systems.ptes_baseSystemScript;

public class ptes_recruitmentProgram implements ptes_baseEffectPlugin {
    @Override
    public void beforeGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {
        genScript.params.officerNumberMult *= 1.5f;
        genScript.params.officerLevelLimit += 2f;
        genScript.params.officerLevelBonus += 2f;
        genScript.params.commanderLevelLimit += 2f;
    }

    @Override
    public void afterGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {

    }
}
