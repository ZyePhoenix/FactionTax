package com.gmail.ryderzye.factionstax.engine;

import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.event.EventFactionsDisband;
import com.gmail.ryderzye.factionstax.entity.TFaction;
import com.gmail.ryderzye.factionstax.entity.TFactionColl;
import com.massivecraft.massivecore.Engine;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

public class EngineDataCleaner extends Engine {
    private static EngineDataCleaner i = new EngineDataCleaner();

    public static EngineDataCleaner get() {
        return i;
    }

    public EngineDataCleaner() {
        setPeriod(Long.valueOf(1200L));
        setSync(false);
    }

    public void run() {
        final List<TFaction> toremove = new ArrayList<>();
        for (TFaction tfaction : TFactionColl.get().getAll()) {
            if (FactionColl.get().containsId(tfaction.getId()))
                continue;
            toremove.add(tfaction);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)getPlugin(), new Runnable() {
            public void run() {
                for (TFaction tfaction : toremove)
                    tfaction.detach();
            }
        },  0L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFactionDisband(EventFactionsDisband event) {
        String factionId = event.getFactionId();
        if (factionId == null)
            return;
        if (!TFactionColl.get().containsId(factionId))
            return;
        TFactionColl.get().detachId(factionId);
    }
}
