package data.scripts.combat.BT.AI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class bt_phaseDive extends BaseShipSystemScript {

    float maxSafeTime = 2f; //its max time ship will remain in out state if its under other ship
    float safeTime = 0f;

    final DamagingExplosionSpec explosion = new DamagingExplosionSpec(0.1f,
                500,
                250,
                250,
                250 * 0.5f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                3,
                3,
                0.5f,
                10,
                new Color(33, 255, 122, 255),
                new Color(MathUtils.getRandomNumberInRange(215, 255), MathUtils.getRandomNumberInRange(130, 170), MathUtils.getRandomNumberInRange(15, 55), 255)
        );
    {
        explosion.setDamageType(DamageType.HIGH_EXPLOSIVE);
    }


    final Map<ShipCommand, Boolean> commands = new HashMap<>();
    {
        commands.put(ShipCommand.ACCELERATE, false);
        commands.put(ShipCommand.ACCELERATE_BACKWARDS, false);
        commands.put(ShipCommand.STRAFE_LEFT, false);
        commands.put(ShipCommand.STRAFE_RIGHT, false);
    }

    final Map<ShipAPI.HullSize, Float> sizePushMulti = new HashMap<>();
    {
        sizePushMulti.put(ShipAPI.HullSize.DEFAULT, 1.0F);
        sizePushMulti.put(ShipAPI.HullSize.FIGHTER, 0.75F);
        sizePushMulti.put(ShipAPI.HullSize.FRIGATE, 0.5F);
        sizePushMulti.put(ShipAPI.HullSize.DESTROYER, 0.3F);
        sizePushMulti.put(ShipAPI.HullSize.CRUISER, 0.2F);
        sizePushMulti.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.1F);
    }

    final Map<ShipAPI.HullSize, Float> strafeMulti = new HashMap<>();

    {
        strafeMulti.put(ShipAPI.HullSize.FIGHTER, 1f);
        strafeMulti.put(ShipAPI.HullSize.FRIGATE, 1f);
        strafeMulti.put(ShipAPI.HullSize.DESTROYER, 0.75f);
        strafeMulti.put(ShipAPI.HullSize.CRUISER, 0.5f);
        strafeMulti.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.25f);
    }

    boolean doOnce = true;
    boolean doExplosiveExit = true;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (doOnce){
            Vector2f newVector = new Vector2f();
            if (ship.getEngineController().isAccelerating()) {
                commands.put(ShipCommand.ACCELERATE, true);
                newVector.y += 1 * ship.getAcceleration();
            }
            if (ship.getEngineController().isAcceleratingBackwards() || ship.getEngineController().isDecelerating()) {
                commands.put(ShipCommand.ACCELERATE_BACKWARDS, true);
                newVector.y -= 1 * ship.getDeceleration();
            }
            if (ship.getEngineController().isStrafingLeft()) {
                commands.put(ShipCommand.STRAFE_LEFT, true);
                newVector.x -= 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
            }
            if (ship.getEngineController().isStrafingRight()) {
                commands.put(ShipCommand.STRAFE_RIGHT, true);
                newVector.x += 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
            }

            ship.getExactBounds().update(ship.getLocation(), ship.getFacing());
            List<BoundsAPI.SegmentAPI> Segments = ship.getExactBounds().getSegments();
            spawnDistortion(ship.getLocation());
            for (int i = 0; i < 10; i++) {
                engine.spawnEmpArcVisual(
                        Segments.get(MathUtils.getRandomNumberInRange(0,Segments.size() - 1)).getP1(),
                        ship,
                        MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(200f, 500f),Misc.getAngleInDegrees(newVector) - 180 + MathUtils.getRandomNumberInRange(-45f, 45f)),
                        null,
                        20,
                        new Color(172, 25, 255,255),
                        new Color(239, 23, 23,255));
            }
            doOnce = false;

        }
        if (state == State.IN || state == State.ACTIVE){
            stats.getAcceleration().modifyMult("bt_phaseDive",1 + 9 * effectLevel);
            stats.getAcceleration().modifyFlat("bt_phaseDive",1 + 9 * effectLevel);
            stats.getTimeMult().modifyMult("bt_phaseDive",1 + 2 * effectLevel);

            ship.setExtraAlphaMult(1f - (1f - 0.25f) * effectLevel);
            ship.setApplyExtraAlphaToEngines(true);
            if (effectLevel >= 0.5f) ship.setPhased(true);

            for (Map.Entry<ShipCommand, Boolean> command : commands.entrySet()){
                if (command.getValue()){
                    ship.giveCommand(command.getKey(), null,1);
                } else {
                    ship.blockCommandForOneFrame(command.getKey());
                }
            }
        } else if (state == State.OUT){
            stats.getTimeMult().unmodify("bt_phaseDive");
            float amount = engine.getElapsedInLastFrame();
            float pushRange = ship.getCollisionRadius() + 500f;
            for (CombatEntityAPI entity : CombatUtils.getEntitiesWithinRange(ship.getLocation(), pushRange)){
                if (entity instanceof ShipAPI){
                    if (((ShipAPI) entity).isStationModule() && ((ShipAPI) entity).isAlive()){
                        entity = ((ShipAPI) entity).getParentStation();
                        if (MathUtils.isWithinRange(entity.getLocation(), ship.getLocation(), pushRange))
                            continue;
                    }
                    if (((ShipAPI) entity).isStation()) entity = ship;
                }
                if (entity instanceof ShipAPI && entity == ship) continue;
                float angle = VectorUtils.getAngle(ship.getLocation(), entity.getLocation());
                Misc.getUnitVectorAtDegreeAngle(angle);
                Vector2f repulsion = Misc.getUnitVectorAtDegreeAngle(angle);
                float pushRatio = 1 - Math.min(1, MathUtils.getDistanceSquared(ship.getLocation(), entity.getLocation()) / (float) Math.pow(pushRange, 2));
                repulsion.scale(amount * 250.0f * pushRatio);
                Vector2f.add(entity.getLocation(), repulsion, entity.getLocation());

                if (entity instanceof ShipAPI) {
                    repulsion.scale(this.sizePushMulti.get(((ShipAPI) entity).getHullSize()));
                }
                Vector2f.add(entity.getVelocity(), repulsion, entity.getVelocity());

                if (entity.getOwner() != ship.getOwner()) {
                    if (entity instanceof DamagingProjectileAPI) {
                        entity.setCollisionClass(CollisionClass.PROJECTILE_FF);
                    }

                    if (entity instanceof MissileAPI) {
                        ((MissileAPI) entity).flameOut();
                    }
                }
            }
            if (CombatUtils.getShipsWithinRange(ship.getLocation(), ship.getCollisionRadius() * 0.6f).size() <= 1 || safeTime >= maxSafeTime){
                ship.setExtraAlphaMult(1f - (1f - 0.25f) * effectLevel);
                ship.setApplyExtraAlphaToEngines(false);
                if (effectLevel <= 0.5f) ship.setPhased(false);

                if (doExplosiveExit){
                    doExplosiveExit = false;
                    engine.spawnDamagingExplosion(
                            explosion,
                            ship,
                            new Vector2f(ship.getLocation()),
                            false
                    );
                    for (ShipAPI target : AIUtils.getNearbyEnemies(ship, 500)){
                        for (int i = 0; i < 1; i++) {
                            engine.spawnEmpArc(ship,
                                    ship.getLocation(),
                                    ship,
                                    target,
                                    DamageType.ENERGY,
                                    150,
                                    150,
                                    2000,
                                    null,
                                    10,
                                    new Color(172, 25, 255,255),
                                    new Color(239, 23, 23,255));
                        }
                    }
                }
            } else if (ship.isPhased()){
                safeTime += amount;
                ship.getSystem().forceState(ShipSystemAPI.SystemState.OUT, 0);
            }

        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        doOnce = true;
        doExplosiveExit = true;
        safeTime = 0;

        commands.put(ShipCommand.ACCELERATE, false);
        commands.put(ShipCommand.ACCELERATE_BACKWARDS, false);
        commands.put(ShipCommand.STRAFE_LEFT, false);
        commands.put(ShipCommand.STRAFE_RIGHT, false);

        stats.getTimeMult().unmodify("bt_phaseDive");
        stats.getAcceleration().unmodify("bt_phaseDive");

        ShipAPI ship = (ShipAPI) stats.getEntity();
        ship.setExtraAlphaMult(1);
        ship.setApplyExtraAlphaToEngines(false);
        ship.setPhased(false);
    }

    public void spawnDistortion (Vector2f shipLoc) {
        WaveDistortion wave = new WaveDistortion(shipLoc, new Vector2f());
        wave.setIntensity(1f);
        wave.setSize(250f);
        wave.flip(false);
        wave.fadeOutIntensity(0.2f);
        wave.setLifetime(0.2f);
        wave.fadeOutIntensity(0.2f);
        wave.setLocation(shipLoc);
        DistortionShader.addDistortion(wave);
    }

}
