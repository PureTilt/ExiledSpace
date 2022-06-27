package data.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.*;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.ptes_ModPlugin;
import data.scripts.plugins.ptes_salvageEntity;
import data.scripts.procgen.ptes_refittedProcGen;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import static com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.NEBULA_NONE;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.addSalvageEntity;
import static data.scripts.ptes_ModPlugin.*;

public class ptes_genericSystem extends ptes_baseSystemScript {

    final LinkedHashMap<BaseThemeGenerator.LocationType, Float> locationsList = new LinkedHashMap<>();

    {
        locationsList.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 10f);
        locationsList.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, 20f);
        locationsList.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, 20f);
        locationsList.put(BaseThemeGenerator.LocationType.STAR_ORBIT, 2f);
        locationsList.put(BaseThemeGenerator.LocationType.IN_SMALL_NEBULA, 20f);
        locationsList.put(BaseThemeGenerator.LocationType.NEAR_STAR, 2f);
        locationsList.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 10f);
        locationsList.put(BaseThemeGenerator.LocationType.L_POINT, 5f);
        locationsList.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 0f);
    }

    List<SectorEntityToken> entitiesToDefend = new ArrayList<>();

    public void generateSystem(StarSystemAPI system, InteractionDialogAPI dialog) {
        float pad = 3f;
        float spad = 5f;
        float opad = 10f;


        FactionAPI factionAPI = Global.getSector().getFaction(faction.faction);
        TooltipMakerAPI tooltip = dialog.getTextPanel().beginTooltip();

        TooltipMakerAPI image = tooltip.beginImageWithText(Global.getSector().getFaction(mapData.FactionId).getCrest(), 48);
        image.addPara("This location contains fleets which mimics " + factionAPI.getDisplayName() + ".", pad, factionAPI.getColor(), factionAPI.getDisplayName());

        image.addPara("Power of fleets: " + EnemyFP, pad , Misc.getHighlightColor(), EnemyFP + "");
        image.addPara("Loot quantity: " + LootPoints, pad , Misc.getHighlightColor(), LootPoints + "");

        tooltip.addImageWithText(opad);

        dialog.getTextPanel().addTooltip();

        //generate stuff in system
        generatePlanets(system);
        generatePointsOfInterest(system);
        generateFleets(system);
        system.updateAllOrbits();

        //choose BG
        String BG = ptes_ModPlugin.backGrounds.get(MathUtils.getRandomNumberInRange(0, ptes_ModPlugin.backGrounds.size() - 1));
        system.setBackgroundTextureFilename("graphics/backgrounds/" + BG);
        /*

        WeightedRandomPicker<String> graveyardFactions = new WeightedRandomPicker<>();
        graveyardFactions.add("omega", 10);

        addShipGraveyard(mainStar, graveyardFactions);
        */
    }

    void generatePlanets(StarSystemAPI system) {
        StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.ANY);
        params.forceNebula = false;
        params.maxStars = 1;
        params.minStars = 1;
        params.name = "System";
        params.secondaryName = params.name;
        params.location = system.getLocation();
        params.age = StarAge.ANY;
        ptes_refittedProcGen gen = new ptes_refittedProcGen(params);
        gen.initGen(system, Global.getSector(), NEBULA_NONE, mapData.systemType);
        gen.generateSystem();
    }

    void generatePointsOfInterest(StarSystemAPI system) {
        entitiesToDefend.clear();
        WeightedRandomPicker<ptes_salvageEntity> salvageEntitiesSmall = new WeightedRandomPicker<>();
        WeightedRandomPicker<ptes_salvageEntity> salvageEntitiesBig = new WeightedRandomPicker<>();
        for (ptes_salvageEntity entity : salvageList) {
            if (entity.factions.size() == 0 || entity.factions.contains(faction.faction)) {
                if (entity.cost < 100) {
                    salvageEntitiesSmall.add(entity, entity.weight);
                    Global.getLogger(ptes_genericSystem.class).info("added to small: " + entity.id + " " + entity.cost);
                } else {
                    salvageEntitiesBig.add(entity, entity.weight);
                    Global.getLogger(ptes_genericSystem.class).info("added to big: " + entity.id + " " + entity.cost);
                }
            }
        }
        float pointsSpend = 0;
        WeightedRandomPicker<BaseThemeGenerator.EntityLocation> validPoints = BaseThemeGenerator.getLocations(new Random(), system, 50f, locationsList);

        Global.getLogger(ptes_genericSystem.class).info(LootPoints);
        while (pointsSpend < LootPoints) {
            BaseThemeGenerator.EntityLocation placeToSpawn = null;
            while (placeToSpawn == null) {
                placeToSpawn = validPoints.pick();
                if (placeToSpawn.orbit == null || placeToSpawn.orbit.getFocus() instanceof CustomCampaignEntityAPI) placeToSpawn = null;
            }
            float pointsLeft = LootPoints - pointsSpend;
            ptes_salvageEntity entity;
            if (pointsLeft >= 150) {
                entity = salvageEntitiesBig.pick();
            } else {
                entity = salvageEntitiesSmall.pick();
            }
            if (entity == null) break;
            Global.getLogger(ptes_genericSystem.class).info(entity.id + " " + entity.cost);
            pointsSpend += entity.cost;
            CustomCampaignEntityAPI spawnedEntity = (CustomCampaignEntityAPI) addSalvageEntity(system, entity.id, Factions.NEUTRAL);
            if (entity.cost >= 100) entitiesToDefend.add(spawnedEntity);

            spawnedEntity.setOrbit(placeToSpawn.orbit);
            spawnedEntity.setCircularOrbitPointingDown(spawnedEntity.getOrbitFocus(), MathUtils.getRandomNumberInRange(0, 360), spawnedEntity.getCircularOrbitRadius(), spawnedEntity.getCircularOrbitPeriod());
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



    void generateFleets(StarSystemAPI system) {
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
        /*
        params.officerLevelBonus = Math.round(EnemyFP / 250f - 0.5f);
        params.officerNumberMult = Math.round(EnemyFP / 100f - 0.5f);
        params.officerNumberBonus = Math.round(EnemyFP / 100f - 0.5f);
        params.officerLevelLimit = 5 + Math.round(EnemyFP / 250f - 0.5f);
        params.commanderLevelLimit = 7 + Math.round(EnemyFP / 200f - 0.5f);

         */
        //params.combatPts *= 1 - EnemyFP / 2000f;

        while (fleetsToSpawn > 0){

            CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
            fleet.getMemoryWithoutUpdate().set("$faction", faction.faction);
            fleet.setFaction("uknown", true);
            system.addEntity(fleet);

            if (entitiesToDefend.size() != 0){
                SectorEntityToken entity = entitiesToDefend.get(MathUtils.getRandomNumberInRange(0, entitiesToDefend.size() - 1));
                fleet.setLocation(entity.getLocation().x, entity.getLocation().y);
                fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, entity, 9999999999f);
                entitiesToDefend.remove(entity);
            } else  {
                fleet.setLocation(MathUtils.getRandomNumberInRange(2000, 5000) * MathUtils.getRandomNumberInRange(-1, 1), MathUtils.getRandomNumberInRange(1000, 5000) * MathUtils.getRandomNumberInRange(-1, 1));
                fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, system.getCenter(), 9999999999f);
            }

//          fleet.removeAbility(Abilities.EMERGENCY_BURN);
            fleet.removeAbility(Abilities.SENSOR_BURST);
            fleet.removeAbility(Abilities.GO_DARK);
//
            // to make sure they attack the player on sight when player's transponder is off
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, false);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);

            fleet.getStats().getFleetwideMaxBurnMod().modifyFlat("ptes", 2);
            fleet.getStats().getSensorRangeMod().modifyFlat("ptes", 500);

            fleetsToSpawn--;
        }
    }

    public String getFleetType (float FP){
        if (FP <= 25) return FleetTypes.PATROL_SMALL;
        else if (FP <= 100) return FleetTypes.PATROL_MEDIUM;
        else return FleetTypes.PATROL_LARGE;
    }
}