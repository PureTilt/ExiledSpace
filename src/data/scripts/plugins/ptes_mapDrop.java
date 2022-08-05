package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.Status;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.items.ptes_mapItemInfo;
import org.lazywizard.lazylib.MathUtils;
import com.fs.starfarer.api.impl.campaign.DModManager;

import java.util.*;

import static data.scripts.ptes_ModPlugin.*;

public class ptes_mapDrop extends BaseCampaignEventListener {

    public ptes_mapDrop(boolean permaRegister) {
        super(permaRegister);
    }

    public ptes_mapDrop() {
        super(true);
    }

    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
        WeightedRandomPicker<StarSystemGenerator.StarSystemType> picker = new WeightedRandomPicker<>();
        picker.addAll(EnumSet.allOf(StarSystemGenerator.StarSystemType.class));

        List<FleetMemberData> casualties = plugin.getLoserData().getOwnCasualties();
        float FleetFP = 0;
        if (plugin.getLoser().getMemoryWithoutUpdate().contains("$fleetFP")){
            FleetFP = (int) plugin.getLoser().getMemoryWithoutUpdate().get("$fleetFP");
            Global.getLogger(ptes_mapDrop.class).info(FleetFP);
        } else {
            for (FleetMemberData member : casualties) {
                if (member.getStatus() == Status.NORMAL) continue;
                FleetFP += member.getMember().getFleetPointCost();
            }
        }

        String factionID;
        if (plugin.getLoser().getMemoryWithoutUpdate().contains("$faction")){
            factionID = (String) plugin.getLoser().getMemoryWithoutUpdate().get("$faction");
        } else {
            factionID = plugin.getLoserData().getFleet().getFaction().getId();
        }
        float lootMult = 1;
        if (FactionMap.containsKey(factionID)){
            FleetFP *= 1f / FactionMap.get(factionID).FPMulti;
            lootMult = FactionMap.get(factionID).lootMulti;
        }

        if (FleetFP < 100) return;
        int totalMaps = 0;

        for (FleetMemberData member : casualties) {
            if (member.getStatus() == Status.NORMAL) continue;
            float ShipFP = member.getMember().getFleetPointCost();
            //Global.getLogger(ptes_mapDrop.class).info("Enemy lost: " + member.getMember().getVariant().getFullDesignationWithHullName() + "" + ShipFP);

            // officers as prisoners
            //PersonAPI captain = member.getMember().getCaptain();
            /*

             */
            float chanceMult = (float) Math.pow(0.5f, totalMaps);
            float num = (float) Math.pow(ShipFP, (1/1.2f)) * 0.01f;
            float ran = (float) Math.random();
            Global.getLogger(ptes_mapDrop.class).info(ShipFP + " " + num + "/" + ran);
            if (num * chanceMult >= ran) {
                Global.getLogger(ptes_mapDrop.class).info("generate map");
                int DMods = DModManager.getNumDMods(member.getMember().getVariant());
                int officerLevel = 0;
                for (MutableCharacterStatsAPI.SkillLevelAPI skill : member.getMember().getCaptain().getStats().getSkillsCopy()){
                    officerLevel += skill.getLevel();
                }
                WeightedRandomPicker<String> weightedEffects = new WeightedRandomPicker<>();
                for (Map.Entry<String, ptes_mapEffectEntry> entry : mapEffectsMap.entrySet()){
                    weightedEffects.add(entry.getKey(), entry.getValue().weight);
                }
                int SMods = member.getMember().getVariant().getSMods().size();
                List<String> mapEffects = new ArrayList<>();
                for (int i = -2; i < officerLevel; i ++){
                    if (Math.random() >= 0.9f){
                        String effect = weightedEffects.pick();
                        mapEffects.add(effect);
                        weightedEffects.remove(effect);
                        Global.getLogger(ptes_mapDrop.class).info("first effect:"  + effect);
                        break;
                    }
                }
                for (int i = -1; i < SMods; i ++){
                    if (Math.random() >= 0.8f){
                        String effect = weightedEffects.pick();
                        mapEffects.add(effect);
                        weightedEffects.remove(effect);
                        Global.getLogger(ptes_mapDrop.class).info("second effect:"  + effect);
                        break;
                    }
                }

                float FP = FleetFP * MathUtils.getRandomNumberInRange(0.9f, 1.15f);
                Random chance = new Random(member.getMember().getId().hashCode());
                if (chance.nextFloat() >= 0.66f || !FactionMap.containsKey(factionID)) {
                    factionID = weightedFactions.pick().faction;
                }

                if (FactionMap.get(factionID).subFactions.size() > 1){
                    WeightedRandomPicker<String> subFactionsPicker = new WeightedRandomPicker<>();
                    for (Map.Entry<String, Float> entry : FactionMap.get(factionID).subFactions.entrySet()){
                        subFactionsPicker.add(entry.getKey(), entry.getValue());
                    }
                    factionID = subFactionsPicker.pick();
                }
                Global.getLogger(ptes_mapDrop.class).info("map faction: "  + factionID);

                loot.addSpecial(new ptes_mapItemInfo("pos_map", null, Math.round(FP), Math.round(FP * MathUtils.getRandomNumberInRange(0.75f, 1.25f) * lootMult * (1 - (DMods * 0.075f))), factionID, picker.pick(), mapEffects), 1);

                Global.getLogger(ptes_mapDrop.class).info("map added");
                totalMaps++;
            }
        }
    }
}
