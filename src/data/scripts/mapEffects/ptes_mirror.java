package data.scripts.mapEffects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.world.systems.ptes_baseSystemScript;
import org.lazywizard.lazylib.MathUtils;

public class ptes_mirror implements ptes_baseEffectPlugin {

    @Override
    public void beforeGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {

    }

    @Override
    public void afterGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {
        CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet("unknown", FleetTypes.PATROL_LARGE,null);
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()){
            fleet.getFleetData().addFleetMember(member);
        }
        fleet.setName(Global.getSector().getPlayerFleet().getCommander().getNameString() + " fleet");
        system.spawnFleet(system.getCenter(),
                MathUtils.getRandomNumberInRange(3000, 5000) * MathUtils.getRandomNumberInRange(-1, 1),
                MathUtils.getRandomNumberInRange(3000, 5000) * MathUtils.getRandomNumberInRange(-1, 1),
                fleet);
    }
}
