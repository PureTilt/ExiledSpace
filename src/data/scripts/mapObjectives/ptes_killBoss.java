package data.scripts.mapObjectives;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.intel.ptes_mapObjectiveIntel;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.plugins.ptes_mapObjectiveEntry;
import data.world.systems.ptes_baseSystemScript;
import org.lazywizard.lazylib.MathUtils;

import static data.scripts.ptes_ModPlugin.mapObjectivesMap;

public class ptes_killBoss extends ptes_baseMapObjective {

    CampaignFleetAPI bossFleet;

    public void init(ptes_mapItemInfo map, ptes_baseSystemScript script){
        bossFleet = script.spawnedFleets.get(MathUtils.getRandomNumberInRange(0, script.spawnedFleets.size() - 1));
        bossFleet.getMemoryWithoutUpdate().set(MemFlags.ENTITY_MISSION_IMPORTANT, true);
        Global.getLogger(ptes_killAll.class).info("objective added");
        super.init(map,script);
    }

    @Override
    public boolean objectiveComplete() {
        return !bossFleet.isAlive();
    }

    @Override
    public void advance(float amount) {
        if (bossFleet == null || bossFleet.isEmpty() || !bossFleet.isAlive()) {
            notifyComplete();
        }
    }
    public void createIntelInfo(TooltipMakerAPI info){
        ptes_mapObjectiveEntry objectiveInfo = mapObjectivesMap.get(objectiveID);

        if (objectiveComplete()){
            info.addPara("Objective complete" , pad);
        } else {
            info.addPara("Kill special fleet", pad);
        }
    }

}
