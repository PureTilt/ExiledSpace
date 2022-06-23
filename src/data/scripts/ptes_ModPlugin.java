package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.plugins.ptes_faction;
import data.scripts.plugins.ptes_salvageEntity;
import data.scripts.plugins.ptes_mapDrop;
import data.world.ptes_gen;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class ptes_ModPlugin extends BaseModPlugin {

    private static final org.apache.log4j.Logger log = Global.getLogger(ptes_ModPlugin.class);
    private static final boolean DevMode = Global.getSettings().isDevMode();

    static public List<String> backGrounds = new ArrayList<>();
    static public WeightedRandomPicker<ptes_faction> weightedFactions = new WeightedRandomPicker<>();
    static public HashMap<String, ptes_faction> FactionMap = new HashMap<>();
    static public List<ptes_salvageEntity> salvageList = new ArrayList<>();

    static void logger (String text){
        if (DevMode) log.info(text);
    }

    @Override
    public void onApplicationLoad(){
        loadData();
    }

    public void onGameLoad(boolean newGame) {
    }

    @Override
    public void onDevModeF8Reload() {
        loadData();
    }

    public void loadData(){
        backGrounds.clear();
        ClassLoader classLoader = Global.getSettings().getScriptClassLoader();

        try {
            JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("name", "data/config/ExiledSpace/backgrounds.csv", "pt_exiledSpace");

            for (int i = 0; i < spreadsheet.length(); i++) {
                JSONObject row = spreadsheet.getJSONObject(i);
                String name = row.getString("name");

                backGrounds.add(name);
            }
        } catch (Exception e) {
            log.error(e);
        }
        weightedFactions.clear();
        FactionMap.clear();
        try {
            JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/config/ExiledSpace/factions.csv", "pt_exiledSpace");

            for (int i = 0; i < spreadsheet.length(); i++) {
                JSONObject row = spreadsheet.getJSONObject(i);
                String id = row.getString("id");
                float weight = (float) row.getDouble("weight");
                float FPMulti = (float) row.getDouble("FPMulti");
                String genClass = row.getString("effectPlugin");
                float lootMulti = (float) row.getDouble("lootMulti");
                ptes_faction New = new ptes_faction(id, weight, FPMulti, classLoader.loadClass(genClass), lootMulti);
                weightedFactions.add(New, weight);
                FactionMap.put(New.faction, New);
            }
        } catch (Exception e) {
            log.error(e);
        }
        salvageList.clear();
        try {
            JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/config/ExiledSpace/PointsOfInterest.csv", "pt_exiledSpace");

            for (int i = 0; i < spreadsheet.length(); i++) {
                JSONObject row = spreadsheet.getJSONObject(i);
                String id = row.getString("id");
                float cost = (float) row.getDouble("cost");
                float weight = (float) row.getDouble("weight");
                String factions = row.getString("faction restriction");

                salvageList.add(new ptes_salvageEntity(id, weight, cost, factions));
            }
        } catch (Exception e) {
            log.error(e);
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
        Global.getSector().addListener(new ptes_mapDrop());
        for (FactionAPI faction : Global.getSector().getAllFactions()){
            faction.setRelationship("uknown", -100);
        }
        Global.getSector().getFaction("uknown").setRelationship("uknown", 100);
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