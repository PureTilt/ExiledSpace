package data.scripts.mapObjectives;

import com.fs.starfarer.api.EveryFrameScript;
import data.scripts.items.ptes_mapItemInfo;
import data.world.systems.ptes_baseSystemScript;

public abstract class ptes_baseMapObjective implements EveryFrameScript {


    public void init(ptes_mapItemInfo map, ptes_baseSystemScript script){

    }

    public boolean objectiveComplete(){
        return false;
    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }


}
