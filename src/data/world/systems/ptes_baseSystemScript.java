package data.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.scripts.plugins.ptes_faction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static data.scripts.ptes_ModPlugin.FactionMap;

public class ptes_baseSystemScript {

    float
            EnemyFP,
            LootPoints;

    ptes_faction faction;
    ptes_mapItemInfo mapData;
    List<ptes_baseEffectPlugin> effects;

    boolean devMode = Global.getSettings().isDevMode();



    public void generate(SectorAPI sector, SectorEntityToken RiftGate, StarSystemAPI system, InteractionDialogAPI dialog, ptes_mapItemInfo mapData) {
        //generate system if it doesnt exists
        if (system == null) {
            system = sector.createStarSystem("PoSMap");
            system.setName("Map");
            system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER);
            system.addTag(Tags.TRANSIENT);
            Global.getLogger(ptes_genericSystem.class).info("made new system");
            //Global.getLogger(ptes_genericSystem.class).info(system.getName());
        } else {
            Global.getLogger(ptes_genericSystem.class).info("found system");
            List<SectorEntityToken> cloneList = new ArrayList<>(system.getAllEntities());
            for (SectorEntityToken entity : cloneList) {
                system.removeEntity(entity);
            }
        }

        //move system under gate just in case
        system.getLocation().set(RiftGate.getLocation());

        //set data
        EnemyFP = mapData.FP;
        LootPoints = mapData.LP;
        this.mapData = mapData;
        this.effects = mapData.effects;
        faction = FactionMap.get(mapData.FactionId);

        //apply effects before gen
        for (ptes_baseEffectPlugin effect : effects){
            effect.beforeGeneration(system, this);
        }
        //generate system
        generateSystem(system, dialog);
        //redirect jump points
        for (SectorEntityToken point : system.getJumpPoints()) {
            JumpPointAPI jumpPoint = (JumpPointAPI) point;
            jumpPoint.clearDestinations();
            jumpPoint.addDestination(new JumpPointAPI.JumpDestination(RiftGate, "the Rift gate"));
            jumpPoint.setStandardWormholeToHyperspaceVisual();
        }
        //apply effects after gen
        for (ptes_baseEffectPlugin effect : effects){
            effect.afterGeneration(system, this);
        }

        //set planets to correct survey level
        for (SectorEntityToken entity : new ArrayList<>(system.getAllEntities())) {
            system.renderingLayersUpdated(entity);
            if (entity instanceof PlanetAPI) {
                if (entity.getMarket() == null) continue;
                entity.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.SEEN);
                for (MarketConditionAPI condition : entity.getMarket().getConditions()) {
                    if (!condition.requiresSurveying()) {
                        condition.setSurveyed(true);
                    }
                }
            }
        }
    }

    public void generateSystem(StarSystemAPI system, InteractionDialogAPI dialog){

    }
}
