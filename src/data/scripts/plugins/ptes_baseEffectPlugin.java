package data.scripts.plugins;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import data.world.systems.ptes_baseSystemScript;

public interface ptes_baseEffectPlugin {

    public void beforeGeneration(StarSystemAPI system, ptes_baseSystemScript genScript);

    public void afterGeneration(StarSystemAPI system, ptes_baseSystemScript genScript);
}
