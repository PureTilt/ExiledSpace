package com.fs.starfarer.api.impl.campaign.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.items.ptes_mapItemPlugin;
import data.scripts.plugins.ptes_mapEffectEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static data.scripts.items.ptes_mapItemPlugin.systemTypeIcons;
import static data.scripts.items.ptes_mapItemPlugin.systemTypeNames;
import static data.scripts.ptes_ModPlugin.FactionMap;
import static data.scripts.ptes_ModPlugin.mapEffectsMap;

public class ptes_mapSelectUI implements CustomDialogDelegate {


    protected InteractionDialogAPI dialog;
    public TooltipMakerAPI
            fleetPrev,
            infoPanel;
    public CustomPanelAPI mainPanel;


    float pad = 3f;
    float spad = 5f;
    float opad = 10f;

    public ptes_mapSelectUI(InteractionDialogAPI dialog) {
        this.dialog = dialog;
    }


    HashMap<ButtonAPI, CargoStackAPI> buttons = new HashMap<>();
    HashMap<ButtonAPI, mapInfo> madeFleets = new HashMap<>();
    //List<ButtonAPI> buttons = new ArrayList<>();

    @Override
    public void createCustomDialog(CustomPanelAPI panel) {
        mainPanel = panel;


        float width = 375;
        float height = 700;

        TooltipMakerAPI UI = panel.createUIElement(width, height, true);


        float spacerHeight = 5f;
        final float backgroundBoxWidth = width - 10f;
        boolean doOnce = true;

        for (CargoStackAPI stack : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()) {
            if (stack.getPlugin() instanceof ptes_mapItemPlugin) {
                ptes_mapItemInfo data = (ptes_mapItemInfo) stack.getSpecialDataIfSpecial();
                ButtonAPI button = UI.addAreaCheckbox("", null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 0, 0, 0, true);
                //UI.addTooltipToPrevious(new BackgroundTooltipCreator(data, backgroundBoxWidth), TooltipMakerAPI.TooltipLocation.BELOW);

                FactionAPI faction = Global.getSector().getFaction(data.FactionId);
                String factionName = faction.getDisplayNameWithArticle();


                //UI.addToGrid(10,10,factionName,"10");

                TooltipMakerAPI image = UI.beginImageWithText(faction.getCrest(), 64);

                image.addPara("This location contains fleets which mimics " + factionName + ".", pad, faction.getColor(), factionName);
                image.addPara("Power of fleets: " + data.FP, pad, Misc.getHighlightColor(), data.FP + "");
                image.addPara("Loot quantity: " + data.LP, pad, Misc.getHighlightColor(), data.LP + "");

                image.addSpacer(10f);

                UI.addImageWithText(opad);

                PositionAPI imageWithTextPosition = UI.getPrev().getPosition();

                float imageWithTextHeight = imageWithTextPosition.getHeight();
                float imageWithTextWidth = imageWithTextPosition.getWidth();
                float imageWithTextXOffset = 7f;
                UI.addSpacer(spacerHeight);
                imageWithTextPosition.setXAlignOffset(imageWithTextXOffset);
                imageWithTextPosition.setSize(imageWithTextWidth, imageWithTextHeight);
                button.getPosition().setSize(backgroundBoxWidth, imageWithTextHeight + spacerHeight * 2f);
                imageWithTextPosition.setYAlignOffset(imageWithTextHeight + spacerHeight);
                UI.addSpacer(0f).getPosition().setXAlignOffset(-imageWithTextXOffset);

                buttons.put(button, stack);
                if (doOnce) {
                    button.setChecked(true);
                    doOnce = false;
                }
            }
        }
        UI.addSpacer(0f);
        panel.addUIElement(UI).inTL(0f, 0f);

        //map info table
        infoPanel = panel.createUIElement(700, 465, true);
        infoPanel.addSectionHeading("Chip information", Alignment.MID, pad);


        panel.addUIElement(infoPanel).inTL(388, 0f);


        //fleet previe table
        fleetPrev = panel.createUIElement(672, 235, true);

        fleetPrev.addSectionHeading("Enemy preview", Alignment.MID, pad);
        //fleetPrev.addShipList(7,5,57,Misc.getBasePlayerColor(),Global.getSector().getPlayerFleet().getMembersWithFightersCopy(), pad);

        panel.addUIElement(fleetPrev).inBL(414, 0f);
    }

    @Override
    public boolean hasCancelButton() {
        return true;
    }

    @Override
    public String getConfirmText() {
        return "Open map";
    }

    @Override
    public String getCancelText() {
        return "Close";
    }

    @Override
    public void customDialogConfirm() {
        Map.Entry<ButtonAPI, CargoStackAPI> active = null;
        for (Map.Entry<ButtonAPI, CargoStackAPI> pair : buttons.entrySet()) {
            ButtonAPI button = pair.getKey();
            if (button.isChecked()) {
                active = pair;
                break;
            }
        }
        if (active == null) return;


        FactionMap.get(((ptes_mapItemInfo) active.getValue().getData()).FactionId).genClass.generate(Global.getSector(), dialog.getInteractionTarget(), Global.getSector().getStarSystem("PoSMap"), dialog, (ptes_mapItemInfo) active.getValue().getData());
        dialog.getInteractionTarget().getMemory().set("$activeMap", active.getValue().getData());
        dialog.getInteractionTarget().getMemory().set("$mapVisited", false);
        Global.getSector().getPlayerFleet().getCargo().removeStack(active.getValue());
    }

