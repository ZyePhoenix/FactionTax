package com.gmail.ryderzye.factionstax;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.cmd.CmdFactions;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.gmail.ryderzye.factionstax.cmd.CmdFactionsTax;
import com.gmail.ryderzye.factionstax.engine.EngineDataCleaner;
import com.gmail.ryderzye.factionstax.engine.EngineMain;
import com.gmail.ryderzye.factionstax.engine.EngineTask;
import com.gmail.ryderzye.factionstax.entity.TConf;
import com.gmail.ryderzye.factionstax.entity.TConfColl;
import com.gmail.ryderzye.factionstax.entity.TFaction;
import com.gmail.ryderzye.factionstax.entity.TFactionColl;
import com.massivecraft.massivecore.MassivePlugin;
import com.massivecraft.massivecore.command.MassiveCommand;
import com.massivecraft.massivecore.money.Money;
import com.massivecraft.massivecore.util.MUtil;
import com.massivecraft.massivecore.util.TimeDiffUtil;
import com.massivecraft.massivecore.util.TimeUnit;
import com.massivecraft.massivecore.util.Txt;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FactionsTax extends MassivePlugin {
    private static FactionsTax i;

    public static FactionsTax get() {
        return i;
    }

    public FactionsTax() {
        i = this;
    }

    public void onEnableInner() {
        activate(new Object[] { TConfColl.class, TFactionColl.class, EngineMain.class, EngineTask.class, EngineDataCleaner.class });
        getPermTax();
        getFlagTaxkick();
        getFlagTaxfree();
        CmdFactions cmdFactions = CmdFactions.get();
        cmdFactions.replaceChild((MassiveCommand)CmdFactionsTax.get(), (MassiveCommand)cmdFactions.cmdFactionsTax);
        CmdFactionsTax.get().setupAddChildren();
        CmdFactionsTax.get().setupChildren();
    }

    public static MPerm getPermTax() {
        return MPerm.getCreative(16500, "tax", "tax", "manage taxes", MUtil.set((Object[])new Rel[] { Rel.LEADER }), false, true, true);
    }

    public static MFlag getFlagTaxkick() {
        return MFlag.getCreative(2500, "taxkick", "taxkick", "Will players who can't afford tax get kicked?", "Players who can't afford tax will get kicked.", "Players who can't afford tax will stay.", false, true, true);
    }

    public static MFlag getFlagTaxfree() {
        return MFlag.getCreative(2510, "taxfree", "taxfree", "Is the faction pardoned from upkeep tax?", "The faction is pardoned from upkeep tax.", "The faction pays upkeep tax as usual.", false, false, true);
    }

    public static String affordDesc(double tax, double money) {
        boolean canAfford = (money >= tax);
        String ret = canAfford ? Const.YES : Const.NO;
        ret = ret + Txt.parse("<i> ");
        if (tax == 0.0D) {
            ret = ret + "since there is nothing to pay.";
        } else if (tax < 0.0D) {
            ret = ret + "anyone can \"afford\" getting payed.";
        } else if (canAfford) {
            long now = System.currentTimeMillis();
            int affordTimes = (int)Math.floor(money / tax);
            long affordMillis = TConf.get().getNextMillisFromMillis(now) - now + (affordTimes - 1) * (TConf.get()).periodMillis;
            LinkedHashMap<TimeUnit, Long> affordCounts = TimeDiffUtil.unitcounts(affordMillis, TimeUnit.getAllButMillis());
            affordCounts = TimeDiffUtil.limit(affordCounts, 2);
            ret = ret + "for the next " + TimeDiffUtil.formatedVerboose(affordCounts) + Txt.parse("<i>.");
        } else {
            double missing = tax - money;
            String missingDesc = Txt.parse("<h>%s <b>missing!", new Object[] { Money.format(missing, true) });
            ret = ret + missingDesc;
        }
        return ret;
    }

    public static boolean warnShould(MPlayer mplayer) {
        Faction faction = mplayer.getFaction();
        TFaction tfaction = TFaction.get(faction);
        double tax = tfaction.getTax(mplayer);
        if (tax <= 0.0D)
            return false;
        double balance = Money.get(mplayer);
        int count = (int)Math.floor(balance / tax);
        if (count >= (TConf.get()).playerWarningTaxCount)
            return false;
        return true;
    }

    public static boolean warnShould(Faction faction) {
        if (faction.isNone())
            return false;
        TFaction tfaction = TFaction.get(faction);
        double tax = tfaction.getUpkeepTax();
        if (tax <= 0.0D)
            return false;
        double balance = Money.get(faction);
        int count = (int)Math.floor(balance / tax);
        if (count >= (TConf.get()).factionWarningTaxCount)
            return false;
        return true;
    }

    public static void warnDo(MPlayer mplayer) {
        warnDo(mplayer, (MPlayer)null);
    }

    public static void warnDo(MPlayer mplayer, MPlayer mrecipient) {
        ArrayList<String> messages = Txt.parse((TConf.get()).playerWarningLines);
        mrecipient.message(messages);
    }

    public static void warnDo(Faction faction) {
        warnDo(faction, (MPlayer)null);
    }

    public static void warnDo(Faction faction, MPlayer mrecipient) {
        ArrayList<String> messages = Txt.parse((TConf.get()).factionWarningLines);
        if (mrecipient != null) {
            mrecipient.message(messages);
        } else {
            faction.sendMessage(messages);
        }
    }

    public static boolean warnPerhaps(MPlayer mplayer) {
        return warnPerhaps(mplayer, mplayer);
    }

    public static boolean warnPerhaps(MPlayer mplayer, MPlayer mrecipient) {
        boolean should = warnShould(mplayer);
        if (should)
            warnDo(mplayer, mrecipient);
        return should;
    }

    public static boolean warnPerhaps(Faction faction) {
        return warnPerhaps(faction, (MPlayer)null);
    }

    public static boolean warnPerhaps(Faction faction, MPlayer mrecipient) {
        boolean should = warnShould(faction);
        if (should)
            warnDo(faction, mrecipient);
        return should;
    }

    public static void inform(MPlayer mplayer) {
        Faction faction = mplayer.getFaction();
        TFaction tfaction = TFaction.get(faction);
        double tax = tfaction.getTax(mplayer);
        double account = Money.get(mplayer);
        String taxDesc = Txt.parse("<h>%s<i> / <h>%s<i> in your account.", new Object[] { Money.format(tax, true), Money.format(account, true) });
        String message = Txt.parse("<a>Your Tax: <i>%s", new Object[] { taxDesc });
        mplayer.message(message);
    }
}
