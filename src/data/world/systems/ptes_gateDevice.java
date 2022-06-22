package data.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

public class ptes_gateDevice {

    public float syncOrbitDays = -80f;

    public void generate(SectorAPI sector) {

        LocationAPI hyper = sector.getHyperspace();

        SectorEntityToken gate = hyper.addCustomEntity("ptpos_RiftGate", "Rift Gate", "ptpos_gate", "neutral");

        MarketAPI market = Global.getFactory().createMarket("vic_ApotheosisAbandonedStationMarket","Abandoned Station",0);
            market.setPrimaryEntity(gate);
            market.setFactionId(gate.getFaction().getId());
            market.addIndustry(Industries.SPACEPORT);
            market.addCondition(Conditions.ABANDONED_STATION);
            market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
            ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);

        gate.setMarket(market);
        gate.setCustomDescriptionId("ptes_gate");

        gate.addTag("rift_gate");
        gate.setDiscoverable(true);
        gate.setDiscoveryXP(2000f);
        gate.setSensorProfile(500f);
        gate.setLocation(500, -500);

        cleanup(gate);
    }

    private void cleanup(SectorEntityToken entity) {
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = 200f;
        editor.clearArc(entity.getLocation().x, entity.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(entity.getLocation().x, entity.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

    }
}