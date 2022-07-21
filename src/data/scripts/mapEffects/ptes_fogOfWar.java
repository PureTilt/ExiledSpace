package data.scripts.mapEffects;

import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.world.systems.ptes_baseSystemScript;
import org.lwjgl.util.vector.Vector2f;

import java.util.Random;

public class ptes_fogOfWar implements ptes_baseEffectPlugin {

    @Override
    public void beforeGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {

    }

    @Override
    public void afterGeneration(StarSystemAPI system, ptes_baseSystemScript genScript) {


        int w = 128;
        int h = 128;

        StringBuilder string = new StringBuilder();
        for (int y = h - 1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
                string.append("x");
            }
        }
        SectorEntityToken nebula = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(string.toString(),
                w, h,
                "terrain", "nebula", 4, 4, null));
        nebula.getLocation().set(0, 0);

        NebulaTerrainPlugin nebulaPlugin = (NebulaTerrainPlugin)((CampaignTerrainAPI)nebula).getPlugin();
        NebulaEditor editor = new NebulaEditor(nebulaPlugin);

        editor.regenNoise();

        // good medium thickness: 0.6
        //editor.noisePrune(0.8f);

        // yes, star age here, despite using constellation age to determine if a nebula to all exists
        // basically: young star in old constellation will have lots of nebula, but of the constellation-age color
        //editor.noisePrune(0.75f);
        //editor.noisePrune(0.1f);

//		for (float f = 0.1f; f <= 0.9f; f += 0.05f) {
//			editor.noisePrune(f);
//		}

        editor.regenNoise();

        if (genScript.mapData.systemType != StarSystemGenerator.StarSystemType.NEBULA) {
            for (PlanetAPI planet : system.getPlanets()) {

                if (planet.getOrbit() != null && planet.getOrbit().getFocus() != null &&
                        planet.getOrbit().getFocus().getOrbit() != null) {
                    // this planet is orbiting something that's orbiting something
                    // its motion will be relative to its parent moving
                    // don't clear anything out for this planet
                    continue;
                }

                float clearThreshold = 0f; // clear everything by default
                float clearInnerRadius = 0f;
                float clearOuterRadius = 0f;
                Vector2f clearLoc = null;


                if (!planet.isStar() && !planet.isGasGiant()) {
                    clearThreshold = 1f - Math.min(0f, planet.getRadius() / 300f);
                    if (clearThreshold > 0.5f) clearThreshold = 0.5f;
                }

                Vector2f loc = planet.getLocation();
                if (planet.getOrbit() != null && planet.getOrbit().getFocus() != null) {
                    Vector2f focusLoc = planet.getOrbit().getFocus().getLocation();
                    float dist = Misc.getDistance(planet.getOrbit().getFocus().getLocation(), loc);
                    float width = planet.getRadius() * 4f + 100f;
                    if (planet.isStar()) {
                        StarCoronaTerrainPlugin corona = Misc.getCoronaFor(planet);
                        if (corona != null) {
                            width = corona.getParams().bandWidthInEngine * 4f;
                        }
                        PulsarBeamTerrainPlugin pulsar = Misc.getPulsarFor(planet);
                        if (pulsar != null) {
                            width = Math.max(width, pulsar.getParams().bandWidthInEngine * 0.5f);
                        }
                    }
                    clearLoc = focusLoc;
                    clearInnerRadius = dist - width / 2f;
                    clearOuterRadius = dist + width / 2f;
                } else if (planet.getOrbit() == null) {
                    float width = planet.getRadius() * 4f + 100f;
                    if (planet.isStar()) {
                        StarCoronaTerrainPlugin corona = Misc.getCoronaFor(planet);
                        if (corona != null) {
                            width = corona.getParams().bandWidthInEngine * 4f;
                        }
                        PulsarBeamTerrainPlugin pulsar = Misc.getPulsarFor(planet);
                        if (pulsar != null) {
                            width = Math.max(width, pulsar.getParams().bandWidthInEngine * 0.5f);
                        }
                    }
                    clearLoc = loc;
                    clearInnerRadius = 0f;
                    clearOuterRadius = width * 0.33f;
                }

                if (clearLoc != null) {
                    float min = nebulaPlugin.getTileSize() * 2f;
                    if (clearOuterRadius - clearInnerRadius < min) {
                        clearOuterRadius = clearInnerRadius + min;
                    }
                    editor.clearArc(clearLoc.x, clearLoc.y, clearInnerRadius, clearOuterRadius, 0, 360f, clearThreshold);
                }
            }
        }
        Random random = new Random();
        // add a spiral going from the outside towards the star
        float angleOffset = random.nextFloat() * 360f;
        editor.clearArc(0f, 0f, 30000, 31000 + 1000f * random.nextFloat(),
                angleOffset + 0f, angleOffset + 360f * (2f + random.nextFloat() * 2f), 0.01f, 0f);

        // do some random arcs
        int numArcs = (int) (8f + 6f * random.nextFloat());
        //int numArcs = 11;

        for (int i = 0; i < numArcs; i++) {
            //float dist = 4000f + 10000f * random.nextFloat();
            float dist = 15000f + 15000f * random.nextFloat();
            float angle = random.nextFloat() * 360f;

            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
            dir.scale(dist - (2000f + 8000f * random.nextFloat()));

            //float tileSize = nebulaPlugin.getTileSize();
            //float width = tileSize * (2f + 4f * random.nextFloat());
            float width = 400f * (1f + 2f * random.nextFloat());

            float clearThreshold = 0f + 0.5f * random.nextFloat();
            //clearThreshold = 0f;

            editor.clearArc(dir.x, dir.y, dist - width/2f, dist + width/2f, 0, 360f, clearThreshold);
        }
    }
}
