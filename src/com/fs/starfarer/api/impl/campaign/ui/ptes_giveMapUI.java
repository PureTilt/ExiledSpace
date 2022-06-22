package com.fs.starfarer.api.impl.campaign.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.items.ptes_mapItemPlugin;
import data.scripts.plugins.ptes_faction;

import java.util.HashMap;
import java.util.Map;

import static data.scripts.ptes_ModPlugin.FactionMap;
import static data.scripts.ptes_ModPlugin.weightedFactions;

public class ptes_giveMapUI implements CustomDialogDelegate {

    HashMap<ButtonAPI, Map.Entry<String, ptes_faction>> buttons = new HashMap<ButtonAPI, java.util.Map.Entry<String, ptes_faction>>();

    TextFieldAPI text;
    public ptes_giveMapUI(){

    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel) {
        float pad = 3f;
        float spad = 5f;
        float opad = 10f;

        float width = 300;
        float height = 700;

        float spacerHeight = 5f;
        final float backgroundBoxWidth = width - 10f;
        TooltipMakerAPI UI = panel.createUIElement(width,height, true);
        float totalWeight = weightedFactions.getTotal();
        for (Map.Entry<String, ptes_faction> pair : FactionMap.entrySet()){

                ButtonAPI button = UI.addAreaCheckbox("", null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 0, 0, 0, true);

                FactionAPI faction = Global.getSector().getFaction(pair.getValue().faction);
                String factionName =  faction.getDisplayName();


                //UI.addToGrid(10,10,factionName,"10");

                TooltipMakerAPI image = UI.beginImageWithText(faction.getCrest(), 64);

                image.addPara(factionName + "", pad, faction.getColor(), factionName);
                image.addPara("Weight: " + pair.getValue().weight, pad , Misc.getHighlightColor(), pair.getValue().weight + "");
                float chanceToDrop = Math.round((pair.getValue().weight / totalWeight) * 10000f) / 100f;
                image.addPara("Chance to drop: " + chanceToDrop + "%%", pad , Misc.getHighlightColor(), chanceToDrop + "%");

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

                //buttons.put(button, stack);

        }

        panel.addUIElement(UI).inTL(0f, 0f);
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
        try {
            Global.getLogger(ptes_giveMapUI.class).info(Integer.valueOf(text.getText()));
        } catch (NumberFormatException e) {
            Global.getLogger(ptes_giveMapUI.class).info("not a number");
            e.printStackTrace();
        }

    }

    @Override
    public void customDialogCancel() {

    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return null;
    }
}
