package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ui.ptes_giveMapUI;
import com.fs.starfarer.api.impl.campaign.ui.ptes_mapSelectUI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.intel.ptes_mapObjectiveIntel;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.plugins.ptes_mapEffectEntry;
import data.scripts.plugins.ptes_mapObjectiveEntry;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static data.scripts.items.ptes_mapItemPlugin.systemTypeIcons;
import static data.scripts.items.ptes_mapItemPlugin.systemTypeNames;
import static data.scripts.ptes_ModPlugin.mapEffectsMap;
import static data.scripts.ptes_ModPlugin.mapObjectivesMap;

public class ptes_riftGateDialog extends BaseCommandPlugin {

    public static Logger log = Global.getLogger(ptes_riftGateDialog.class);

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected CargoAPI playerCargo;
    protected MemoryAPI memory;
    protected MarketAPI market;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected FactionAPI faction;
    public StarSystemAPI system = null;

    protected void init(SectorEntityToken entity) {
        memory = entity.getMemoryWithoutUpdate();
        this.entity = entity;
        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        faction = entity.getFaction();

        market = entity.getMarket();
    }

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        //super.execute(ruleId, dialog, params, memoryMap);

        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        entity = dialog.getInteractionTarget();
        init(entity);

        memory = getEntityMemory(memoryMap);

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        switch (command) {
            case "main":
                addOptions();
                break;
            case "generateSystem":
                GenerateSystem();
                break;
            case "getInToSystem":
                GetInToSystem();
                break;
            case "giveRandomMap":
                GiveMap();
                break;
            case "moveGate":
                moveGate();
                break;
        }
        return true;
    }

    protected void addOptions() {
        int supplies = getSuppliesCost();
        int fuel = getFuelCost();
        String text = "Jump will consume:\n" + supplies + " Supplies\n" + fuel + " Fuel";
        String text2 = "";
        String text3 = "";
        String text4 = "";
        Color highlightColorSup = Misc.getHighlightColor();
        Color highlightColorFuel = Misc.getHighlightColor();
        if (Global.getSettings().isDevMode()) {
            entity.getMemoryWithoutUpdate().expire("$wasMoved", 0);
        } else {
            if (supplies > playerCargo.getSupplies()) {
                text += "\nNot enough supplies.";
                text2 = "Not enough supplies.";
                options.setEnabled("ptes_getInside", false);
                highlightColorSup = Misc.getNegativeHighlightColor();
            }
            if (fuel > playerCargo.getFuel()) {
                text += "\nNot enough fuel.";
                text3 = "Not enough fuel.";
                options.setEnabled("ptes_getInside", false);
                highlightColorFuel = Misc.getNegativeHighlightColor();
            }
        }
        system = Global.getSector().getStarSystem("PoSMap");
        boolean blockJump = system == null || system.getJumpPoints().size() <= 0;
        if (blockJump) {
            text4 = "No location chip was used yet.";
            text += "\n" + text4;
            options.setEnabled("ptes_getInside", false);
        }
        options.setTooltip("ptes_getInside", text);
        options.setTooltipHighlights("ptes_getInside", supplies + "", fuel + "", text2, text3, text4);
        options.setTooltipHighlightColors("ptes_getInside", highlightColorSup, highlightColorFuel, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
        if (entity.getMemoryWithoutUpdate().contains("$wasMoved")) {
            options.setEnabled("ptes_moveGate", false);
            options.setTooltip("ptes_moveGate", "Gate cant be moved for: " + Math.round(entity.getMemoryWithoutUpdate().getExpire("$wasMoved")) + " days");
            options.setTooltipHighlights("ptes_moveGate", Math.round(entity.getMemoryWithoutUpdate().getExpire("$wasMoved")) + "");
        }
        options.setTooltip("ptes_giveRandomMap", "Its for debug and testing will remove it for release.\nIf it taunts you that much remove\nptes_giveRandomMap:Get random Map item\nin rules.csv first row options column");
        if (!Global.getSector().getIntelManager().hasIntelOfClass(ptes_mapObjectiveIntel.class)) {
            Global.getSector().getIntelManager().addIntel(new ptes_mapObjectiveIntel(entity));
        }
    }


    protected void GenerateSystem() {
        dialog.showCustomDialog(1100, 700, new ptes_mapSelectUI(dialog));
    }

    protected void GetInToSystem() {
        int effects = ((ptes_mapItemInfo) entity.getMemory().get("$activeMap")).effects.size();
        final int height = 322 + 58 * effects + (effects > 0 ? 20 : 0);
        dialog.showCustomDialog(500, height, new CustomDialogDelegate() {
            @Override
            public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
                float pad = 3f;
                float spad = 5f;
                float opad = 10f;

                float width = 500;

                TooltipMakerAPI subPanel = panel.createUIElement(width, height, true);
                subPanel.setTitleOrbitronLarge();
                subPanel.setParaOrbitronLarge();
                if (dialog.getInteractionTarget().getMemory().contains("$mapVisited") && (boolean) dialog.getInteractionTarget().getMemory().get("$mapVisited")) {
                    subPanel.addTitle("Confirm jump (location was already visited)");
                } else {
                    subPanel.addTitle("Confirm jump");
                }

                ptes_mapItemInfo activeMap = (ptes_mapItemInfo) entity.getMemory().get("$activeMap");

                FactionAPI faction = Global.getSector().getFaction(activeMap.FactionId);
                String factionName = faction.getDisplayNameWithArticle();

                TooltipMakerAPI image = subPanel.beginImageWithText(faction.getCrest(), 48);
                image.addPara("This location contains fleets which mimics " + factionName + ".", pad, faction.getColor(), factionName);

                image.addPara("Power of fleets: " + activeMap.FP, pad, Misc.getHighlightColor(), activeMap.FP + "");
                image.addPara("Loot quantity: " + activeMap.LP, pad, Misc.getHighlightColor(), activeMap.LP + "");

                subPanel.addImageWithText(opad);

                if (activeMap.objectiveID != null) {
                    ptes_mapObjectiveEntry objectiveInfo = mapObjectivesMap.get(activeMap.objectiveID);
                    image = subPanel.beginImageWithText(objectiveInfo.iconPath, 48);
                    image.addPara("Objective: " + objectiveInfo.name, pad, Misc.getHighlightColor());
                    image.addPara(objectiveInfo.description, pad, Misc.getHighlightColor());
                    subPanel.addImageWithText(opad);
                }

                if (activeMap.systemType != null) {
                    image = subPanel.beginImageWithText(systemTypeIcons.get(activeMap.systemType), 48);
                    image.addPara("System type:", pad);
                    image.addPara(systemTypeNames.get(activeMap.systemType), pad);

                    subPanel.addImageWithText(opad);
                }


                if (!activeMap.effects.isEmpty()) {
                    subPanel.addSectionHeading("Additional effects", Alignment.MID, pad);
                    for (String effectID : activeMap.effects) {
                        ptes_mapEffectEntry effect = mapEffectsMap.get(effectID);
                        TooltipMakerAPI effectEntry = subPanel.beginImageWithText(effect.iconPath, 48);

                        effectEntry.addPara(effect.name, Misc.getHighlightColor(), pad);
                        effectEntry.addPara(effect.description, pad);

                        subPanel.addImageWithText(opad);
                    }
                }

                subPanel.addSectionHeading("Jump cost", Alignment.MID, opad);
                subPanel.addSpacer(spad);

                int supplies = getSuppliesCost();
                int fuel = getFuelCost();
                LabelAPI firstPAra = subPanel.addPara("", pad, Misc.getHighlightColor(), supplies + "", fuel + "");
                firstPAra.getPosition().setYAlignOffset(firstPAra.computeTextHeight(""));
                subPanel.addPara("Your fleet currently have:\n" + Math.round(playerCargo.getSupplies()) + " Supplies\n" + Math.round(playerCargo.getFuel()) + " Fuel", pad, Misc.getHighlightColor(), Math.round(playerCargo.getSupplies()) + "", Math.round(playerCargo.getFuel()) + "");
                firstPAra.setText("Jump will consume:\n" + supplies + " Supplies\n" + fuel + " Fuel");
                firstPAra.setHighlight(supplies + "", fuel + "");
                firstPAra.getPosition().setYAlignOffset(0);
                subPanel.getPrev().getPosition().setXAlignOffset(250);
                subPanel.getPrev().getPosition().setYAlignOffset(firstPAra.computeTextHeight(""));
                //firstPAra.getPosition().setYAlignOffset(0);


                panel.addUIElement(subPanel);
            }

            @Override
            public boolean hasCancelButton() {
                return true;
            }

            @Override
            public String getConfirmText() {
                return null;
            }

            @Override
            public String getCancelText() {
                return null;
            }

            @Override
            public void customDialogConfirm() {
                system = Global.getSector().getStarSystem("PoSMap");
                SectorEntityToken target = system.getJumpPoints().get(MathUtils.getRandomNumberInRange(0, system.getJumpPoints().size() - 1));
                //SectorEntityToken target = system.getAllEntities().get(0);
                JumpPointAPI.JumpDestination dest = new JumpPointAPI.JumpDestination(target, "System");
                Global.getSector().doHyperspaceTransition(playerFleet, entity, dest, 0);
                Global.getSector().reportFleetJumped(playerFleet, entity, dest);
                ((GateEntityPlugin) entity.getCustomPlugin()).showBeingUsed(2, 30);

                if (!Global.getSettings().isDevMode()) {
                    playerCargo.removeSupplies(getSuppliesCost());
                    playerCargo.removeFuel(getFuelCost());
                }

                dialog.getInteractionTarget().getMemory().set("$mapVisited", true);
                dialog.dismissAsCancel();
            }

            @Override
            public void customDialogCancel() {

            }

            @Override
            public CustomUIPanelPlugin getCustomPanelPlugin() {
                return null;
            }
        });

    }

    protected void GiveMap() {
        dialog.showCustomDialog(1120, 700, new ptes_giveMapUI());
        /*
        WeightedRandomPicker<StarSystemGenerator.StarSystemType> picker = new WeightedRandomPicker<>();
        picker.addAll(EnumSet.allOf(StarSystemGenerator.StarSystemType.class));

        float FP = MathUtils.getRandomNumberInRange(50, 400);
        ptes_mapItemInfo map = new ptes_mapItemInfo("pos_map", null, FP, FP * MathUtils.getRandomNumberInRange(0.5f, 2f), weightedFactions.pick().faction, picker.pick());
        Global.getSector().getPlayerFleet().getCargo().addSpecial(map, 1);

         */
    }

    protected void moveGate() {
        ArrayList<SectorEntityToken> destinations = new ArrayList<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (system.isEnteredByPlayer()) {
                destinations.add(system.getCenter());
            }
        }
        dialog.showCampaignEntityPicker("Select destination", "Destination:", "Initiate transit",
                Global.getSector().getPlayerFaction(), destinations,
                new CampaignEntityPickerListener() {

                    public void pickedEntity(SectorEntityToken entity) {
                        float cost = computeFuelCost(entity);
                        float fuelInStorage = market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().getFuel();
                        if (!Global.getSettings().isDevMode()) {
                            if (cost > fuelInStorage) {
                                cost -= fuelInStorage;
                                market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().removeFuel(fuelInStorage);
                                playerCargo.removeFuel(cost);
                            } else {
                                market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().removeFuel(cost);
                            }
                        }

                        SectorEntityToken gate = dialog.getInteractionTarget();
                        Vector2f rotation = new Vector2f(200f, 200f);
                        rotation = Misc.rotateAroundOrigin(rotation, MathUtils.getRandomNumberInRange(0, 360));
                        gate.setLocation(entity.getStarSystem().getLocation().x + rotation.x, entity.getStarSystem().getLocation().y + rotation.y);
                        gate.getMemoryWithoutUpdate().set("$wasMoved", true, 120);
                        system = Global.getSector().getStarSystem("PoSMap");
                        if (system != null) {
                            system.getLocation().set(gate.getLocation());
                            system.getHyperspaceAnchor().setLocation(system.getLocation().x, system.getLocation().y);
                        }
                        dialog.dismissAsCancel();
                    }

                    public void cancelledEntityPicking() {

                    }

                    public String getMenuItemNameOverrideFor(SectorEntityToken entity) {
                        return null;
                    }

                    public String getSelectedTextOverrideFor(SectorEntityToken entity) {
                        return entity.getName() + " - " + entity.getContainingLocation().getNameWithTypeShort();
                    }

                    public void createInfoText(TooltipMakerAPI info, SectorEntityToken entity) {

                        int cost = computeFuelCost(entity);
                        int available = (int) (market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().getFuel() + Global.getSector().getPlayerFleet().getCargo().getFuel());

                        Color reqColor = Misc.getHighlightColor();
                        Color availableColor = Misc.getHighlightColor();
                        if (cost > available) {
                            reqColor = Misc.getNegativeHighlightColor();
                        }

                        info.setParaSmallInsignia();
//					LabelAPI label = info.addPara("Transit requires %s fuel. "
//							+ "You have %s units of fuel available.", 0f,
//							Misc.getTextColor(),
//							//Misc.getGrayColor(),
//							availColor, Misc.getWithDGS(cost), Misc.getWithDGS(available));
//					label.setHighlightColors(reqColor, availColor);

                        info.beginGrid(200f, 2, Misc.getGrayColor());
                        info.setGridFontSmallInsignia();
                        info.addToGrid(0, 0, "    Fuel required:", Misc.getWithDGS(cost), reqColor);
                        info.addToGrid(1, 0, "    Fuel available:", Misc.getWithDGS(available), availableColor);
                        info.addGrid(0);
                    }

                    public boolean canConfirmSelection(SectorEntityToken entity) {
                        if (Global.getSettings().isDevMode()) return true;
                        int cost = computeFuelCost(entity);
                        int available = (int) Global.getSector().getPlayerFleet().getCargo().getFuel();
                        return cost <= available;
                    }

                    public float getFuelColorAlphaMult() {
                        return 0.5f;
                    }

                    public float getFuelRangeMult() { // just for showing it on the map when picking destination
                        return 0f;
                    }

                    @Override
                    public List<IntelInfoPlugin.ArrowData> getArrows() {
                        return null;
                    }

                    @Override
                    public List<MarkerData> getMarkers() {
                        return null;
                    }

                    @Override
                    public Set<StarSystemAPI> getStarSystemsToShow() {
                        return null;
                    }
                });
    }

    public static int computeFuelCost(SectorEntityToken targetGate) {
        float dist = Misc.getDistanceToPlayerLY(targetGate);
        float fuelPerLY = 100f;

        return (int) Math.ceil(dist * fuelPerLY);
    }

    protected int getSuppliesCost() {
        float burn = playerFleet.getFleetData().getMinBurnLevel() + playerFleet.getStats().getFleetwideMaxBurnMod().flatBonus;
        float time = 30f / (burn * 0.1f);
        float maintPerDay = 0;
        for (FleetMemberAPI mem : playerFleet.getMembersWithFightersCopy()) {
            if (mem.isMothballed()) continue;
            maintPerDay += mem.getStats().getSuppliesPerMonth().getModifiedValue() / 30f;
        }
        return Math.round(maintPerDay * time);
    }

    protected int getFuelCost() {
        return Math.round(playerFleet.getLogistics().getFuelCostPerLightYear() * 30f);
    }
}
