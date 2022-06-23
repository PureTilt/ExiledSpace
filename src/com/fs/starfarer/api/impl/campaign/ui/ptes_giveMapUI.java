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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static data.scripts.ptes_ModPlugin.FactionMap;
import static data.scripts.ptes_ModPlugin.weightedFactions;

public class ptes_giveMapUI implements CustomDialogDelegate {

    HashMap<ButtonAPI, ptes_faction> buttons = new HashMap<>();

    TextFieldAPI FP;
    TextFieldAPI LP;

    List<ButtonAPI> givemapButtonlist = new ArrayList<>();
    List<TextFieldAPI> textFields = new ArrayList<>();
    ButtonAPI giveMapButton;

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
        TooltipMakerAPI radioSelectPanel = panel.createUIElement(width,height, true);
        float totalWeight = weightedFactions.getTotal();
        boolean doonce = true;
        for (Map.Entry<String, ptes_faction> pair : FactionMap.entrySet()){

                FactionAPI faction = Global.getSector().getFaction(pair.getValue().faction);
                String factionName =  faction.getDisplayName();


                ButtonAPI button = radioSelectPanel.addAreaCheckbox("", faction.getId(), Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 0, 0, 0, true);

                //radioSelectPanel.addToGrid(10,10,factionName,"10");

                TooltipMakerAPI image = radioSelectPanel.beginImageWithText(faction.getCrest(), 64);

                image.addPara(factionName + "", pad, faction.getColor(), factionName);
                image.addPara("Weight: " + pair.getValue().weight, pad , Misc.getHighlightColor(), pair.getValue().weight + "");
                float chanceToDrop = Math.round((pair.getValue().weight / totalWeight) * 10000f) / 100f;
                image.addPara("Chance to drop: " + chanceToDrop + "%%", pad , Misc.getHighlightColor(), chanceToDrop + "%");

                image.addSpacer(10f);

                radioSelectPanel.addImageWithText(opad);

                PositionAPI imageWithTextPosition = radioSelectPanel.getPrev().getPosition();

                float imageWithTextHeight = imageWithTextPosition.getHeight();
                float imageWithTextWidth = imageWithTextPosition.getWidth();
                float imageWithTextXOffset = 7f;
                radioSelectPanel.addSpacer(spacerHeight);
                imageWithTextPosition.setXAlignOffset(imageWithTextXOffset);
                imageWithTextPosition.setSize(imageWithTextWidth, imageWithTextHeight);
                button.getPosition().setSize(backgroundBoxWidth, imageWithTextHeight + spacerHeight * 2f);
                imageWithTextPosition.setYAlignOffset(imageWithTextHeight + spacerHeight);
                radioSelectPanel.addSpacer(0f).getPosition().setXAlignOffset(-imageWithTextXOffset);

                buttons.put(button, pair.getValue());
                if (doonce){
                    button.setChecked(true);
                    doonce = false;
                }
        }

        panel.addUIElement(radioSelectPanel).inTL(0f, 0f);

        TooltipMakerAPI numberPanel = panel.createUIElement(100, 50, false);
        numberPanel.addTitle("Fleet Points");
        TextFieldAPI text = numberPanel.addTextField(100,pad);
        FP = text;
        textFields.add(FP);
        text.getTextLabelAPI().setAlignment(Alignment.BR);
        text.setText("100");
        panel.addUIElement(numberPanel).inTL(380, 0f);

        numberPanel = panel.createUIElement(100, 50, false);
        numberPanel.addTitle("Loot Points");
        text = numberPanel.addTextField(100,pad);
        LP = text;
        textFields.add(LP);
        text.getTextLabelAPI().setAlignment(Alignment.BR);
        text.setText("100");
        panel.addUIElement(numberPanel).inTL(380, 50f);

        numberPanel = panel.createUIElement(100, 25, false);
        panel.addUIElement(numberPanel).inTL(380, 100);
        giveMapButton = numberPanel.addAreaCheckbox("Give Map", null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 100, 25, 0, true);
        givemapButtonlist.add(giveMapButton);
        if (giveMapButton == null) Global.getLogger(ptes_giveMapUIPlugin.class).info("button is null");

    }

    @Override
    public boolean hasCancelButton() {
        return false;
    }

    @Override
    public String getConfirmText() {
        return "Close";
    }

    @Override
    public String getCancelText() {
        return null;
    }

    @Override
    public void customDialogConfirm() {
    }

    @Override
    public void customDialogCancel() {

    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return new ptes_giveMapUIPlugin(buttons, givemapButtonlist, textFields);
    }
}
