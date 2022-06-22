package data.scripts.procgen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.*;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

public class ptes_refittedProcGen extends StarSystemGenerator{

	boolean randomSystemType = true;
	public ptes_refittedProcGen(CustomConstellationParams params) {
		super(params);
	}

	public StarSystemType getRandomType(){
		WeightedRandomPicker<StarSystemType> picker = new WeightedRandomPicker<>();
		picker.addAll(EnumSet.allOf(StarSystemType.class));
		return picker.pick();
	}

	public void generateSystem(){
		generateSystem(new Vector2f());
	}

	@Override
	public void generateSystem(Vector2f loc) {
		//index++;

		if (randomSystemType) systemType = getRandomType();

//		if (systemType == StarSystemType.NEBULA) {
//			System.out.println("wfwefwe1231232");
//		}

		String uuid = Misc.genUID();

//		String id = "system_" + index;
//		String name = "System " + index;

		String id = "system_" + uuid;
		String name = "System " + uuid;

//		String base = constellationName.nameWithRomanSuffixIfAny;
//		if (constellationName.secondaryWithRomanSuffixIfAny != null) {
//			base = constellationName.secondaryWithRomanSuffixIfAny;
//		}
//		String name = Global.getSettings().getNextGreekLetter(constellationName) + " " + base;
//		String id = name.toLowerCase();


		//if (systemType == StarSystemType.NEBULA) name += " Nebula";

		/*
		if (!initSystem(name, loc)) {
			cleanup();
			return;
		}

		 */

		star = null;
		secondary = null;
		tertiary = null;
		systemCenter = null;



		if (!addStars(id)) {
			cleanup();
			return;
		}

//		if (systemType == StarSystemType.NEBULA) {
//			if (star.getSpec().isBlackHole()) {
//				System.out.println("wefwefew");
//			}
//		}

		updateAgeAfterPickingStar();

		float binaryPad = 1500f;

		float maxOrbitRadius = MAX_ORBIT_RADIUS;
		if (systemType == StarSystemType.BINARY_FAR ||
				systemType == StarSystemType.TRINARY_1CLOSE_1FAR ||
				systemType == StarSystemType.TRINARY_2FAR) {
			maxOrbitRadius -= FAR_MAX_ORBIT_RADIUS + binaryPad;
		}
		GenResult result = addPlanetsAndTerrain(MAX_ORBIT_RADIUS);
		//addJumpPoints(result);
		float primaryOrbitalRadius = star.getRadius();
		if (result != null) {
			primaryOrbitalRadius = result.orbitalWidth * 0.5f;
		}

		// add far stars, if needed
		float orbitAngle = random.nextFloat() * 360f;
		float baseOrbitRadius = primaryOrbitalRadius + binaryPad;
		float orbitDays = baseOrbitRadius / (3f + random.nextFloat() * 2f);
		if (systemType == StarSystemType.BINARY_FAR && secondary != null) {
			addFarStar(secondary, orbitAngle, baseOrbitRadius, orbitDays);
		} else if (systemType == StarSystemType.TRINARY_1CLOSE_1FAR && tertiary != null) {
			addFarStar(tertiary, orbitAngle, baseOrbitRadius, orbitDays);
		} else if (systemType == StarSystemType.TRINARY_2FAR) {
			addFarStar(secondary, orbitAngle, baseOrbitRadius, orbitDays);
			addFarStar(tertiary, orbitAngle + 60f + 180f * random.nextFloat(), baseOrbitRadius, orbitDays);
		}


		if (systemType == StarSystemType.NEBULA) {
			star.setSkipForJumpPointAutoGen(true);
		}

		addJumpPoints(result, false);

		if (systemType == StarSystemType.NEBULA) {
			system.removeEntity(star);
			StarCoronaTerrainPlugin coronaPlugin = Misc.getCoronaFor(star);
			if (coronaPlugin != null) {
				system.removeEntity(coronaPlugin.getEntity());
			}
			system.setStar(null);
			system.initNonStarCenter();
			for (SectorEntityToken entity : system.getAllEntities()) {
				if (entity.getOrbitFocus() == star ||
						entity.getOrbitFocus() == system.getCenter()) {
					entity.setOrbit(null);
				}
			}
			system.getCenter().addTag(Tags.AMBIENT_LS);
		}

		//system.autogenerateHyperspaceJumpPoints(true, false);

		if (systemType == StarSystemType.NEBULA) {
			//system.addEntity(star);
			system.setStar(star);
			//system.removeEntity(system.getCenter());
			//system.setCenter(null);
		}

		//addStableLocations();

		addSystemwideNebula();
	}

	@Override
	public PlanetSpecAPI pickStar(StarAge age) {
		if (params != null && !params.starTypes.isEmpty()) {
			String id = params.starTypes.remove(0);
			//if (id.equals("black_hole") && systemType == StarSystemType.NEBULA) id = "nebula_center_old";
			for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
				if (spec.getPlanetType().equals(id)) {
					Object test = Global.getSettings().getSpec(StarGenDataSpec.class, id, true);
					if (test == null) continue;
					StarGenDataSpec data = (StarGenDataSpec) test;
					boolean hasTag = data.hasTag(StarSystemType.NEBULA.name());
					boolean nebType = systemType == StarSystemType.NEBULA;
					boolean nebulaStatusOk = hasTag == nebType;
					if (nebulaStatusOk) {
						return spec;
					}
				}
			}
			// doesn't work because the actual class the spec is registered under is PlanetSpec,
			// not the API-exposed PlanetSpecAPI
			//return (PlanetSpecAPI) Global.getSettings().getSpec(PlanetSpecAPI.class, id, true);
		}

		WeightedRandomPicker<PlanetSpecAPI> picker = new WeightedRandomPicker<PlanetSpecAPI>();
		for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
			if (!spec.isStar()) continue;
			if (spec.isPulsar()) continue;

			String id = spec.getPlanetType();
			Object test = Global.getSettings().getSpec(StarGenDataSpec.class, id, true);
			if (test == null) continue;
			StarGenDataSpec data = (StarGenDataSpec) test;
			boolean hasTag = data.hasTag(StarSystemType.NEBULA.name());
			boolean nebType = systemType == StarSystemType.NEBULA;
			boolean nebulaStatusOk = hasTag == nebType;
			if (!nebulaStatusOk) continue;

			float weight = 1;
			if (spec.isBlackHole() || spec.isPulsar()) weight = 0.25f;
			picker.add(spec, weight);
		}

		return picker.pick();
	}

	public void initGen(StarSystemAPI system, SectorAPI sector, String nebulaType){
		this.system = system;
		this.sector = sector;
		this.nebulaType = nebulaType;
		//terrainPlugins.clear();
	}

	public void initGen(StarSystemAPI system, SectorAPI sector, String nebulaType, StarSystemType systemType){
		this.system = system;
		this.sector = sector;
		this.nebulaType = nebulaType;
		if (systemType == null) return;
		this.systemType = systemType;
		this.randomSystemType = false;
		//terrainPlugins.clear();
	}
}