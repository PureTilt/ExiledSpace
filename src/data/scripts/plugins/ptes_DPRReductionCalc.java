package data.scripts.plugins;

public class ptes_DPRReductionCalc {

    public static float DPRReduction(float FP){
        float DPreduction = 1f;
        if (FP > 500) DPreduction = Math.max(0.5f, Math.min(1, 500f / FP));
        return DPreduction;
    }
}
