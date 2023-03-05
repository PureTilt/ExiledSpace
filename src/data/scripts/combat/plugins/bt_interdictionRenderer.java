package data.scripts.combat.plugins;

import com.fs.graphics.LayeredRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.combat.shipSystems.bt_phaseInterdictionField;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class bt_interdictionRenderer extends BaseCombatLayeredRenderingPlugin {

    static SpriteAPI sprite;
    static boolean doOnce = true;

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (doOnce){
            try {
                Global.getSettings().loadTexture("graphics/FX/bt_phase_interdiction.png");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sprite = Global.getSettings().getSprite("graphics/FX/bt_phase_interdiction.png");
            sprite.setColor(new Color(97, 0, 183,255));
            float range = bt_phaseInterdictionField.range * 2f * 1.1f;
            sprite.setSize(range, range);
        }
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();

        if (layer == CombatEngineLayers.BELOW_SHIPS_LAYER){
            List<ShipAPI> shipsWithSystem = new ArrayList<>();
            if (customCombatData.get("bt_phaseInterdictionFieldRender") instanceof List)
                shipsWithSystem = (List<ShipAPI>) customCombatData.get("bt_phaseInterdictionFieldRender");

            for (ShipAPI ship : shipsWithSystem){
                if (ship.getSystem().isActive()){
                    sprite.setAlphaMult(ship.getSystem().getEffectLevel());
                    sprite.renderAtCenter(ship.getLocation().x,ship.getLocation().y);

                }
            }
        }
    }

    public float getRenderRadius() {
        return 9.9999999E14F;
    }

    public EnumSet<CombatEngineLayers> getActiveLayers() {
        EnumSet<CombatEngineLayers> set = EnumSet.noneOf(CombatEngineLayers.class);
        set.add(CombatEngineLayers.BELOW_SHIPS_LAYER);
        return set;
        //return EnumSet.allOf(CombatEngineLayers.class);
    }
}
