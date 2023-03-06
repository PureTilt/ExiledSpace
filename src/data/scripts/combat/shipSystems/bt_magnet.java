package data.scripts.combat.shipSystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class bt_magnet extends BaseShipSystemScript {

    float maxRange = 1000;
    float forceAmount = 5000;
    IntervalUtil arcTimer = new IntervalUtil(0.1f,0.2f); //for charge up

    Color arcFringe = new Color(255,255,255,255);
    Color arcCore = new Color(255,255,255,255);

    ShipAPI target = null;
    boolean doOnce = true;
    boolean systemActivation = true;



    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        if (doOnce){
            target = findTarget(ship);
            doOnce = false;
        }

        float amount = Global.getCombatEngine().getElapsedInLastFrame();

        switch (state){
            case IN:
                arcTimer.advance(amount);
                if (arcTimer.intervalElapsed()){
                    Vector2f arcLoc = getPointOnBounds(target);
                    Global.getCombatEngine().spawnEmpArcVisual(ship.getLocation(),ship,arcLoc,target,10,arcFringe,arcCore);
                }
                break;
            case ACTIVE:
                if (systemActivation){
                    //if want to change amount of arc on activation change second number
                    for (int i = 0; i < 15; i++){
                        Vector2f arcLoc = getPointOnBounds(target);
                        Global.getCombatEngine().spawnEmpArcVisual(ship.getLocation(),ship,arcLoc,target,10,arcFringe,arcCore);
                    }
                    CombatUtils.applyForce(target,VectorUtils.getAngle(target.getLocation(), ship.getLocation()),forceAmount);
                    systemActivation = false;
                }
                break;
        }
        //debug stuff
        /*
        List<BoundsAPI.SegmentAPI> Segments = target.getExactBounds().getSegments();
        target.setRenderBounds(true);
        for (BoundsAPI.SegmentAPI segment : Segments){
            {
                Vector2f arcLoc = segment.getP1();
                //arcLoc = new Vector2f(arcLoc.x + target.getLocation().x, arcLoc.y + target.getLocation().y);
                Global.getCombatEngine().addHitParticle(arcLoc, new Vector2f(), 20, 1, amount, Color.CYAN);
            }
            {
                Vector2f arcLoc = segment.getP1();
                //arcLoc = new Vector2f(arcLoc.x + target.getLocation().x,arcLoc.y + target.getLocation().y);
                Global.getCombatEngine().addHitParticle(arcLoc,new Vector2f(), 20,1,amount,Color.RED);
            }
        }
         */
    }

    public Vector2f getPointOnBounds(ShipAPI target){
        target.getExactBounds().update(target.getLocation(), target.getFacing());
        List<BoundsAPI.SegmentAPI> Segments = target.getExactBounds().getSegments();
        int firstBound = MathUtils.getRandomNumberInRange(0,Segments.size() - 1);
        int secondBound = 0;
        if (firstBound != Segments.size() - 1) secondBound = firstBound + 1;
        Vector2f firstBoundLoc = Segments.get(firstBound).getP1();
        Vector2f secondBoundLoc = Segments.get(secondBound).getP1();
        return new Vector2f((firstBoundLoc.x + secondBoundLoc.x) * 0.5f, (firstBoundLoc.y + secondBoundLoc.y) * 0.5f);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        if (target!= null) target.setRenderBounds(false);
        target = null;
        doOnce = true;
        systemActivation = true;
    }

    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        //if (true) return true;
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }

    protected ShipAPI findTarget(ShipAPI ship) {
        float range = getMaxRange(ship);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum) target = null;
        } else {
            if (target == null || target.getOwner() == ship.getOwner()) {
                if (player) {
                    target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FIGHTER, range, true);
                } else {
                    Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
                    if (test instanceof ShipAPI) {
                        target = (ShipAPI) test;
                        float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                        float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                        if (dist > range + radSum) target = null;
                    }
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FIGHTER, range, true);
            }
        }

        return target;
    }

    public float getMaxRange(ShipAPI ship) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(maxRange);
        //return RANGE;
    }
}
