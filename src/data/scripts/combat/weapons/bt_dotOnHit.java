package data.scripts.combat.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class bt_dotOnHit implements OnHitEffectPlugin {

    float duration = 10f;
    float totalDamage = 200f;

    float fractionPerSecond = 1 / duration;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        Global.getLogger(bt_dotOnHit.class).info("proced");
        if (!shieldHit && target instanceof ShipAPI) {
            Global.getLogger(bt_dotOnHit.class).info("gotAdded");
            engine.addPlugin(new EveryFrameCombatPlugin() {

                float totalDuration = 0;
                final float initialFacing = target.getFacing();
                final Vector2f shipRefHitLoc = new Vector2f(point.x - target.getLocation().x, point.y - target.getLocation().y);
                final IntervalUtil damageTimer = new IntervalUtil(0.25f, 0.25f);
                final IntervalUtil FXTimer = new IntervalUtil(0.1f, 0.1f);


                @Override
                public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {

                }

                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    if (engine.isPaused()) return;
                    totalDuration += amount;
                    damageTimer.advance(amount);
                    FXTimer.advance(amount);
                    Vector2f hitLoc = new Vector2f();
                    if (damageTimer.intervalElapsed() || FXTimer.intervalElapsed()) {
                        hitLoc = VectorUtils.rotate(new Vector2f(shipRefHitLoc), target.getFacing() - initialFacing);
                        hitLoc = new Vector2f(hitLoc.x + target.getLocation().x, hitLoc.y + target.getLocation().y);
                    }
                    if (damageTimer.intervalElapsed()) {
                        engine.applyDamage(target, hitLoc, totalDamage * fractionPerSecond * damageTimer.getIntervalDuration(), DamageType.FRAGMENTATION, 0, true, true, projectile.getSource());
                    }
                    if (FXTimer.intervalElapsed()) {
                        engine.addSwirlyNebulaParticle(hitLoc, target.getVelocity(), MathUtils.getRandomNumberInRange(10, 30), MathUtils.getRandomNumberInRange(1, 2), MathUtils.getRandomNumberInRange(0.8f, 1.2f), MathUtils.getRandomNumberInRange(0.3f, 0.7f), MathUtils.getRandomNumberInRange(0.4f, 0.6f), new Color(MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(100, 160), MathUtils.getRandomNumberInRange(0, 60), MathUtils.getRandomNumberInRange(100, 200)), true);
                        engine.addNebulaParticle(hitLoc, target.getVelocity(), MathUtils.getRandomNumberInRange(10, 30), MathUtils.getRandomNumberInRange(1, 2), MathUtils.getRandomNumberInRange(0.8f, 1.2f), MathUtils.getRandomNumberInRange(0.3f, 0.7f), MathUtils.getRandomNumberInRange(0.4f, 0.6f), new Color(MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(100, 160), MathUtils.getRandomNumberInRange(0, 60), MathUtils.getRandomNumberInRange(100, 200)), true);

                        engine.addSwirlyNebulaParticle(hitLoc, target.getVelocity(), MathUtils.getRandomNumberInRange(15, 45), MathUtils.getRandomNumberInRange(3, 4), MathUtils.getRandomNumberInRange(0.8f, 1.2f), MathUtils.getRandomNumberInRange(0.3f, 0.7f), MathUtils.getRandomNumberInRange(0.4f, 0.6f), new Color(MathUtils.getRandomNumberInRange(20, 40), MathUtils.getRandomNumberInRange(20, 40), MathUtils.getRandomNumberInRange(20, 40), MathUtils.getRandomNumberInRange(50, 150)), true);
                        engine.addNebulaParticle(hitLoc, target.getVelocity(), MathUtils.getRandomNumberInRange(15, 45), MathUtils.getRandomNumberInRange(3, 4), MathUtils.getRandomNumberInRange(0.8f, 1.2f), MathUtils.getRandomNumberInRange(0.3f, 0.7f), MathUtils.getRandomNumberInRange(0.4f, 0.6f), new Color(MathUtils.getRandomNumberInRange(20, 40), MathUtils.getRandomNumberInRange(20, 40), MathUtils.getRandomNumberInRange(20, 40), MathUtils.getRandomNumberInRange(50, 150)), true);
                    }
                    if (totalDuration >= duration) engine.removePlugin(this);
                }

                @Override
                public void renderInWorldCoords(ViewportAPI viewport) {

                }

                @Override
                public void renderInUICoords(ViewportAPI viewport) {

                }

                @Override
                public void init(CombatEngineAPI engine) {

                }
            });
        }
    }
}
