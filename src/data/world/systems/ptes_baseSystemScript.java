package data.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.Themes;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.scripts.plugins.ptes_faction;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

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

    FleetParamsV3 params;

    boolean devMode = Global.getSettings().isDevMode();



    public void generate(SectorAPI sector, SectorEntityToken RiftGate, StarSystemAPI system, InteractionDialogAPI dialog, ptes_mapItemInfo mapData) {
        //generate system if it doesnt exists
        if (system == null) {
            system = sector.createStarSystem("PoSMap");
            system.setName("Map");
            system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER);
            system.addTag(Tags.TRANSIENT);
            system.addTag(Tags.THEME_HIDDEN);
            system.generateAnchorIfNeeded();
            Global.getLogger(ptes_genericSystem.class).info("made new system");
            //Global.getLogger(ptes_genericSystem.class).info(system.getName());
        } else {
            Global.getLogger(ptes_genericSystem.class).info("found system");
            List<SectorEntityToken> cloneList = new ArrayList<>(system.getAllEntities());
            for (SectorEntityToken entity : cloneList) {
                system.removeEntity(entity);
            }
            system.generateAnchorIfNeeded();
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

    void generateFleetParams(){
        float EnemyFP = this.EnemyFP * faction.FPMulti;
        int fleetsToSpawn = MathUtils.getRandomNumberInRange(2, 4);
        fleetsToSpawn += Math.round(EnemyFP / 200f - 0.5f);
        params = new FleetParamsV3(
                null, // market
                new Vector2f(), // location
                faction.faction, // fleet's faction, if different from above, which is also used for source market picking
                null,
                getFleetType(EnemyFP),
                EnemyFP, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                200 // qualityBonus
        );


        params.maxNumShips = 100;
        params.minShipSize = Math.min(3, Math.round(EnemyFP / 200f - 0.5f));
        params.ignoreMarketFleetSizeMult = true;
    }

    public String getFleetType (float FP){
        if (FP <= 25) return FleetTypes.PATROL_SMALL;
        else if (FP <= 100) return FleetTypes.PATROL_MEDIUM;
        else return FleetTypes.PATROL_LARGE;
    }

    public void generateSystem(StarSystemAPI system, InteractionDialogAPI dialog){

    }
}
