package data.scripts.mapObjectives;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.plugins.ptes_mapObjectiveEntry;
import data.world.systems.ptes_baseSystemScript;

import static data.scripts.ptes_ModPlugin.mapObjectivesMap;

public abstract class ptes_baseMapObjective implements EveryFrameScript {

    String objectiveID;

    public void init(ptes_mapItemInfo map, ptes_baseSystemScript script){

        objectiveID = map.objectiveID;
    }

    public boolean objectiveComplete(){
        return false;
    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    public void notifyComplete(){
        Global.getLogger(ptes_killAll.class).info("objective complete");
        ptes_mapObjectiveEntry objectiveInfo = mapObjectivesMap.get(objectiveID);
        MessageIntel intel = new MessageIntel(objectiveInfo.name + "\n    Objective complete",
                Misc.getBasePlayerColor(), new String[]{"Objective complete"}, Misc.getTextColor());//, new String[] {"" + points}, Misc.getHighlightColor());
        intel.setIcon(objectiveInfo.iconPath);
        Global.getSector().getCampaignUI().addMessage(intel);
    }


}
