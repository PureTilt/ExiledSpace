package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.Status;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.items.ptes_mapItemInfo;
import org.lazywizard.lazylib.MathUtils;

import java.util.*;

import static data.scripts.ptes_ModPlugin.*;

public class ptes_mapDrop extends BaseCampaignEventListener {

    private void logger(String text) {
        if (Global.getSettings().isDevMode())
            Global.getLogger(ptes_mapDrop.class).info(text);
    }

    public ptes_mapDrop(boolean permaRegister) {
        super(permaRegister);
    }

    public ptes_mapDrop() {
        super(false);
    }

    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
        WeightedRandomPicker<StarSystemGenerator.StarSystemType> picker = new WeightedRandomPicker<>();
        picker.addAll(EnumSet.allOf(StarSystemGenerator.StarSystemType.class));

        List<FleetMemberData> casualties = plugin.getLoserData().getOwnCasualties();
        float FleetFP = 0;
        if (plugin.getLoser().getMemoryWithoutUpdate().contains("$fleetFP")) {
            FleetFP = Math.round((float) plugin.getLoser().getMemoryWithoutUpdate().get("$fleetFP"));
            logger(FleetFP + "");
        } else {
            for (FleetMemberData member : casualties) {
                if (member.getStatus() == Status.NORMAL) continue;
                FleetFP += member.getMember().getFleetPointCost();
            }
        }

        String factionID;
        if (plugin.getLoser().getMemoryWithoutUpdate().contains("$faction")) {
            factionID = (String) plugin.getLoser().getMemoryWithoutUpdate().get("$faction");
            logger("grabbed ID: " + factionID + "\\" + FactionMap.containsKey(factionID));
        } else {
            factionID = plugin.getLoserData().getFleet().getFaction().getId();
        }
        float lootMult = 1;
        if (FactionMap.containsKey(factionID)) {
            FleetFP *= 1f / FactionMap.get(factionID).FPMulti;
            lootMult = FactionMap.get(factionID).lootMulti;
        }

        if (FleetFP < 100) return;

        WeightedRandomPicker<ptes_mapItemInfo> mapList = new WeightedRandomPicker<>();

        for (FleetMemberData member : casualties) {
            if (member.getStatus() == Status.NORMAL || member.getMember().isFighterWing()) continue;

            float ShipFP = member.getMember().getFleetPointCost();
            Random chance = new Random(member.getMember().getId().hashCode());

            float mapWeight = 10 + ShipFP;
            //mapWeight += (float) Math.pow(ShipFP, (1 / 1.2f));
            mapWeight *= getRandomNumberInRange(chance.nextFloat(), 0.8f, 1.2f);
            //Global.getLogger(ptes_mapDrop.class).info("Enemy lost: " + member.getMember().getVariant().getFullDesignationWithHullName() + "" + ShipFP);


            //do pre-calculations
            float FPMulti = getRandomNumberInRange(chance.nextFloat(), 0.9f, 1.15f);
            float LPMulti = getRandomNumberInRange(chance.nextFloat(), 0.75f, 1.25f);

            mapWeight *= FPMulti;
            mapWeight *= LPMulti;

            int FP = Math.round(FleetFP * FPMulti);

            //add map mods
            WeightedRandomPicker<String> weightedEffects = new WeightedRandomPicker<>();
            for (Map.Entry<String, ptes_mapEffectEntry> entry : mapEffectsMap.entrySet()) {
                weightedEffects.add(entry.getKey(), entry.getValue().weight);
            }
            List<String> mapEffects = new ArrayList<>();
            for (int i = -2; i < member.getMember().getCaptain().getStats().getLevel(); i++) {
                if (chance.nextFloat() >= 0.9f) {
                    String effect = weightedEffects.pick();
                    mapEffects.add(effect);
                    weightedEffects.remove(effect);
                    //Global.getLogger(ptes_mapDrop.class).info("first effect:" + effect);
                    mapWeight += mapEffectsMap.get(effect).cost;
                    break;
                }
            }
            int SMods = member.getMember().getVariant().getSMods().size();
            for (int i = -1; i < SMods; i++) {
                if (chance.nextFloat() >= 0.8f) {
                    String effect = weightedEffects.pick();
                    mapEffects.add(effect);
                    weightedEffects.remove(effect);
                    //Global.getLogger(ptes_mapDrop.class).info("second effect:" + effect);
                    mapWeight += mapEffectsMap.get(effect).cost;
                    break;
                }
            }


            if (chance.nextFloat() >= 0.66f || !FactionMap.containsKey(factionID)) {
                factionID = weightedFactions.pick().faction;
            }

            if (FactionMap.get(factionID).subFactions.size() > 1) {
                WeightedRandomPicker<String> subFactionsPicker = new WeightedRandomPicker<>();
                for (Map.Entry<String, Float> entry : FactionMap.get(factionID).subFactions.entrySet()) {
                    subFactionsPicker.add(entry.getKey(), entry.getValue());
                }
                factionID = subFactionsPicker.pick();
            }
            //Global.getLogger(ptes_mapDrop.class).info("map faction: "  + factionID);

            int DMods = DModManager.getNumDMods(member.getMember().getVariant());

            int lootPoints = Math.round(FP * LPMulti * lootMult * (1 - (DMods * 0.075f)));
            mapList.add(new ptes_mapItemInfo("pos_map", null, FP, lootPoints, factionID, picker.pick(), mapEffects), mapWeight);

            logger(FP + "\\" + lootPoints + "\\" + mapEffects.size() + "\\" + mapWeight);
            //loot.addSpecial(new ptes_mapItemInfo("pos_map", null, FP, lootPoints, factionID, picker.pick(), mapEffects), 1);
            //Global.getLogger(ptes_mapDrop.class).info("map added");

        }
        int maps = mapList.getItems().size();
        logger("Num maps: " + mapList.getItems().size());

        float random = getRandomNumberInRange((float) Math.random(), 0f, 0.5f);
        random += maps * 0.01f;
        logger("Will get: " + random);
        while (random > 1f){
            loot.addSpecial(mapList.pickAndRemove(), 1);
            random--;
        }
        if (Math.random() <= random){
            loot.addSpecial(mapList.pickAndRemove(), 1);
        }
    }

    public float getRandomNumberInRange(float random, float min, float max) {
        return random * (max - min) + min;
    }
}
