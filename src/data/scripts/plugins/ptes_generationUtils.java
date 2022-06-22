package data.scripts.plugins;

import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import static com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.random;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.addSalvageEntity;

public class ptes_generationUtils {

    public static void addShipGraveyard(SectorEntityToken focus, WeightedRandomPicker<String> factions) {
        int numShips = random.nextInt(9) + 3;
        //numShips = 12;

        WeightedRandomPicker<Float> bands = new WeightedRandomPicker<Float>(random);
        for (int i = 0; i < numShips + 5; i++) {
            bands.add((float) (140 + i * 20), (i + 1) * (i + 1));
        }

//		WeightedRandomPicker<String> factions = new WeightedRandomPicker<String>(random);
//		factions.add(Factions.TRITACHYON, 10f);
//		factions.add(Factions.HEGEMONY, 7f);
//		factions.add(Factions.INDEPENDENT, 3f);

        for (int i = 0; i < numShips; i++) {
            float radius = bands.pickAndRemove();

            DerelictShipEntityPlugin.DerelictShipData params = DerelictShipEntityPlugin.createRandom(factions.pick(), null, random, DerelictShipEntityPlugin.getDefaultSModProb());
            if (params != null) {
                CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) addSalvageEntity(random,
                        focus.getContainingLocation(),
                        Entities.WRECK, Factions.NEUTRAL, params);
                entity.setDiscoverable(true);
                float orbitDays = radius / (5f + random.nextFloat() * 10f);
                entity.setCircularOrbit(focus, random.nextFloat() * 360f, radius, orbitDays);
            }
        }
    }

}
