package data.world.systems;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.mapObjectives.ptes_baseMapObjective;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.scripts.plugins.ptes_faction;
import data.scripts.plugins.ptes_mapObjectiveEntry;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

import static data.scripts.ptes_ModPlugin.*;
import static data.scripts.ptes_ModPlugin.mapObjectivesMap;

public class ptes_baseSystemScript {

    public float
            EnemyFP,
            LootPoints;

    int amountOfFleets;

    public List<CampaignFleetAPI> spawnedFleets = new ArrayList<>();
    public List<SectorEntityToken> spawnedLoot = new ArrayList<>();

    public ptes_faction faction;
    public ptes_mapItemInfo mapData;

    List<String> effects;


    public FleetParamsV3 params;

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
            system.setName("Map");
            List<SectorEntityToken> cloneList = new ArrayList<>(system.getAllEntities());
            for (SectorEntityToken entity : cloneList) {
                system.removeEntity(entity);
            }
            system.generateAnchorIfNeeded();
        }
        spawnedFleets.clear();
        spawnedLoot.clear();

        //move system under gate just in case
        system.getLocation().set(RiftGate.getLocation());

        //set data
        EnemyFP = mapData.FP;
        LootPoints = mapData.LP;
        Collections.sort(mapData.effects, new Comparator<String>() {

            public int compare(String o1, String o2) {
                // compare two instance of `Score` and return `int` as result.
                return Integer.compare(mapEffectsMap.get(o1).order, mapEffectsMap.get(o2).order);
            }
        });

        this.mapData = mapData;
        this.effects = mapData.effects;
        faction = FactionMap.get(mapData.FactionId);

        generateFleetParams(EnemyFP, faction);
        //apply effects before gen
        for (String effectID : effects) {
            mapEffectsMap.get(effectID).effectClass.beforeGeneration(system, this);
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
        for (String effectID : effects) {
            mapEffectsMap.get(effectID).effectClass.afterGeneration(system, this);
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

        //add objective
        for (EveryFrameScript script : new ArrayList<>(system.getScripts())){
            system.removeScript(script);
        }
        if (mapData.objectiveID != null && mapObjectivesMap.containsKey(mapData.objectiveID)){
            try {
                ptes_baseMapObjective objectiveClass = (ptes_baseMapObjective) mapObjectivesMap.get(mapData.objectiveID).genClass.newInstance();
                objectiveClass.init(mapData, this);
                system.addScript(objectiveClass);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public FleetParamsV3 generateFleetParams(float EnemyFP, ptes_faction faction) {
        amountOfFleets = MathUtils.getRandomNumberInRange(2, 4);
        amountOfFleets += Math.round(EnemyFP / 200f - 0.5f);

        params = new FleetParamsV3(
                null, // market
                new Vector2f(), // location
                faction.faction, // fleet's faction, if different from above, which is also used for source market picking
                null,
                getFleetType(EnemyFP),
                EnemyFP * faction.FPMulti, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0 // qualityBonus
        );
        params.officerLevelLimit = 5;
        params.commanderLevelLimit = 7;
        params.maxNumShips = 100;
        params.minShipSize = Math.min(3, Math.round(EnemyFP / 200f - 0.5f));
        params.ignoreMarketFleetSizeMult = true;
        params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
        if (faction.quality != null){
            params.qualityOverride = faction.quality;
        }
        return params;
    }

    public String getFleetType(float FP) {
        if (FP <= 25) return FleetTypes.PATROL_SMALL;
        else if (FP <= 100) return FleetTypes.PATROL_MEDIUM;
        else return FleetTypes.PATROL_LARGE;
    }

    public void generateSystem(StarSystemAPI system, InteractionDialogAPI dialog) {

    }
}
