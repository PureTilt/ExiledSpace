package data.scripts.mapEffects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.world.systems.ptes_baseSystemScript;

import java.util.ArrayList;
import java.util.List;

public class ptes_monofleets implements ptes_baseEffectPlugin {
    @Override
    public void beforeGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {

    }

    @Override
    public void afterGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {
        for (CampaignFleetAPI fleet : genScript.spawnedFleets) {
            WeightedRandomPicker<FleetMemberAPI> variantPicker = new WeightedRandomPicker<>();
            List<PersonAPI> officers = new ArrayList<>();
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                officers.add(member.getCaptain());
                variantPicker.add(member, member.getFleetPointCost());
                fleet.getFleetData().removeFleetMember(member);
            }

            FleetMemberAPI replacementMember = variantPicker.pick();
            int shipAmount = Math.round((float) fleet.getFleetPoints() / replacementMember.getFleetPointCost());

            for (int i = 0; i < shipAmount; i++) {
                FleetMemberAPI newShip = fleet.getFleetData().addFleetMember(replacementMember.getVariant().getHullVariantId());
                newShip.getRepairTracker().applyCREvent(1, "repair");
                if (!officers.isEmpty()) {
                    //Global.getLogger(ptes_monofleets.class).info("officer added");
                    newShip.setCaptain(officers.get(0));
                    officers.remove(0);
                }
            }
            Global.getLogger(ptes_monofleets.class).info(fleet.getFleetPoints());
        }
    }
}
