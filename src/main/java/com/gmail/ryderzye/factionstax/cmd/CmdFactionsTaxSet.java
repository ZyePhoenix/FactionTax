package com.gmail.ryderzye.factionstax.cmd;


import com.massivecraft.factions.RelationParticipator;
import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.gmail.ryderzye.factionstax.FactionsTax;
import com.gmail.ryderzye.factionstax.cmd.arg.TypeTaxKey;
import com.gmail.ryderzye.factionstax.entity.TConf;
import com.gmail.ryderzye.factionstax.entity.TFaction;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.type.Type;
import com.massivecraft.massivecore.command.type.TypeNullable;
import com.massivecraft.massivecore.command.type.primitive.TypeDouble;
import com.massivecraft.massivecore.mixin.MixinDisplayName;
import com.massivecraft.massivecore.money.Money;

public class CmdFactionsTaxSet extends FactionsTaxCommand {
    public CmdFactionsTaxSet() {
        addParameter((Type)TypeNullable.get((Type)TypeDouble.get()), "amount|none");
        addParameter((Type)TypeTaxKey.get(), "default|rank|player|all", "default");
        addParameter((Type)TypeFaction.get(), "faction", "you");
    }

    public void perform() throws MassiveException {
        Double value = (Double)readArg();
        if (value != null) {
            value = Double.valueOf(Money.prepare(value.doubleValue()));
            double taxMaximum = (TConf.get()).taxMaximum;
            if (value.doubleValue() > taxMaximum) {
                msg("<b>The maximum tax is <h>%s<b>.", new Object[] { Money.format(taxMaximum) });
                return;
            }
            double taxMinimum = (TConf.get()).taxMinimum;
            if (value.doubleValue() < taxMinimum) {
                msg("<b>The minimum tax is <h>%s<b>.", new Object[] { Money.format(taxMinimum) });
                return;
            }
        }
        String valueDesc = "none";
        if (value != null)
            valueDesc = Money.format(value.doubleValue());
        String key = (String)readArg("default");
        String keyDesc = key;
        if (!TypeTaxKey.specials.contains(key))
            keyDesc = MixinDisplayName.get().getDisplayName(key, this.sender);
        Faction faction = (Faction)readArg(this.msenderFaction);
        TFaction tfaction = TFaction.get(faction);
        if (!FactionsTax.getPermTax().has(this.msender, faction, true))
            return;
        boolean changed = tfaction.setTax(key, value);
        if (!changed) {
            msg("<i>%s's<i> tax for <h>%s<i> was already <h>%s<i>.", new Object[] { faction.describeTo((RelationParticipator)this.msender, true), keyDesc, valueDesc });
            return;
        }
        faction.msg("<i>%s's<i> tax for <h>%s<i> is now <h>%s<i>.", new Object[] { faction.getName((RelationParticipator)this.msender), keyDesc, valueDesc });
        if (value != null && value.doubleValue() < 0.0D)
            faction.msg("<b>NOTE: <i>This negative tax will work like a salary!");
        for (MPlayer mplayer : faction.getMPlayersWhereOnline(true)) {
            FactionsTax.inform(mplayer);
            FactionsTax.warnPerhaps(mplayer);
        }
    }
}
