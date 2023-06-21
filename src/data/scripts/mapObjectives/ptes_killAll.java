package data.scripts.mapObjectives;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.plugins.ptes_mapObjectiveEntry;
import data.world.systems.ptes_baseSystemScript;

import java.util.ArrayList;
import java.util.List;

import static data.scripts.ptes_ModPlugin.mapObjectivesMap;

public class ptes_killAll extends ptes_baseMapObjective {

    public List<CampaignFleetAPI> fleetsToKill = new ArrayList<>();
    public int wasFleets = 0;

    public void init(ptes_mapItemInfo map, ptes_baseSystemScript script){
        fleetsToKill.clear();
        fleetsToKill.addAll(script.spawnedFleets);
        for (CampaignFleetAPI fleet : fleetsToKill){
            fleet.getMemoryWithoutUpdate().set(MemFlags.ENTITY_MISSION_IMPORTANT, true);
        }
        wasFleets = fleetsToKill.size();
        Global.getLogger(ptes_killAll.class).info("objective added");
        super.init(map, script);
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
                    notifyComplete();
                }
            }
        }
    }

    public void createIntelInfo(TooltipMakerAPI info){

        if (objectiveComplete()){
            info.addPara("Objective complete" , 0f);
        } else {
            info.addPara("Fleets left to kill: " + (wasFleets - fleetsToKill.size()) + "/" + wasFleets, 0f);
        }
    }

}
