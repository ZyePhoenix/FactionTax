package com.gmail.ryderzye.factionstax.cmd;

import com.gmail.ryderzye.factionstax.engine.EngineTask;
import com.massivecraft.massivecore.command.Visibility;

public class CmdFactionsTaxRun extends FactionsTaxCommand {
    public CmdFactionsTaxRun() {
        setVisibility(Visibility.SECRET);
    }

    public void perform() {
        EngineTask.get().invoke(System.currentTimeMillis());
    }
}
