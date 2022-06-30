package data.scripts.procgen;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.NameAssigner;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;

public class ptes_refittedNamer extends NameAssigner {

    public ptes_refittedNamer(Constellation constellation) {
        super(constellation);
    }

    @Override
    public void updateJumpPointNameFor(SectorEntityToken entity) {

    }

    @Override
    public void updateJumpPointDestinationNames(StarSystemAPI system) {

    }
}
