package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.plugins.ptes_faction;
import data.scripts.plugins.ptes_mapDrop;
import data.scripts.plugins.ptes_mapEffectEntry;
import data.scripts.plugins.ptes_salvageEntity;
import data.world.ptes_gen;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static data.scripts.ids.ptes_factions.MAP_FACTION;

public class ptes_ModPlugin extends BaseModPlugin {

    private static final org.apache.log4j.Logger log = Global.getLogger(ptes_ModPlugin.class);
    private static final boolean DevMode = Global.getSettings().isDevMode();

    static public List<String> backGrounds = new ArrayList<>();
    static public WeightedRandomPicker<ptes_faction> weightedFactions = new WeightedRandomPicker<>();
    static public HashMap<String, ptes_faction> FactionMap = new HashMap<>();
    static public List<ptes_salvageEntity> salvageList = new ArrayList<>();
    static public HashMap<String, ptes_mapEffectEntry> mapEffectsMap = new HashMap<>();
    static public List<String> mapEffects = new ArrayList<>();

    static void logger(String text) {
        if (DevMode) log.info(text);
    }

    @Override
    public void onApplicationLoad() {
        loadData();
    }

    public void onGameLoad(boolean newGame) {
        Global.getSector().addTransientListener(new ptes_mapDrop());
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (faction.getId().equals(MAP_FACTION)) continue;
            faction.setRelationship(MAP_FACTION, -100);
        }
        /*
        if (!Global.getSector().getMemory().contains("pt_gate")){

            Global.getSector().getMemory().set("pt_gate", );
        }

         */
        factionCheck();
    }

    @Override
    public void onDevModeF8Reload() {
        loadData();
        factionCheck();
    }

    public void loadData() {
        List<ModSpecAPI> mods = Global.getSettings().getModManager().getEnabledModsCopy();
        for (ModSpecAPI mod : mods) {
            if (mod.getId().equals("pt_exiledSpace")) {
                mods.remove(mod);
                mods.add(0, mod);
                break;
            }
        }
        ClassLoader classLoader = Global.getSettings().getScriptClassLoader();

        logger("loading BGs");
        backGrounds.clear();
        for (ModSpecAPI mod : mods) {
            try {
                //JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("name", "data/config/ExiledSpace/backgrounds.csv", "pt_exiledSpace");
                JSONArray spreadsheet = Global.getSettings().loadCSV("data/config/ExiledSpace/backgrounds.csv", mod.getId());

                for (int i = 0; i < spreadsheet.length(); i++) {
                    JSONObject row = spreadsheet.getJSONObject(i);
                    String name = row.getString("name");

                    backGrounds.add(name);
                }
            } catch (RuntimeException ignored) {

            } catch (Exception e) {
                log.error(e);
                log.error("Cause: " + mod.getName());
            }
        }
        logger("BGs: " + backGrounds.size());

        logger("loading Factions");
        weightedFactions.clear();
        FactionMap.clear();
        for (ModSpecAPI mod : mods) {
            try {
                //JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/config/ExiledSpace/factions.csv", "pt_exiledSpace");
                JSONArray spreadsheet = Global.getSettings().loadCSV("data/config/ExiledSpace/factions.csv", mod.getId());

                for (int i = 0; i < spreadsheet.length(); i++) {
                    JSONObject row = spreadsheet.getJSONObject(i);
                    String id = row.getString("id");
                    String idOverride = row.getString("countsAs");
                    if (idOverride.equals("")) idOverride = null;
                    float weight = (float) row.optDouble("weight", 1);
                    float FPMulti = (float) row.optDouble("FPMulti", 1);
                    String genClass = row.getString("effectPlugin");
                    float lootMulti = (float) row.optDouble("lootMulti", 1);
                    float quality = (float) row.optDouble("quality", 1);
                    ptes_faction New = new ptes_faction(id, idOverride, weight, FPMulti, classLoader.loadClass(genClass), lootMulti, quality);
                    weightedFactions.add(New, weight);
                    FactionMap.put(New.faction, New);
                }
            } catch (RuntimeException ignored) {
            } catch (Exception e) {
                log.error(e);
                log.error("Cause: " + mod.getName());
            }
        }
        logger("Factions: " + FactionMap.size());
        logger("loading subfactions");
        int subFactionsLoaded = 0;
        for (ModSpecAPI mod : mods) {
            try {
                //JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("subFactionID", "data/config/ExiledSpace/subFactions.csv", "pt_exiledSpace");
                JSONArray spreadsheet = Global.getSettings().loadCSV("data/config/ExiledSpace/subFactions.csv", mod.getId());

                for (int i = 0; i < spreadsheet.length(); i++) {
                    JSONObject row = spreadsheet.getJSONObject(i);
                    String parentID = row.getString("parentFactionID");
                    String subId = row.getString("subFactionID");
                    float weight = (float) row.optDouble("weight", 1);
                    FactionMap.get(parentID).subFactions.put(subId, weight);
                    subFactionsLoaded++;
                }
            } catch (RuntimeException ignored) {

            } catch (Exception e) {
                log.error(e);
            }
        }
        logger("Subfcations: " + subFactionsLoaded);

        salvageList.clear();
        logger("Loading Salvage objects");
        for (ModSpecAPI mod : mods) {
            try {
                //JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/config/ExiledSpace/PointsOfInterest.csv", "pt_exiledSpace");
                JSONArray spreadsheet = Global.getSettings().loadCSV("data/config/ExiledSpace/PointsOfInterest.csv", mod.getId());

                for (int i = 0; i < spreadsheet.length(); i++) {
                    JSONObject row = spreadsheet.getJSONObject(i);
                    String id = row.getString("id");
                    float cost = (float) row.getDouble("cost");
                    float weight = (float) row.optDouble("weight", 1);
                    String factions = row.getString("faction restriction");

                    salvageList.add(new ptes_salvageEntity(id, weight, cost, factions));
                }
            } catch (RuntimeException ignored) {

            } catch (Exception e) {
                log.error(e);
            }
        }
        logger("Salvage objects: " + salvageList.size());

        logger("loading Map Effects");
        mapEffects.clear();
        for (ModSpecAPI mod : mods) {
            try {
                //JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/config/ExiledSpace/mapEffects.csv", "pt_exiledSpace");
                JSONArray spreadsheet = Global.getSettings().loadCSV("data/config/ExiledSpace/mapEffects.csv", mod.getId());

                for (int i = 0; i < spreadsheet.length(); i++) {
                    try {
                        JSONObject row = spreadsheet.getJSONObject(i);
                        String id = row.getString("id");
                        String name = row.getString("name");
                        if (!mod.getId().equals("pt_exiledSpace")) {
                            name = "[" + mod.getName() + "] " + name;
                        }
                        float cost = (float) row.getDouble("cost");
                        float weight = (float) row.optDouble("weight", 1);
                        float order = (float) row.getDouble("order");
                        String description = row.getString("description");
                        String iconPath = row.getString("icon");
                        Global.getSettings().loadTexture(iconPath);
                        String genClass = row.getString("effectPlugin");
                        //logger(name);
                        mapEffectsMap.put(id, new ptes_mapEffectEntry(id, name, cost, weight, order, description, iconPath, classLoader.loadClass(genClass)));
                        mapEffects.add(id);
                    } catch (Exception e) {
                        log.error(e);
                    }
                }
            } catch (RuntimeException ignored) {

            } catch (Exception e) {
                log.error(e);
            }
        }
        logger("effects: " + mapEffects.size());
    }

    public void factionCheck() {
        for (Map.Entry<String, ptes_faction> entry : new ArrayList<>(FactionMap.entrySet())) {
            FactionAPI faction = Global.getSector().getFaction(entry.getKey());
            if (faction == null) {
                logger("wrong faction ID: " + entry.getKey());
                FactionMap.remove(entry.getKey());
                weightedFactions.remove(entry.getValue());
            }
        }
    }

    @Override
    public void onNewGame() {
        /*
        //Nex compatibility setting, if there is no nex or corvus mode(Nex), just generate the system
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        new ptes_gen().generate(Global.getSector());
        Global.getSector().addListener(new ptes_mapDrop());

         */
    }

    @Override
    public void onEnabled(boolean wasEnabledBefore) {
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        new ptes_gen().generate(Global.getSector());
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (faction.getId().equals(MAP_FACTION)) continue;
            faction.setRelationship(MAP_FACTION, -100);
        }
        //Global.getSector().getFaction("unknown").setRelationship("uknown", 100);
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        /*

        MarketAPI market = Global.getSector().getEconomy().getMarket("vic_planet_cocytus_market");
        if (market != null) {
            PersonAPI admin = Global.getFactory().createPerson();
            admin.setFaction("vic");
            admin.setGender(FullName.Gender.FEMALE);
            admin.setPostId(Ranks.POST_FACTION_LEADER);
            admin.setRankId(Ranks.FACTION_LEADER);
            admin.getName().setFirst("Tatiana");
            admin.getName().setLast("Murakami");
            admin.setPortraitSprite("graphics/portraits/characters/vic_tatiana.jpg");

            admin.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 3);
            admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
            admin.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);

            market.setAdmin(admin);
            market.getCommDirectory().addPerson(admin, 0);
            market.addPerson(admin);
        }

         */
    }
}