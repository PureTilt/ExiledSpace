package data.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.plugins.ptes_salvageEntity;
import data.scripts.procgen.ptes_refittedProcGen;
import data.scripts.ptes_ModPlugin;
import org.lazywizard.lazylib.MathUtils;
import data.scripts.plugins.ptes_DPRReductionCalc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import static com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.NEBULA_NONE;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.addSalvageEntity;
import static data.scripts.ids.ptes_factions.MAP_FACTION;
import static data.scripts.ptes_ModPlugin.salvageList;

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


    public void generateSystem(StarSystemAPI system, InteractionDialogAPI dialog) {
        float pad = 3f;
        float spad = 5f;
        float opad = 10f;


        FactionAPI factionAPI = Global.getSector().getFaction(faction.faction);
        TooltipMakerAPI tooltip = dialog.getTextPanel().beginTooltip();

        TooltipMakerAPI image = tooltip.beginImageWithText(Global.getSector().getFaction(mapData.FactionId).getCrest(), 48);
        image.addPara("This location contains fleets which mimics " + factionAPI.getDisplayName() + ".", pad, factionAPI.getColor(), factionAPI.getDisplayName());

        image.addPara("Power of fleets: " + EnemyFP, pad, Misc.getHighlightColor(), EnemyFP + "");
        image.addPara("Loot quantity: " + LootPoints, pad, Misc.getHighlightColor(), LootPoints + "");

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
        //namer.assignStructuralNames(system,);
    }

    void generatePointsOfInterest(StarSystemAPI system) {
        spawnedLoot.clear();
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
                if (placeToSpawn.orbit == null || placeToSpawn.orbit.getFocus() instanceof CustomCampaignEntityAPI)
                    placeToSpawn = null;
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
            if (entity.cost >= 100) spawnedLoot.add(spawnedEntity);

            spawnedEntity.setOrbit(placeToSpawn.orbit);
            spawnedEntity.setCircularOrbitPointingDown(spawnedEntity.getOrbitFocus(), MathUtils.getRandomNumberInRange(0, 360), spawnedEntity.getCircularOrbitRadius(), spawnedEntity.getCircularOrbitPeriod());
        }
    }


    void generateFleets(StarSystemAPI system) {
        /*
        params.officerLevelBonus = Math.round(EnemyFP / 250f - 0.5f);
        params.officerNumberMult = Math.round(EnemyFP / 100f - 0.5f);
        params.officerNumberBonus = Math.round(EnemyFP / 100f - 0.5f);
        params.officerLevelLimit = 5 + Math.round(EnemyFP / 250f - 0.5f);
        params.commanderLevelLimit = 7 + Math.round(EnemyFP / 200f - 0.5f);

         */
        //params.combatPts *= 1 - EnemyFP / 2000f;

        int fleetsSpawned = 0;
        List<SectorEntityToken> pointsToDefend = new ArrayList<>(spawnedLoot);

        while (amountOfFleets > fleetsSpawned) {

            CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
            if (faction.factionOverride != null) fleet.getMemoryWithoutUpdate().set("$faction", faction.factionOverride);
            else fleet.getMemoryWithoutUpdate().set("$faction", faction.faction);

            fleet.getMemoryWithoutUpdate().set("$fleetFP", fleet.getFleetPoints());

            //TODO: decide on FP threshold
            float DPreduction = ptes_DPRReductionCalc.DPRReduction(fleet.getFleetPoints());
            if (DPreduction != 1f) {
                fleet.getMemoryWithoutUpdate().set("$difficultyDPMulty", DPreduction);
                fleet.getCommander().getStats().setSkillLevel("vic_difficultyDPReduction",1);
            }

            fleet.setFaction(MAP_FACTION, true);
            system.addEntity(fleet);

            if (pointsToDefend.size() != 0) {
                SectorEntityToken entity = pointsToDefend.get(MathUtils.getRandomNumberInRange(0, pointsToDefend.size() - 1));
                fleet.setLocation(entity.getLocation().x, entity.getLocation().y);
                fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, entity, 99999999f);
                pointsToDefend.remove(entity);
            } else {
                fleet.setLocation(MathUtils.getRandomNumberInRange(3000, 5000) * MathUtils.getRandomNumberInRange(-1, 1), MathUtils.getRandomNumberInRange(3000, 5000) * MathUtils.getRandomNumberInRange(-1, 1));
                fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, system.getCenter(), 9999999999f);
            }

            //fleet.removeAbility(Abilities.EMERGENCY_BURN);
            //fleet.removeAbility(Abilities.SENSOR_BURST);
            fleet.removeAbility(Abilities.GO_DARK);

            // to make sure they attack the player on sight when player's transponder is off
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, false);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, false);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY, true);

            fleet.setNoAutoDespawn(true);
            fleet.getStats().getFleetwideMaxBurnMod().modifyFlat("ptes", 2);
            fleet.getStats().getSensorRangeMod().modifyFlat("ptes", 500);


            spawnedFleets.add(fleet);
            fleetsSpawned++;
        }
    }
}