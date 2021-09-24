package com.gmail.ryderzye.factionstax.cmd;

import com.massivecraft.factions.RelationParticipator;
import com.massivecraft.factions.cmd.type.TypeMPlayer;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.factions.entity.MPlayer;
import com.gmail.ryderzye.factionstax.FactionsTax;
import com.gmail.ryderzye.factionstax.entity.TFaction;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.money.Money;
import com.massivecraft.massivecore.util.Txt;

public class CmdFactionsTaxPlayer extends FactionsTaxCommand {
    public CmdFactionsTaxPlayer() {
        addParameter(TypeMPlayer.get(), "player", "you");
    }

    public void perform() throws MassiveException {
        MPlayer mplayer = (MPlayer)readArg(this.msender);
        Faction faction = mplayer.getFaction();
        TFaction tfaction = TFaction.get(faction);
        double tax = tfaction.getTax(mplayer);
        double account = Money.get(mplayer);
        Object title = "Player Tax " + mplayer.getDisplayName(this.msender);
        title = Txt.titleize(title);
        message(title);
        String factionDesc = faction.getName((RelationParticipator)this.msender);
        msg("<a>Your Faction: <i>%s", new Object[] { factionDesc });
        String taxDesc = Txt.parse("<h>%s<i> / <h>%s<i> in your account.", new Object[] { Money.format(tax, true), Money.format(account, true) });
        msg("<a>Your Tax: <i>%s", new Object[] { taxDesc });
        double money = Money.get(mplayer);
        boolean afford = (money >= tax);
        String affordDesc = FactionsTax.affordDesc(tax, money);
        msg("<a>Can Afford: <i>%s", new Object[] { affordDesc });
        if (!afford) {
            MFlag flagTaxKick = FactionsTax.getFlagTaxkick();
            boolean taxKick = faction.getFlag(flagTaxKick);
            String taxKickDesc = flagTaxKick.getStateDesc(taxKick, true, false, false, true, true);
            msg("<a>Player Kick: <i>%s", new Object[] { taxKickDesc });
        }
        FactionsTax.warnPerhaps(mplayer, this.msender);
    }
}
