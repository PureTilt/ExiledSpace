package com.fs.starfarer.api.impl.campaign.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.items.ptes_mapItemPlugin;
import data.scripts.items.ptes_mapItemInfo;

import java.util.HashMap;
import java.util.Map;

import static data.scripts.ptes_ModPlugin.FactionMap;
import static data.scripts.items.ptes_mapItemPlugin.systemTypeIcons;
import static data.scripts.items.ptes_mapItemPlugin.systemTypeNames;

public class ptes_mapSelectUI implements CustomDialogDelegate {


    protected InteractionDialogAPI dialog;

    public ptes_mapSelectUI(InteractionDialogAPI dialog){
        this.dialog = dialog;
    }


    HashMap<ButtonAPI, CargoStackAPI> buttons = new HashMap<>();
    //List<ButtonAPI> buttons = new ArrayList<>();

    @Override
    public void createCustomDialog(CustomPanelAPI panel) {


        float pad = 3f;
        float spad = 5f;
        float opad = 10f;

        float width = 500;
        float height = 700;

        TooltipMakerAPI UI = panel.createUIElement(width,height, true);



        float spacerHeight = 5f;
        final float backgroundBoxWidth = width - 10f;
        boolean doOnce = true;

        for (CargoStackAPI stack : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()){
            if (stack.getPlugin() instanceof ptes_mapItemPlugin){
                ptes_mapItemInfo data = (ptes_mapItemInfo) stack.getSpecialDataIfSpecial();
                ButtonAPI button = UI.addAreaCheckbox("", null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 0, 0, 0, true);
                UI.addTooltipToPrevious(new BackgroundTooltipCreator(data, backgroundBoxWidth),TooltipMakerAPI.TooltipLocation.BELOW);

                FactionAPI faction = Global.getSector().getFaction(data.FactionId);
                String factionName =  faction.getDisplayNameWithArticle();


                //UI.addToGrid(10,10,factionName,"10");

                TooltipMakerAPI image = UI.beginImageWithText(faction.getCrest(), 64);

                image.addPara("This location contains fleets which mimics " + factionName + ".", pad, faction.getColor(), factionName);
                image.addPara("Power of fleets: " + data.FP, pad , Misc.getHighlightColor(), data.FP + "");
                image.addPara("Loot quantity: " + data.LP, pad , Misc.getHighlightColor(), data.LP + "");

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
            }

        }

        UI.addSpacer(0f);
        panel.addUIElement(UI).inTL(0f, 0f);
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
        for (Map.Entry<ButtonAPI, CargoStackAPI> pair : buttons.entrySet()){
            ButtonAPI button = pair.getKey();
            if (button.isChecked()){
                active = pair;
                break;
            }
        }
        if (active == null) return;


        FactionMap.get(((ptes_mapItemInfo) active.getValue().getData()).FactionId).genClass.generate(Global.getSector(), dialog.getInteractionTarget(), Global.getSector().getStarSystem("PoSMap"), dialog, (ptes_mapItemInfo) active.getValue().getData());
        Global.getSector().getPlayerFleet().getCargo().removeStack(active.getValue());
    }

    @Override
    public void customDialogCancel() {

    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return new ptes_mapSelectUIPlugin(buttons);
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
            if (systemType != null){
                TooltipMakerAPI image = tooltip.beginImageWithText(systemTypeIcons.get(systemType), 48);
                image.addPara("System type:", pad);
                image.addPara(systemTypeNames.get(systemType), pad);

                tooltip.addImageWithText(opad);
            }
        }
    }
}
