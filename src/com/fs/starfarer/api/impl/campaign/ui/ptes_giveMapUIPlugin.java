package com.fs.starfarer.api.impl.campaign.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TextFieldAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.items.ptes_mapItemInfo;
import data.scripts.plugins.ptes_faction;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static data.scripts.ptes_ModPlugin.weightedFactions;

public class ptes_giveMapUIPlugin implements CustomUIPanelPlugin {

    HashMap<ButtonAPI, ptes_faction> buttons;
    ButtonAPI lastActive = null;
    List<ButtonAPI> giveMapButton;
    List<TextFieldAPI> textFields;

    public ptes_giveMapUIPlugin(HashMap<ButtonAPI, ptes_faction> buttons, List<ButtonAPI> giveMapButton, List<TextFieldAPI> textFields){
        this.buttons = buttons;
        if (giveMapButton == null) Global.getLogger(ptes_giveMapUIPlugin.class).info("plugin button is null");
        this.giveMapButton = giveMapButton;
        this.textFields = textFields;
    }

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
        if (!atLeastOne) lastActive.setChecked(true);
        if (giveMapButton.size() != 0 && giveMapButton.get(0).isChecked()){
            try {
                giveMapButton.get(0).setChecked(false);
                int FP = Integer.parseInt(textFields.get(0).getText());
                int LP = Integer.parseInt(textFields.get(1).getText());

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
                if (faction != null) {
                    ptes_mapItemInfo map = new ptes_mapItemInfo("pos_map", null, FP, LP, faction, picker.pick());
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
}
