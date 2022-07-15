package data.scripts.items;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;

public class ptes_mapItemPlugin extends BaseSpecialItemPlugin {

    int
            FP,
            LP;
    String FactionId;
    FactionAPI faction;
    StarSystemGenerator.StarSystemType systemType;

    public static HashMap<StarSystemGenerator.StarSystemType, String> systemTypeIcons = new HashMap<>();
    public static HashMap<StarSystemGenerator.StarSystemType, String> systemTypeNames = new HashMap<>();

    static {
        systemTypeIcons.put(StarSystemGenerator.StarSystemType.BINARY_CLOSE, "graphics/icons/ptpos_binaryClose.png");
        systemTypeIcons.put(StarSystemGenerator.StarSystemType.BINARY_FAR, "graphics/icons/ptpos_binaryFar.png");
        systemTypeIcons.put(StarSystemGenerator.StarSystemType.TRINARY_1CLOSE_1FAR, "graphics/icons/ptpos_trinity1Far.png");
        systemTypeIcons.put(StarSystemGenerator.StarSystemType.TRINARY_2FAR, "graphics/icons/ptpos_trinity2Far.png");
        systemTypeIcons.put(StarSystemGenerator.StarSystemType.TRINARY_2CLOSE, "graphics/icons/ptpos_trinityClose.png");
        systemTypeIcons.put(StarSystemGenerator.StarSystemType.NEBULA, "graphics/icons/ptpos_nebula.png");
        systemTypeIcons.put(StarSystemGenerator.StarSystemType.SINGLE, "graphics/icons/ptpos_star.png");

        systemTypeNames.put(StarSystemGenerator.StarSystemType.BINARY_CLOSE, "Binary system");
        systemTypeNames.put(StarSystemGenerator.StarSystemType.BINARY_FAR, "Binary system");
        systemTypeNames.put(StarSystemGenerator.StarSystemType.TRINARY_1CLOSE_1FAR, "Trinity system");
        systemTypeNames.put(StarSystemGenerator.StarSystemType.TRINARY_2FAR, "Trinity system");
        systemTypeNames.put(StarSystemGenerator.StarSystemType.TRINARY_2CLOSE, "Trinity system");
        systemTypeNames.put(StarSystemGenerator.StarSystemType.NEBULA, "Nebula");
        systemTypeNames.put(StarSystemGenerator.StarSystemType.SINGLE, "Single star");
    }

    @Override
    public void init(CargoStackAPI stack) {
        super.init(stack);
        ptes_mapItemInfo info = (ptes_mapItemInfo) stack.getSpecialDataIfSpecial();
        this.FP = Math.round(info.FP);
        this.LP =  Math.round(info.LP);
        this.FactionId = info.FactionId;
        this.faction = Global.getSector().getFaction(FactionId);
        this.systemType = info.systemType;
    }

    @Override
    public String getName() {
        return spec.getName() + ": " + faction.getDisplayName();
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        float pad = 3f;
        float spad = 5f;
        float opad = 10f;

        tooltip.addTitle(getName());

        String factionName =  faction.getDisplayNameWithArticle();

        TooltipMakerAPI image = tooltip.beginImageWithText(faction.getCrest(), 48);
        image.addPara("This location contains fleets which mimics " + factionName + ".", pad, faction.getColor(), factionName);

        image.addPara("Power of fleets: " + FP, pad , Misc.getHighlightColor(), FP + "");
        image.addPara("Loot quantity: " + LP, pad , Misc.getHighlightColor(), LP + "");

        tooltip.addImageWithText(opad);


        if (systemType != null){
            image = tooltip.beginImageWithText(systemTypeIcons.get(systemType), 48);
            image.addPara("System type:", pad);
            image.addPara(systemTypeNames.get(systemType), pad);

            tooltip.addImageWithText(opad);
        }
        //tooltip.addPara(systemTypeNames.get(systemType), pad);
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemRendererAPI renderer) {

            SpriteAPI sprite = Global.getSettings().getSprite(faction.getLogo());
            if (sprite.getTextureId() == 0) return; // no texture for a "holo", so no custom rendering


            float cx = x + w/2f;
            float cy = y + h/2f;

            w = 40;
            h = 40;

            float p = 1;
            float blX = cx - 12f - p;
            float blY = cy - 22f - p;
            float tlX = cx - 26f - p;
            float tlY = cy + 19f + p;
            float trX = cx + 20f + p;
            float trY = cy + 24f + p;
            float brX = cx + 34f + p;
            float brY = cy - 9f - p;

            float mult = 1f;
            sprite.setAlphaMult(alphaMult * mult);
            sprite.setNormalBlend();
            sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);

            if (glowMult > 0) {
                sprite.setAlphaMult(alphaMult * glowMult * 0.5f * mult);
                sprite.setAdditiveBlend();
                sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);
            }

            renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);
            //renderer.renderSchematic(sprite, cx, cy, alphaMult * 0.67f);
            //renderer.renderSchematicWithCorners(sprite, null, blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult * 0.67f);
    }

}
