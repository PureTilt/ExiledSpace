package data.scripts.combat.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ptes_decoEnginesController implements EveryFrameWeaponEffectPlugin {

    boolean doOnce = true;
    ArrayList<decoEngine> engines = new ArrayList<>();

    final Map<ShipAPI.HullSize, Float> strafeMulti = new HashMap<>();

    {
        strafeMulti.put(ShipAPI.HullSize.FIGHTER, 1f);
        strafeMulti.put(ShipAPI.HullSize.FRIGATE, 1f);
        strafeMulti.put(ShipAPI.HullSize.DESTROYER, 0.75f);
        strafeMulti.put(ShipAPI.HullSize.CRUISER, 0.5f);
        strafeMulti.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.25f);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        ShipAPI ship = weapon.getShip();
        if (doOnce) {
            Global.getLogger(ptes_decoEnginesController.class).info(ship.getLocation());
            for (WeaponAPI tempWeapon : ship.getAllWeapons()){
                if (tempWeapon.getSlot().getId().startsWith("DE")){
                    ShipEngineControllerAPI.ShipEngineAPI thruster = null;
                    for (ShipEngineControllerAPI.ShipEngineAPI e : ship.getEngineController().getShipEngines()) {
                        if (MathUtils.isWithinRange(e.getLocation(), tempWeapon.getLocation(), 4)) {
                            thruster = e;
                            break;
                        }
                    }
                    engines.add(new decoEngine(tempWeapon, thruster));
                    engine.addFloatingText(tempWeapon.getLocation(), tempWeapon.getSlot().getId(),20, Color.white, ship, 0,0);
                } else {
                }
            }
            doOnce = false;
        }

        Vector2f newVector = new Vector2f();
        if (ship.getEngineController().isAccelerating()) {
            newVector.y += 1 * ship.getAcceleration();
        }
        if (ship.getEngineController().isAcceleratingBackwards()) {
            newVector.y -= 1 * ship.getDeceleration();
        }
        if (ship.getEngineController().isStrafingLeft()) {
            newVector.x -= 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        if (ship.getEngineController().isStrafingRight()) {
            newVector.x += 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        if (ship.getEngineController().isDecelerating()){
            if (ship.getVelocity().lengthSquared() > 0){
                Vector2f normalizedVel = new Vector2f(ship.getVelocity());
                normalizedVel = Misc.normalise(normalizedVel);
                normalizedVel = VectorUtils.rotate(normalizedVel, - ship.getFacing() - 90);
                newVector.x += normalizedVel.x;
                newVector.y += normalizedVel.y;
            }
        }
        newVector.scale(-1);
        Color shift = ship.getEngineController().getFlameColorShifter().getCurr();
        float ratio = shift.getAlpha() / 255f;
        float currAngle = Misc.getAngleInDegrees(newVector);

        int turn = 0;
        if (ship.getEngineController().isTurningRight()){
            turn++;
        }
        if (ship.getEngineController().isTurningLeft()){
            turn--;
        }

            /*
            int Red = Math.min(255, Math.round(engineColor.getRed() * (1f - ratio) + shift.getRed() * ratio));
            int Green = Math.min(255, Math.round(engineColor.getGreen() * (1f - ratio) + shift.getGreen() * ratio));
            int Blue = Math.min(255, Math.round(engineColor.getBlue() * (1f - ratio) + shift.getBlue() * ratio));

             */
            //engine.addHitParticle(weapon.getLocation(), (Vector2f) Misc.getUnitVectorAtDegreeAngle(currAngle + ship.getFacing() - 90).scale(300f), 20, 1, amount * 10, Color.red);
            for (decoEngine e : engines){
                float thrust = 0;

                if (!VectorUtils.isZeroVector(newVector) && Math.abs(MathUtils.getShortestRotation(e.angle, currAngle)) <= 45){
                    thrust += 1;

                    //engine.addHitParticle(e.weapon.getLocation(), (Vector2f) Misc.getUnitVectorAtDegreeAngle(e.angle + ship.getFacing() - 90).scale(300f), 20, 1, amount * 10, colorToUse);
                }
                if (turn != 0 && e.turn == turn){
                    thrust += 1;
                }

                thrust(e , Math.min(thrust,1.3f));
        }
    }

    static class decoEngine {
        public decoEngine(WeaponAPI weapon, ShipEngineControllerAPI.ShipEngineAPI engine) {
            this.weapon = weapon;
            this.engine = engine;
            this.frames = weapon.getAnimation().getNumFrames();
            angle = weapon.getSlot().getAngle() + 90;
            Vector2f loc = weapon.getSlot().getLocation();
            float absAngle = weapon.getSlot().getAngle();
            Vector2f pushDirection = Misc.getUnitVectorAtDegreeAngle(absAngle);
            float displacementAngle = MathUtils.getShortestRotation(Misc.getAngleInDegrees(loc), Misc.getAngleInDegrees(pushDirection));

            float tolerance = 20;
            if (displacementAngle > 0){
                if (Math.abs(displacementAngle - 90) < tolerance) {
                    turn = 1;
                }
            } else {
                if (Math.abs(Math.abs(displacementAngle) - 90) < tolerance) {
                    turn = -1;
                }
            }

            sizeMulti = engine.getEngineSlot().getWidth() / 3;

        }

        WeaponAPI weapon;
        ShipEngineControllerAPI.ShipEngineAPI engine;
        int turn;
        float angle;
        int frames;
        float previousThrust;
        float sizeMulti;


    }

    private void thrust(decoEngine data, float thrust) {
        ShipAPI ship = data.weapon.getShip();
        Vector2f size = new Vector2f(15, 80);
        float smooth = 0.15f;
        WeaponAPI weapon = data.weapon;
        if (data.engine.isDisabled()) thrust = 0f;

        //random sprite

        int frame = MathUtils.getRandomNumberInRange(1, data.frames - 1);
        if (frame == weapon.getAnimation().getNumFrames()) {
            frame = 1;
        }
        weapon.getAnimation().setFrame(frame);
        //Global.getLogger(ptes_decoEnginesController.class).info(weapon.getSlot().getId());
        SpriteAPI sprite = weapon.getSprite();


        //target angle
        float length = thrust;


        //thrust is reduced while the engine isn't facing the target angle, then smoothed
        length -= data.previousThrust;
        length *= smooth;
        length += data.previousThrust;
        data.previousThrust = length;


        //finally the actual sprite manipulation
        float width = (length * size.x / 2 + size.x / 2) * data.sizeMulti;
        float height =( length * size.y + (float) Math.random() * 3 + 3) * data.sizeMulti;
        sprite.setSize(width, height);
        sprite.setCenter(width / 2, height / 2);

        //clamp the thrust then color stuff
        length = Math.max(0, Math.min(1, length));

        Color engineColor = data.engine.getEngineColor();
        Color shift = ship.getEngineController().getFlameColorShifter().getCurr();
        float ratio = shift.getAlpha() / 255f;
        int Red = Math.min(255, Math.round(engineColor.getRed() * (1f - ratio) + shift.getRed() * ratio));
        int Green = Math.min(255, Math.round((engineColor.getGreen() * (1f - ratio) + shift.getGreen() * ratio) * (0.5f + length / 2)));
        int Blue = Math.min(255, Math.round((engineColor.getBlue() * (1f - ratio) + shift.getBlue() * ratio) * (0.75f + length / 4)));

        sprite.setColor(new Color(Red, Green, Blue));
    }
}
