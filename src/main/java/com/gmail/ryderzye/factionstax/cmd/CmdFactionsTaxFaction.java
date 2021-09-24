package com.gmail.ryderzye.factionstax.cmd;

import com.massivecraft.factions.RelationParticipator;
import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MFlag;
import com.gmail.ryderzye.factionstax.FactionsTax;
import com.gmail.ryderzye.factionstax.entity.TFaction;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.type.Type;
import com.massivecraft.massivecore.money.Money;
import com.massivecraft.massivecore.util.Txt;
import java.util.Map;

public class CmdFactionsTaxFaction extends FactionsTaxCommand {
    public CmdFactionsTaxFaction() {
        addParameter((Type)TypeFaction.get(), "faction", "you");
    }

    public void perform() throws MassiveException {
        Faction faction = (Faction)readArg(this.msenderFaction);
        TFaction tfaction = TFaction.get(faction);
        Object title = "Faction Tax " + faction.describeTo((RelationParticipator)this.msender, true);
        title = Txt.titleize(title);
        message(title);
        double tax = tfaction.getUpkeepTax();
        String taxDesc = tfaction.getUpkeepTaxDesc(tax);
        msg("<a>Upkeep Tax: <i>%s", new Object[] { taxDesc });
        double money = Money.get(faction);
        String affordDesc = FactionsTax.affordDesc(tax, money);
        msg("<a>Can Afford: <i>%s", new Object[] { affordDesc });
        FactionsTax.warnPerhaps(faction, this.msender);
        String factionHistoryDesc = tfaction.getFactionHistoryDesc();
        msg("<a>Upkeep History: <i>%s", new Object[] { factionHistoryDesc });
        String playerHistoryDesc = tfaction.getPlayerHistoryDesc();
        msg("<a>Player History: <i>%s", new Object[] { playerHistoryDesc });
        MFlag flagTaxKick = FactionsTax.getFlagTaxkick();
        boolean taxKick = faction.getFlag(flagTaxKick);
        String taxKickDesc = flagTaxKick.getStateDesc(taxKick, true, false, false, true, true);
        msg("<a>Player Kick: <i>%s", new Object[] { taxKickDesc });
        msg("<a>Player Tax Rules:");
        Map<String, Double> rules = tfaction.getRulesWithDefault();
        for (Map.Entry<String, Double> rule : rules.entrySet())
            message(TFaction.getTaxRuleDesc(rule, this.sender));
    }
}
