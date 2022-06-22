package com.fs.starfarer.api.impl.campaign.ui;

import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ptes_mapSelectUIPlugin implements CustomUIPanelPlugin {


    HashMap<ButtonAPI, CargoStackAPI> buttons;
    ButtonAPI lastActive = null;

    public ptes_mapSelectUIPlugin(HashMap<ButtonAPI, CargoStackAPI> buttons){
        this.buttons = buttons;
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
        for (Map.Entry<ButtonAPI, CargoStackAPI> pair : buttons.entrySet()){
            ButtonAPI button = pair.getKey();
            if (button.isChecked()){
                if (!button.equals(lastActive)){
                    if (lastActive != null) lastActive.setChecked(false);
                    lastActive = button;
                    break;
                }
            }
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }
}
