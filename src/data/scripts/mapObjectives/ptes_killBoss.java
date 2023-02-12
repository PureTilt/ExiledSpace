package data.scripts.mapObjectives;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.scripts.items.ptes_mapItemInfo;
import data.world.systems.ptes_baseSystemScript;
import org.lazywizard.lazylib.MathUtils;

public class ptes_killBoss extends ptes_baseMapObjective {

    public CampaignFleetAPI bossFleet;

    public void init(ptes_mapItemInfo map, ptes_baseSystemScript script){
        bossFleet = script.spawnedFleets.get(MathUtils.getRandomNumberInRange(0, script.spawnedFleets.size() - 1));
        bossFleet.getMemoryWithoutUpdate().set(MemFlags.ENTITY_MISSION_IMPORTANT, true);
        Global.getLogger(ptes_killAll.class).info("objective added");
    }

    @Override
    public boolean objectiveComplete() {
        return !bossFleet.isAlive();
    }

    @Override
    public void advance(float amount) {
        if (bossFleet == null || bossFleet.isEmpty() || !bossFleet.isAlive()) {
            Global.getLogger(ptes_killAll.class).info("objective complete");
            MessageIntel intel = new MessageIntel("Assassination" + "\n    Objective complete",
                    Misc.getBasePlayerColor(), new String[]{"Objective complete"}, Misc.getTextColor());//, new String[] {"" + points}, Misc.getHighlightColor());
            intel.setIcon(Global.getSettings().getSpriteName("intel", "new_planet_info"));
            Global.getSector().getCampaignUI().addMessage(intel);
        }
    }

}
