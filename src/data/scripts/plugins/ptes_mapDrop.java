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

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static data.scripts.ptes_ModPlugin.FactionMap;
import static data.scripts.ptes_ModPlugin.weightedFactions;

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
        for (FleetMemberData member : casualties) {
            if (member.getStatus() == Status.NORMAL) continue;
            FleetFP += member.getMember().getFleetPointCost();
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
        int totalMaps = 1;
        for (FleetMemberData member : casualties) {
            if (member.getStatus() == Status.NORMAL) continue;
            float ShipFP = member.getMember().getFleetPointCost();
            int Dmods = DModManager.getNumDMods(member.getMember().getVariant());
            int officerLevel = 0;
            for (MutableCharacterStatsAPI.SkillLevelAPI skill : member.getMember().getCaptain().getStats().getSkillsCopy()){
                officerLevel += skill.getLevel();
            }
            //Global.getLogger(ptes_mapDrop.class).info("Enemy lost: " + member.getMember().getVariant().getFullDesignationWithHullName() + "" + ShipFP);

            // officers as prisoners
            //PersonAPI captain = member.getMember().getCaptain();
            /*
            String commodity = "survey_data_1";
            if (FleetFP <= 25) commodity = "survey_data";
            else if (FleetFP <= 50) commodity = "survey_data_2";
            else if (FleetFP <= 75) commodity = "survey_data_3";
            else if (FleetFP <= 100) commodity = "survey_data_4";
            else commodity = "survey_data_5";
            loot.addCommodity(commodity,1);

             */
            float chanceMult = (float) Math.pow(0.5f, totalMaps);
            float num = (float) Math.pow(ShipFP, (1/1.2f)) * 0.01f;
            float ran = (float) Math.random();
            //Global.getLogger(ptes_mapDrop.class).info(ShipFP + " " + num + "/" + ran);
            if (num * chanceMult >= ran) {
                float FP = FleetFP * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
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

                loot.addSpecial(new ptes_mapItemInfo("pos_map", null, Math.round(FP), Math.round(FP * MathUtils.getRandomNumberInRange(0.75f, 1.25f) * lootMult * (1 - (Dmods * 0.075f))), factionID, picker.pick()), 1);
                totalMaps++;
            }
        }
        //loot.addCommodity("lobster",1);
    }

    /*
    public static ptes_mapDrop create()
    {
        ptes_mapDrop manager = getManager();
        if (manager != null)
            return manager;

        Map<String, Object> data = Global.getSector().getPersistentData();
        manager = new ptes_mapDrop();
        data.put(MANAGER_MAP_KEY, manager);
        return manager;
    }

     */
}
