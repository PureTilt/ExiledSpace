package data.scripts.mapEffects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
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
            if (member.isFighterWing()) continue;
            FleetMemberAPI newMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
            newMember.setShipName(member.getShipName());

            PersonAPI oldPerson = member.getCaptain();
            /*
            PersonAPI newPerson = OfficerManagerEvent.createOfficer(oldPerson.getFaction(), 0, OfficerManagerEvent.SkillPickPreference.ANY,
                    false, fleet, false, false, -1, MathUtils.getRandom());

             */

            PersonAPI newPerson = fleet.getFaction().createRandomPerson();
            newPerson.setFleet(fleet);

            newPerson.getStats().setLevel(oldPerson.getStats().getLevel());
            newPerson.setName(oldPerson.getName());
            newPerson.setPersonality(oldPerson.getPersonalityAPI().getId());
            newPerson.setPortraitSprite(oldPerson.getPortraitSprite());
            newPerson.setPostId(oldPerson.getPostId());
            newPerson.setRankId(oldPerson.getRankId());

            newMember.setCaptain(newPerson);

            for (MutableCharacterStatsAPI.SkillLevelAPI skill : oldPerson.getStats().getSkillsCopy()){
                newPerson.getStats().setSkillLevel(skill.getSkill().getId(), skill.getLevel());
            }

            if (Global.getSector().getPlayerFleet().getCommander() == oldPerson) {
                fleet.setCommander(newPerson);
            }


            fleet.getFleetData().addFleetMember(newMember);
        }
        fleet.setName(Global.getSector().getPlayerFleet().getCommander().getNameString() + " fleet");
        system.spawnFleet(system.getCenter(),
                MathUtils.getRandomNumberInRange(3000, 5000) * (Math.random() >= 0.5f ? 1 : -1),
                MathUtils.getRandomNumberInRange(3000, 5000) * (Math.random() >= 0.5f ? 1 : -1),
                fleet);
    }
}