    @Override
    public void customDialogCancel() {

    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return new CustomUIPanelPlugin() {

            ButtonAPI lastActive = null;


            @Override
            public void positionChanged(PositionAPI position) {

            }

            @Override
            public void renderBelow(float alphaMult) {

            }

            @Override
            public void render(float alphaMult) {

            }

            @Override
            public void advance(float amount) {
                boolean atLeastOne = false;
                for (Map.Entry<ButtonAPI, CargoStackAPI> pair : buttons.entrySet()) {
                    ButtonAPI button = pair.getKey();
                    if (button.isChecked()) {
                        if (!button.equals(lastActive)) {
                            ptes_mapItemInfo map = ((ptes_mapItemInfo) pair.getValue().getData());

                            if (lastActive != null) {
                                lastActive.setChecked(false);
                                //move out prev UI stuff
                                madeFleets.get(lastActive).fleetPreview.getPosition().setYAlignOffset(10000);
                                for (UIComponentAPI element : madeFleets.get(lastActive).mapData) {
                                    element.getPosition().inTL(10000, 10000);
                                }
                            }

                            //update fleet preview
                            UIComponentAPI fleetList;
                            if (madeFleets.containsKey(button)){
                                //move in current stuff
                                madeFleets.get(button).fleetPreview.getPosition().inTL(5, 30);
                                int elementCount = 0;
                                for (UIComponentAPI element : madeFleets.get(button).mapData) {
                                    element.getPosition().inTL(10, 25 + 69 * elementCount);
                                    elementCount++;
                                }
                            } else {
                                //create new elements
                                mapInfo mapUI = new mapInfo();
                                //fleet prev
                                Global.getLogger(ptes_mapSelectUI.class).info("made new fleet");
                                CampaignFleetAPI fleet = FleetFactoryV3.createFleet(FactionMap.get(map.FactionId).genClass.generateFleetParams(map.FP, FactionMap.get(map.FactionId)));
                                fleetPrev.addShipList(14, 4, 48, Misc.getBasePlayerColor(), fleet.getMembersWithFightersCopy(), 5);
                                fleetList = fleetPrev.getPrev();
                                mapUI.fleetPreview = fleetList;
                                fleetList.getPosition().inTL(5, 30);
                                float DP = 0;
                                for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
                                    if (!member.isFighterWing()) {
                                        DP += member.getDeploymentPointsCost();
                                    }
                                }
                                fleet.despawn();

                                //info panel
                                int elementCount = 0;
                                FactionAPI faction = Global.getSector().getFaction(map.FactionId);
                                String factionName = faction.getDisplayNameWithArticle();

                                TooltipMakerAPI image = infoPanel.beginImageWithText(faction.getCrest(), 64);
                                image.addPara("This location contains fleets which mimics " + factionName + ".", pad, faction.getColor(), factionName);
                                image.addPara("Power of fleets: " + map.FP, pad, Misc.getHighlightColor(), map.FP + "");
                                image.addPara("Loot quantity: " + map.LP, pad, Misc.getHighlightColor(), map.LP + "");
                                infoPanel.addImageWithText(pad);
                                mapUI.mapData.add(infoPanel.getPrev());
                                infoPanel.getPrev().getPosition().inTL(10, 25);
                                elementCount++;

                                image = infoPanel.beginImageWithText(systemTypeIcons.get(map.systemType), 64);
                                image.addPara("System type:", pad);
                                image.addPara(systemTypeNames.get(map.systemType), pad);
                                infoPanel.addImageWithText(pad);
                                mapUI.mapData.add(infoPanel.getPrev());
                                infoPanel.getPrev().getPosition().inTL(10, 25 + 69 * elementCount);
                                elementCount++;

                                for (String effectID : map.effects){
                                    ptes_mapEffectEntry effect = mapEffectsMap.get(effectID);
                                    TooltipMakerAPI effectEntry = infoPanel.beginImageWithText(effect.iconPath, 48);
                                    effectEntry.addPara(effect.name, Misc.getHighlightColor(), pad);

                                    effectEntry.addPara(effect.description, pad);
                                    infoPanel.addImageWithText(pad);
                                    mapUI.mapData.add(infoPanel.getPrev());
                                    infoPanel.getPrev().getPosition().inTL(10, 25 + 69 * elementCount);

                                    elementCount++;
                                }
                                madeFleets.put(button, mapUI);
                            }
                            lastActive = button;
                            atLeastOne = true;
                            break;
                        }
                    }
                }
                if (!atLeastOne && lastActive != null) lastActive.setChecked(true);
            }

            @Override
            public void processInput(List<InputEventAPI> events) {

            }
        };
    }

    public static class BackgroundTooltipCreator implements TooltipMakerAPI.TooltipCreator {

        private final ptes_mapItemInfo data;
        private final float width;

        public BackgroundTooltipCreator(ptes_mapItemInfo data, float width) {
            this.data = data;
            this.width = width - 20f;
        }

        @Override
        public boolean isTooltipExpandable(Object tooltipParam) {
            return false;
        }

        @Override
        public float getTooltipWidth(Object tooltipParam) {
            return width;
        }

        @Override
        public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
            float pad = 3f;
            float spad = 5f;
            float opad = 10f;
            StarSystemGenerator.StarSystemType systemType = data.systemType;
            if (systemType != null) {
                TooltipMakerAPI image = tooltip.beginImageWithText(systemTypeIcons.get(systemType), 48);
                image.addPara("System type:", pad);
                image.addPara(systemTypeNames.get(systemType), pad);
                tooltip.addImageWithText(opad);
            }
        }
    }

    public static class mapInfo {
        public UIComponentAPI fleetPreview;
        public List<UIComponentAPI> mapData = new ArrayList<>();
    }
}
