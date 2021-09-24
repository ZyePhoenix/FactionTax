package com.gmail.ryderzye.factionstax.cmd;

import com.gmail.ryderzye.factionstax.FactionsTax;
import com.gmail.ryderzye.factionstax.Perm;
import com.massivecraft.massivecore.command.MassiveCommandVersion;
import com.massivecraft.massivecore.command.requirement.Requirement;
import com.massivecraft.massivecore.command.requirement.RequirementHasPerm;
import org.bukkit.plugin.Plugin;

public class CmdFactionsTax extends FactionsTaxCommand {
    private static CmdFactionsTax i = new CmdFactionsTax();

    public static CmdFactionsTax get() {
        return i;
    }

    public CmdFactionsTaxInfo cmdFactionsTaxInfo = new CmdFactionsTaxInfo();

    public CmdFactionsTaxFaction cmdFactionsTaxFaction = new CmdFactionsTaxFaction();

    public CmdFactionsTaxPlayer cmdFactionsTaxPlayer = new CmdFactionsTaxPlayer();

    public CmdFactionsTaxSet cmdFactionsTaxSet = new CmdFactionsTaxSet();

    public CmdFactionsTaxRun cmdFactionsTaxRun = new CmdFactionsTaxRun();

    public MassiveCommandVersion cmdFactionsTaxVersion = (MassiveCommandVersion)(new MassiveCommandVersion((Plugin)FactionsTax.get())).addAliases(new String[] { "v", "version" }).addRequirements(new Requirement[] { (Requirement)RequirementHasPerm.get(Perm.VERSION) });

    public CmdFactionsTax() {
        setSetupEnabled(false);
        addAliases(new String[] { "tax" });
        addRequirements(new Requirement[] { (Requirement)RequirementHasPerm.get(Perm.BASECOMMAND) });
    }
}
