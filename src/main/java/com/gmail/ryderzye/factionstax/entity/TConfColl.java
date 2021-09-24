package com.gmail.ryderzye.factionstax.entity;

import com.massivecraft.massivecore.store.Coll;

public class TConfColl extends Coll<TConf> {
    private static TConfColl i = new TConfColl();

    public static TConfColl get() {
        return i;
    }

    private TConfColl() {
        super("factionstax_mconf");
    }

    public void onTick() {
        super.onTick();
    }

    public void setActive(boolean active) {
        super.setActive(active);
        if (!active)
            return;
        TConf.i = (TConf)get("instance", true);
    }
}
