package com.gmail.ryderzye.factionstax.entity;

import com.massivecraft.massivecore.store.Coll;

public class TFactionColl extends Coll<TFaction> {
    private static TFactionColl i = new TFactionColl();

    public static TFactionColl get() {
        return i;
    }

    private TFactionColl() {
        super("factionstax_faction");
    }

    public void onTick() {
        super.onTick();
    }
}
