package com.fs.starfarer.api.impl.campaign.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.items.ptes_mapItemPlugin;
import data.scripts.plugins.ptes_baseEffectPlugin;
import data.scripts.plugins.ptes_faction;
import data.scripts.plugins.ptes_mapEffectEntry;
import data.scripts.plugins.ptes_mapObjectiveEntry;

import java.util.*;

import static data.scripts.ptes_ModPlugin.*;

public class ptes_giveMapUI implements CustomDialogDelegate {

    HashMap<ButtonAPI, ptes_faction> buttons = new HashMap<>();
    HashMap<ButtonAPI, String> effectButtons = new HashMap<>();
    HashMap<ButtonAPI, String> objectiveButtons = new HashMap<>();

    TextFieldAPI FP = null;
    TextFieldAPI LP = null;

    List<TextFieldAPI> textFields = new ArrayList<>();
    ButtonAPI giveMapButton = null;


    public ptes_giveMapUI(){

    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
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
        for (ptes_faction entry :weightedFactions.getItems()){

                FactionAPI faction = Global.getSector().getFaction(entry.faction);
                String factionName =  faction.getDisplayName();


                ButtonAPI button = radioSelectPanel.addAreaCheckbox("", faction.getId(), Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 0, 0, 0, true);

                //radioSelectPanel.addToGrid(10,10,factionName,"10");

                TooltipMakerAPI image = radioSelectPanel.beginImageWithText(faction.getCrest(), 64);

                image.addPara(factionName + "", pad, faction.getColor(), factionName);
                image.addPara("Weight: " + entry.weight, pad , Misc.getHighlightColor(), entry.weight + "");
                float chanceToDrop = Math.round((entry.weight / totalWeight) * 10000f) / 100f;
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

                buttons.put(button, entry);
                if (doonce){
                    button.setChecked(true);
                    doonce = false;
                }
        }
        panel.addUIElement(radioSelectPanel).inTL(0f, 0f);

        TooltipMakerAPI effectsPanel = panel.createUIElement(width,height, true);
        for (String entry : mapEffects){
            ptes_mapEffectEntry effect = mapEffectsMap.get(entry);

            ButtonAPI button = effectsPanel.addAreaCheckbox("", null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 0, 0, 0, true);

            //radioSelectPanel.addToGrid(10,10,factionName,"10");

            TooltipMakerAPI image = effectsPanel.beginImageWithText(effect.iconPath, 48);

            image.addPara(effect.name, Misc.getHighlightColor(), pad);
            image.addPara(effect.description, pad);

            image.addSpacer(10f);

            effectsPanel.addImageWithText(opad);

            PositionAPI imageWithTextPosition = effectsPanel.getPrev().getPosition();

            float imageWithTextHeight = imageWithTextPosition.getHeight();
            float imageWithTextWidth = imageWithTextPosition.getWidth();
            float imageWithTextXOffset = 7f;
            effectsPanel.addSpacer(spacerHeight);
            imageWithTextPosition.setXAlignOffset(imageWithTextXOffset);
            imageWithTextPosition.setSize(imageWithTextWidth, imageWithTextHeight);
            button.getPosition().setSize(backgroundBoxWidth, imageWithTextHeight + spacerHeight * 2f);
            imageWithTextPosition.setYAlignOffset(imageWithTextHeight + spacerHeight);
            effectsPanel.addSpacer(0f).getPosition().setXAlignOffset(-imageWithTextXOffset);

            effectButtons.put(button, effect.id);
        }
        panel.addUIElement(effectsPanel).inTL(310f, 0f);

        TooltipMakerAPI objectivesPanel = panel.createUIElement(width,height, true);
        for (String entry : mapObjectives){
            ptes_mapObjectiveEntry objective = mapObjectivesMap.get(entry);

            ButtonAPI button = objectivesPanel.addAreaCheckbox("", null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 0, 0, 0, true);

            //radioSelectPanel.addToGrid(10,10,factionName,"10");

            TooltipMakerAPI image = objectivesPanel.beginImageWithText(objective.iconPath, 48);

            image.addPara(objective.name, Misc.getHighlightColor(), pad);
            image.addPara(objective.description, pad);

            image.addSpacer(10f);

            objectivesPanel.addImageWithText(opad);

            PositionAPI imageWithTextPosition = objectivesPanel.getPrev().getPosition();

            float imageWithTextHeight = imageWithTextPosition.getHeight();
            float imageWithTextWidth = imageWithTextPosition.getWidth();
            float imageWithTextXOffset = 7f;
            objectivesPanel.addSpacer(spacerHeight);
            imageWithTextPosition.setXAlignOffset(imageWithTextXOffset);
            imageWithTextPosition.setSize(imageWithTextWidth, imageWithTextHeight);
            button.getPosition().setSize(backgroundBoxWidth, imageWithTextHeight + spacerHeight * 2f);
            imageWithTextPosition.setYAlignOffset(imageWithTextHeight + spacerHeight);
            objectivesPanel.addSpacer(0f).getPosition().setXAlignOffset(-imageWithTextXOffset);

            objectiveButtons.put(button, objective.id);
        }
        panel.addUIElement(objectivesPanel).inTL(620f, 0f);

        int Xpos = 930;
        TooltipMakerAPI numberPanel = panel.createUIElement(100, 50, false);
        numberPanel.addTitle("Fleet Points");
        TextFieldAPI text = numberPanel.addTextField(100,pad);
        FP = text;
        textFields.add(FP);
        text.getTextLabelAPI().setAlignment(Alignment.BR);
        text.setText("100");
        panel.addUIElement(numberPanel).inTL(Xpos, 0f);

        numberPanel = panel.createUIElement(100, 50, false);
        numberPanel.addTitle("Loot Points");
        text = numberPanel.addTextField(100,pad);
        LP = text;
        textFields.add(LP);
        text.getTextLabelAPI().setAlignment(Alignment.BR);
        text.setText("100");
        panel.addUIElement(numberPanel).inTL(Xpos, 50f);

        numberPanel = panel.createUIElement(100, 25, false);
        panel.addUIElement(numberPanel).inTL(Xpos, 100);
        giveMapButton = numberPanel.addAreaCheckbox("Give Map", null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 100, 25, 0, true);

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
        return new CustomUIPanelPlugin() {
        ButtonAPI lastActive = null;
        ButtonAPI lastActiveObjective = null;

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
            for (Map.Entry<ButtonAPI, ptes_faction> pair : buttons.entrySet()){
                ButtonAPI button = pair.getKey();
                if (button.isChecked()){
                    if (!button.equals(lastActive)){
                        if (lastActive != null) lastActive.setChecked(false);
                        lastActive = button;
                        atLeastOne = true;
                        break;
                    }
                }
            }
            for (Map.Entry<ButtonAPI, String> pair : objectiveButtons.entrySet()){
                ButtonAPI button = pair.getKey();
                if (button.isChecked()){
                    if (!button.equals(lastActiveObjective)){
                        if (lastActiveObjective != null) lastActiveObjective.setChecked(false);
                        lastActiveObjective = button;
                        break;
                    }
                }
            }
            if (!atLeastOne && lastActive != null) lastActive.setChecked(true);
            if (giveMapButton != null && giveMapButton.isChecked()){
                try {
                    giveMapButton.setChecked(false);

                    WeightedRandomPicker<StarSystemGenerator.StarSystemType> picker = new WeightedRandomPicker<>();
                    picker.addAll(EnumSet.allOf(StarSystemGenerator.StarSystemType.class));

                    String faction = null;
                    for (Map.Entry<ButtonAPI, ptes_faction> pair : buttons.entrySet()){
                        ButtonAPI button = pair.getKey();
                        if (button.isChecked()){
                            faction = pair.getValue().faction;
                            break;
                        }
                    }

                    String objective = null;
                    for (Map.Entry<ButtonAPI, String> pair : objectiveButtons.entrySet()){
                        ButtonAPI button = pair.getKey();
                        if (button.isChecked()){
                            objective = pair.getValue();
                            break;
                        }
                    }

                    List<String> effects = new ArrayList<>();
                    for (Map.Entry<ButtonAPI, String> entry : effectButtons.entrySet()){
                        if (entry.getKey().isChecked()){
                            effects.add(entry.getValue());
                        }
                    }

                    if (faction != null) {
                        ptes_mapItemInfo map = new ptes_mapItemInfo("pos_map", null, Integer.parseInt(FP.getText()), Integer.parseInt(LP.getText()), faction, picker.pick(),effects, objective);
                        Global.getSector().getPlayerFleet().getCargo().addSpecial(map, 1);
                    }

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void processInput(List<InputEventAPI> events) {

        }

            @Override
            public void buttonPressed(Object buttonId) {

            }
        };
    }
}
