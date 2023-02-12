package data.scripts.mapObjectives;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.scripts.items.ptes_mapItemInfo;
import data.world.systems.ptes_baseSystemScript;

import java.util.ArrayList;
import java.util.List;

public class ptes_killAll extends ptes_baseMapObjective {

    public List<CampaignFleetAPI> fleetsToKill = new ArrayList<>();

    public void init(ptes_mapItemInfo map, ptes_baseSystemScript script){
        fleetsToKill.addAll(script.spawnedFleets);
        for (CampaignFleetAPI fleet : fleetsToKill){
            fleet.getMemoryWithoutUpdate().set(MemFlags.ENTITY_MISSION_IMPORTANT, true);
        }
        Global.getLogger(ptes_killAll.class).info("objective added");
    }

    @Override
    public boolean objectiveComplete(){
        return fleetsToKill.isEmpty();
    }

    @Override
    public void advance(float amount) {
        if (fleetsToKill.isEmpty()) {
            return;
        }
        for (CampaignFleetAPI fleet : new ArrayList<>(fleetsToKill)){
            if (fleet == null || !fleet.isAlive()){
                fleetsToKill.remove(fleet);
                if (fleetsToKill.isEmpty()){
                    Global.getLogger(ptes_killAll.class).info("objective complete");
                    MessageIntel intel = new MessageIntel("Annihilation" + "\n    Objective complete",
                            Misc.getBasePlayerColor(), new String[] {"Objective complete"}, Misc.getTextColor());//, new String[] {"" + points}, Misc.getHighlightColor());
                    intel.setIcon(Global.getSettings().getSpriteName("intel", "new_planet_info"));
                    Global.getSector().getCampaignUI().addMessage(intel);
                }
            }
        }
    }

}
