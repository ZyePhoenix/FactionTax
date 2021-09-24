package com.gmail.ryderzye.factionstax.cmd;

import com.massivecraft.factions.cmd.FactionsCommand;
import com.gmail.ryderzye.factionstax.Perm;

public class FactionsTaxCommand extends FactionsCommand {
    public FactionsTaxCommand() {
        setSetupPermBaseClassName(CmdFactionsTax.class.getSimpleName());
        setSetupPermClass(Perm.class);
    }
}
