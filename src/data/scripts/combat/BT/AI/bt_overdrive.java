package data.scripts.combat.BT.AI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class bt_overdrive extends BaseShipSystemScript {

    float
        overloadDur = 3,
        damageTakenMulti = 0.5f,
        speedMulti = 2,
        ballisticRoFMulti = 1.5f,
        ballisticFluxMulti = 1 / ballisticRoFMulti;


    IntervalUtil arcTimer = new IntervalUtil(0.05f,0.1f); //for charge up

    Color arcFringe = new Color(255,255,255,255);
    Color arcCore = new Color(255,255,255,255);

    boolean doOnce = true;



    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }


        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        switch (state){
            case IN:
            case ACTIVE:
                stats.getArmorDamageTakenMult().modifyMult("bt_overdrive",damageTakenMulti);
                stats.getHullDamageTakenMult().modifyMult("bt_overdrive",damageTakenMulti);
                stats.getShieldDamageTakenMult().modifyMult("bt_overdrive",damageTakenMulti);

                stats.getMaxSpeed().modifyMult("bt_overdrive",speedMulti);
                stats.getAcceleration().modifyMult("bt_overdrive",speedMulti);
                stats.getDeceleration().modifyMult("bt_overdrive",speedMulti);

                stats.getMaxTurnRate().modifyMult("bt_overdrive",speedMulti * 2);
                stats.getTurnAcceleration().modifyMult("bt_overdrive",speedMulti * 2);

                stats.getBallisticRoFMult().modifyMult("bt_overdrive",ballisticRoFMulti);
                stats.getBallisticWeaponFluxCostMod().modifyMult("bt_overdrive",ballisticFluxMulti);

                arcTimer.advance(amount);
                if (arcTimer.intervalElapsed()){
                    arcOnBounds(ship, Global.getCombatEngine());
                }
                break;
            case OUT:
                stats.getArmorDamageTakenMult().unmodify("bt_overdrive");
                stats.getHullDamageTakenMult().unmodify("bt_overdrive");
                stats.getShieldDamageTakenMult().unmodify("bt_overdrive");

                stats.getMaxSpeed().unmodify("bt_overdrive");
                stats.getAcceleration().unmodify("bt_overdrive");
                stats.getDeceleration().unmodify("bt_overdrive");

                stats.getMaxTurnRate().unmodify("bt_overdrive");
                stats.getTurnAcceleration().unmodify("bt_overdrive");

                stats.getBallisticRoFMult().unmodify("bt_overdrive");
                stats.getBallisticWeaponFluxCostMod().unmodify("bt_overdrive");
                if (doOnce){
                    ship.getFluxTracker().beginOverloadWithTotalBaseDuration(overloadDur);
                }
        }
    }

    public void arcOnBounds(ShipAPI target, CombatEngineAPI engine){
        target.getExactBounds().update(target.getLocation(), target.getFacing());
        List<BoundsAPI.SegmentAPI> Segments = target.getExactBounds().getSegments();
        int firstBound = MathUtils.getRandomNumberInRange(0,Segments.size() - 1);
        int secondBound = firstBound + 2;
        if (secondBound >= Segments.size() - 1) secondBound -= Segments.size() - 1;
        Vector2f firstBoundLoc = Segments.get(firstBound).getP1();
        Vector2f secondBoundLoc = Segments.get(secondBound).getP1();
        engine.spawnEmpArcVisual(firstBoundLoc,target,secondBoundLoc,target,10,arcFringe,arcCore);
        //return new Vector2f((firstBoundLoc.x + secondBoundLoc.x) * 0.5f, (firstBoundLoc.y + secondBoundLoc.y) * 0.5f);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Overdrive active", false);
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        doOnce = true;
        stats.getArmorDamageTakenMult().unmodify("bt_overdrive");
        stats.getHullDamageTakenMult().unmodify("bt_overdrive");
        stats.getShieldDamageTakenMult().unmodify("bt_overdrive");

        stats.getMaxSpeed().unmodify("bt_overdrive");
        stats.getAcceleration().unmodify("bt_overdrive");
        stats.getDeceleration().unmodify("bt_overdrive");

        stats.getMaxTurnRate().unmodify("bt_overdrive");
        stats.getTurnAcceleration().unmodify("bt_overdrive");

        stats.getBallisticRoFMult().unmodify("bt_overdrive");
        stats.getBallisticWeaponFluxCostMod().unmodify("bt_overdrive");
    }


}
