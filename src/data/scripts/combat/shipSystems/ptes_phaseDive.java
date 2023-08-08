package data.scripts.combat.shipSystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ptes_phaseDive extends BaseShipSystemScript {

    float maxSafeTime = 2f; //its max time ship will remain in out state if its under other ship
    float safeTime = 0f;

    final Map<ShipAPI.HullSize, Float> sizePushMulti = new HashMap<>();

    {
        sizePushMulti.put(ShipAPI.HullSize.DEFAULT, 1.0F);
        sizePushMulti.put(ShipAPI.HullSize.FIGHTER, 0.75F);
        sizePushMulti.put(ShipAPI.HullSize.FRIGATE, 0.5F);
        sizePushMulti.put(ShipAPI.HullSize.DESTROYER, 0.3F);
        sizePushMulti.put(ShipAPI.HullSize.CRUISER, 0.2F);
        sizePushMulti.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.1F);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (state == State.IN || state == State.ACTIVE) {
            //stats.getTimeMult().modifyMult("bt_phaseDive", 1 + 2 * effectLevel);

            ship.setExtraAlphaMult(1f - (1f - 0.25f) * effectLevel);
            ship.setApplyExtraAlphaToEngines(true);
            if (effectLevel >= 0.5f) {
                ship.setPhased(true);
            }
        } else if (state == State.OUT) {
            stats.getTimeMult().unmodify("bt_phaseDive");
            float amount = engine.getElapsedInLastFrame();
            float pushRange = ship.getCollisionRadius() + 50f;
            for (ShipAPI entity : CombatUtils.getShipsWithinRange(ship.getLocation(), pushRange)) {
                if (entity.isStationModule() && entity.isAlive()) {
                    entity = entity.getParentStation();
                    if (MathUtils.isWithinRange(entity.getLocation(), ship.getLocation(), pushRange))
                        continue;
                }
                if (entity.isStation()) entity = ship;
                if (entity == ship) continue;
                float angle = VectorUtils.getAngle(ship.getLocation(), entity.getLocation());
                Misc.getUnitVectorAtDegreeAngle(angle);
                Vector2f repulsion = Misc.getUnitVectorAtDegreeAngle(angle);
                float pushRatio = 1 - Math.min(1, MathUtils.getDistanceSquared(ship.getLocation(), entity.getLocation()) / (float) Math.pow(pushRange, 2));
                repulsion.scale(amount * 250.0f * pushRatio);
                Vector2f.add(entity.getLocation(), repulsion, entity.getLocation());

                repulsion.scale(this.sizePushMulti.get(entity.getHullSize()));
                Vector2f.add(entity.getVelocity(), repulsion, entity.getVelocity());
            }
            if (CombatUtils.getShipsWithinRange(ship.getLocation(), ship.getCollisionRadius() * 0.6f).size() <= 1 || safeTime >= maxSafeTime) {
                ship.setExtraAlphaMult(1f - (1f - 0.25f) * effectLevel);
                ship.setApplyExtraAlphaToEngines(false);
                if (effectLevel <= 0.5f) ship.setPhased(false);
            } else if (ship.isPhased()) {
                safeTime += amount;
                ship.getSystem().forceState(ShipSystemAPI.SystemState.OUT, 0);
            }
        }

        if (state == State.IN || state == State.ACTIVE || state == State.OUT){
            for (DamagingProjectileAPI tmp : Global.getCombatEngine().getProjectiles()) {
                if (tmp.getOwner() != ship.getOwner()) {
                    if (MathUtils.isWithinRange(ship, tmp, 150 * effectLevel)) {
                        if (Math.random() <= 0.75f) {
                            Vector2f particleVel = (Vector2f) normalise(tmp.getVelocity()).scale(250);
                            float size = Math.min(Math.round(Math.pow(tmp.getDamageAmount(), 0.6f)), 100);
                            float fxDuration = MathUtils.getRandomNumberInRange(0.4f, 0.6f);
                            int nebulaCount = MathUtils.getRandomNumberInRange(4, 8) + Math.round(size / 20);
                            for (int i = 0; i < nebulaCount; i++) {
                                Vector2f tempSpeed = new Vector2f(particleVel);
                                Color nebulaColor = Misc.interpolateColor(new Color(MathUtils.getRandomNumberInRange(0, 30), MathUtils.getRandomNumberInRange(170, 230), MathUtils.getRandomNumberInRange(130, 210), Math.round(MathUtils.getRandomNumberInRange(100, 130) * ( 4f / nebulaCount))),
                                        new Color(MathUtils.getRandomNumberInRange(113, 255), MathUtils.getRandomNumberInRange(19, 42), MathUtils.getRandomNumberInRange(133, 255), Math.round(MathUtils.getRandomNumberInRange(180, 210) * (4f / nebulaCount))),
                                        (float) Math.random());
                                engine.addNebulaParticle(tmp.getLocation(),
                                        (Vector2f) VectorUtils.rotate(tempSpeed, MathUtils.getRandomNumberInRange(-10, 10)).scale(MathUtils.getRandomNumberInRange(0.7f, 1f)),
                                        MathUtils.getRandomNumberInRange(0.8f, 1.2f) * size,
                                        MathUtils.getRandomNumberInRange(1.3f, 1.5f),
                                        0,
                                        0.3f,
                                        fxDuration + MathUtils.getRandomNumberInRange(-0.1f, 0.1f),
                                        nebulaColor,
                                        true);
                            }
/*
                            int basePartCount = Math.round(size / 10);
                            for (int i = 0; i < MathUtils.getRandomNumberInRange(4, 10) + basePartCount; i++) {
                                Vector2f tempSpeed = new Vector2f(particleVel);
                                Color nebulaColor = Misc.interpolateColor(new Color(MathUtils.getRandomNumberInRange(0, 30), MathUtils.getRandomNumberInRange(170, 230), MathUtils.getRandomNumberInRange(130, 210), Math.round(MathUtils.getRandomNumberInRange(100, 130) * ( 4f / nebulaCount))),
                                        new Color(MathUtils.getRandomNumberInRange(113, 255), MathUtils.getRandomNumberInRange(19, 42), MathUtils.getRandomNumberInRange(133, 255), Math.round(MathUtils.getRandomNumberInRange(180, 210) * (4f / nebulaCount))),
                                        (float) Math.random());
                                engine.addHitParticle(tmp.getLocation(),
                                        (Vector2f) VectorUtils.rotate(tempSpeed, MathUtils.getRandomNumberInRange(-5, 5)).scale(MathUtils.getRandomNumberInRange(0.85f, 1.7f)),
                                        MathUtils.getRandomNumberInRange(0.8f, 1.2f) * size * 0.25f,
                                        MathUtils.getRandomNumberInRange(0.4f, 0.8f),
                                        fxDuration + MathUtils.getRandomNumberInRange(-0.1f, 0.1f),
                                        nebulaColor);
                            }

 */
                            engine.addHitParticle(tmp.getLocation(),
                                    new Vector2f(),
                                    MathUtils.getRandomNumberInRange(0.8f, 1.2f) * size * 2f,
                                    MathUtils.getRandomNumberInRange(0.8f, 0.9f),
                                    MathUtils.getRandomNumberInRange(0.1f, 0.15f),
                                    new Color(MathUtils.getRandomNumberInRange(113, 255), MathUtils.getRandomNumberInRange(19, 42), MathUtils.getRandomNumberInRange(133, 255), MathUtils.getRandomNumberInRange(180, 210)));

                            engine.removeEntity(tmp);
                        }
                    }
                }
            }
        }
    }

    public final Vector normalise(Vector2f vector) {
        float len = vector.length();
        if (len != 0.0F) {
            float l = 1.0F / len;
            return vector.scale(l);
        } else {
            return vector;
        }
    }
}
