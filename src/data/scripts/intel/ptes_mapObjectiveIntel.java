package data.scripts.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.mapObjectives.ptes_baseMapObjective;
import data.scripts.plugins.ptes_mapObjectiveEntry;

import java.awt.*;
import java.util.Set;

import static data.scripts.ptes_ModPlugin.mapObjectivesMap;

public class ptes_mapObjectiveIntel extends BaseIntelPlugin {

    protected SectorEntityToken gate;
    public StarSystemAPI system;

    Color h = Misc.getHighlightColor();
    Color g = Misc.getGrayColor();
    Color tc = Misc.getTextColor();
    float pad = 3f;
    float opad = 10f;


    public ptes_mapObjectiveIntel(SectorEntityToken gate) {
        this.gate = gate;
        //((ptes_mapItemInfo) gate.getMemory().get("$activeMap"));
        //Global.getSector().getIntelManager().hasIntel()

    }

    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;

        Color tc = getBulletColorForMode(mode);

        bullet(info);
        boolean isUpdate = getListInfoParam() != null;


        unindent(info);
    }


    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.addPara(getName(), c, 0f);

        StarSystemAPI system = Global.getSector().getStarSystem("PoSMap");
        if (system != null && system.getScripts().size() > 0 && system.getScripts().get(0) instanceof ptes_baseMapObjective){
            ((ptes_baseMapObjective) system.getScripts().get(0)).createIntelInfo(info);
        } else {
            info.addPara("Strange gate was found", c, 0f);
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {

        info.addImage("graphics/illustrations/active_gate.jpg", width, pad);

        StarSystemAPI system = Global.getSector().getStarSystem("PoSMap");

        if (system != null && system.getScripts().size() > 0 && system.getScripts().get(0) instanceof ptes_baseMapObjective){

            info.addSectionHeading("Current objective", getFactionForUIColors().getBaseUIColor(),
                    getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
            ptes_mapItemInfo activeMap = (ptes_mapItemInfo) gate.getMemory().get("$activeMap");
            ptes_mapObjectiveEntry objectiveInfo = mapObjectivesMap.get(activeMap.objectiveID);
            info.addPara(objectiveInfo.name, opad);

            bullet(info);
            ((ptes_baseMapObjective) system.getScripts().get(0)).createIntelInfo(info);
            unindent(info);
        } else {
            info.addPara("Chip was not yet used", pad);
        }
    }

    @Override
    public String getIcon() {
        //return "graphics/cons/marketsD_gaseous_eruption.png";
        return "graphics/icons/missions/visit_object.png";
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_MISSIONS);
        return tags;
    }

    public String getSortString() {
        return "Gate status";
    }

    public String getName() {
        return  "Gate status";
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getFaction("player");
    }

    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return gate;
    }

    public boolean shouldRemoveIntel() {
        return isEnded();
    }

    @Override
    public String getCommMessageSound() {
        return super.getCommMessageSound();
    }

    @Override
    public Color getTitleColor(ListInfoMode mode) {
        return Global.getSector().getPlayerFaction().getBaseUIColor();
    }

}
