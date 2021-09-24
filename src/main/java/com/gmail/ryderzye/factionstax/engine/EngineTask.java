package com.gmail.ryderzye.factionstax.engine;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.RelationParticipator;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import com.massivecraft.factions.event.EventFactionsDisband;
import com.massivecraft.factions.event.EventFactionsMembershipChange;
import com.gmail.ryderzye.factionstax.FactionsTax;
import com.gmail.ryderzye.factionstax.entity.TConf;
import com.gmail.ryderzye.factionstax.entity.TFaction;
import com.massivecraft.massivecore.Engine;
import com.massivecraft.massivecore.MassiveCore;
import com.massivecraft.massivecore.collections.MassiveList;
import com.massivecraft.massivecore.collections.MassiveMap;
import com.massivecraft.massivecore.mixin.MixinMessage;
import com.massivecraft.massivecore.money.Money;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.util.IdUtil;
import com.massivecraft.massivecore.util.Txt;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EngineTask extends Engine {
    private static EngineTask i = new EngineTask();

    public static EngineTask get() {
        return i;
    }

    public EngineTask() {
        setPeriod(Long.valueOf(20L));
    }

    public void run() {
        long now = System.currentTimeMillis();
        if (!MassiveCore.isTaskServer())
            return;
        if (!TConf.get().isEnabled())
            return;
        if (TConf.get().getFirstMillis() > now)
            return;
        long currentInvocation = TConf.get().getInvocationFromMillis(now);
        long lastInvocation = TConf.get().getInvocationFromMillis((TConf.get()).lastMillis);
        if (currentInvocation == lastInvocation)
            return;
        invoke(now);
    }

    public void invoke(long now) {
        (TConf.get()).lastMillis = now;
        TConf.get().changed();
        long millisPlayerInactive = (TConf.get()).playerInactiveMillis;
        MassiveList massiveList1 = new MassiveList(new Object[] { Txt.titleize("FactionsTax Start"), Txt.parse("<i>Taxation of players and factions will now occur."), Txt.parse("<i>The server might hang for a moment."), "" });
        MixinMessage.get().messageAll((Collection)massiveList1);
        long before = System.nanoTime();
        int count = 0;
        MassiveMap<Faction, MassiveList> massiveMap = new MassiveMap();
        for (Faction faction : FactionColl.get().getAll()) {
            massiveMap.put(faction, new MassiveList());
            count++;
        }
        for (MPlayer mplayer : MPlayerColl.get().getAll()) {
            MassiveList<MPlayer> massiveList = null;
            Faction faction = mplayer.getFaction();
            if (faction == null)
                continue;
            if (millisPlayerInactive > 0L) {
                Long millisLast = mplayer.getLastPlayed();
                if (millisLast == null || millisLast.longValue() == 0L)
                    continue;
                long millisOffline = now - millisLast.longValue();
                if (millisOffline >= millisPlayerInactive)
                    continue;
            }
            List<MPlayer> players = (List<MPlayer>)massiveMap.get(faction);
            if (players == null) {
                massiveList = new MassiveList();
                massiveMap.put(faction, massiveList);
                count++;
            }
            massiveList.add(mplayer);
            count++;
        }
        Map<Faction, Integer> factionToCount = BoardColl.get().getFactionToCount();
        for (Map.Entry<Faction, List<MPlayer>> entry : massiveMap.entrySet()) {
            Faction faction = entry.getKey();
            List<MPlayer> mplayers = entry.getValue();
            double playerTax = 0.0D;
            for (MPlayer mplayer : mplayers)
                playerTax += taxPlayer(mplayer);
            Integer chunkCount = factionToCount.get(faction);
            if (chunkCount == null)
                chunkCount = Integer.valueOf(0);
            double factionTax = taxFaction(faction, chunkCount.intValue());
            TFaction tfaction = TFaction.get(faction);
            if (tfaction == null)
                continue;
            tfaction.addPlayerHistory(playerTax);
            tfaction.addFactionHistory(factionTax);
        }
        long after = System.nanoTime();
        double millis = (after - before) / 1000000.0D;
        double millisPerThing = millis / count;
        MassiveList massiveList2 = new MassiveList(new Object[] { "", Txt.titleize("FactionsTax End"), Txt.parse("<g>Taxation Complete!"), Txt.parse("<k>Time: <v>%dms<i> / <v>%d<i> = <v>%.5fms<i>", new Object[] { Long.valueOf((long)millis), Integer.valueOf(count), Double.valueOf(millisPerThing) }) });
        MixinMessage.get().messageAll((Collection)massiveList2);
    }

    public static double taxFaction(Faction faction, int chunkCount) {
        TFaction tfaction = TFaction.get(faction);
        double tax = tfaction.getUpkeepTax(chunkCount);
        if (tax == 0.0D)
            return tax;
        String toAccount = (MConf.get()).econUniverseAccount;
        if (toAccount.isEmpty())
            toAccount = null;
        boolean success = Money.move(faction, toAccount, null, tax, (Collection)new MassiveList((Object[])new String[] { "Factions", "Tax", "Faction" }));
        if (success) {
            faction.msg("%sFaction %s<i> payed <h>%s<i> upkeep tax.", new Object[] { faction.getColorTo((RelationParticipator)faction), faction.getName((RelationParticipator)faction), Money.format(tax, true) });
            return tax;
        }
        if (tax < 0.0D)
            return 0.0D;
        punishFaction(faction);
        return 0.0D;
    }

    public static double taxPlayer(MPlayer mplayer) {
        Faction faction = mplayer.getFaction();
        TFaction tfaction = TFaction.get(faction);
        double tax = tfaction.getTax(mplayer);
        if (tax == 0.0D)
            return tax;
        boolean success = Money.move(mplayer, faction, null, tax, (Collection)new MassiveList((Object[])new String[] { "Factions", "Tax", "Player" }));
        if (success) {
            faction.msg("<h>%s<i> payed <h>%s<i> tax to <h>%s<i>.", new Object[] { mplayer.describeTo((RelationParticipator)faction, true), Money.format(tax, true), faction.getName((RelationParticipator)faction) });
            return tax;
        }
        if (tax < 0.0D)
            return 0.0D;
        punishPlayer(faction, mplayer);
        return 0.0D;
    }

    public static void punishPlayer(Faction faction, MPlayer mplayer) {
        if (faction.isNone())
            return;
        if (mplayer.getRank().isLeader() || !faction.getFlag(FactionsTax.getFlagTaxkick())) {
            faction.msg("%s<i> couldn't afford tax!", new Object[] { mplayer.describeTo((RelationParticipator)faction, true) });
            return;
        }
        kickPlayer(mplayer);
    }

    public static void kickPlayer(MPlayer mplayer) {
        EventFactionsMembershipChange event = new EventFactionsMembershipChange(null, mplayer, FactionColl.get().getNone(), EventFactionsMembershipChange.MembershipChangeReason.KICK);
        event.run();
        if (event.isCancelled())
            return;
        Faction faction = mplayer.getFaction();
        faction.msg("%s<i> couldn't afford tax and was kicked!", new Object[] { mplayer.describeTo((RelationParticipator)faction, true) });
        if ((MConf.get()).logFactionKick) {
            MPlayer mconsole = MPlayer.get(IdUtil.getConsole());
            mconsole.msg("%s<i> couldn't afford tax and was kicked from <h>%s<i>.", new Object[] { mplayer.describeTo((RelationParticipator)mconsole, true), faction.getName((RelationParticipator)mconsole) });
        }
        faction.uninvite(mplayer);
        mplayer.resetFactionData();
    }

    public static void punishFaction(Faction faction) {
        if (faction.isNone())
            return;
        if (faction.getFlag(MFlag.getFlagPermanent()))
            return;
        if ((TConf.get()).upkeepFailDisband) {
            disbandFaction(faction);
        } else if ((TConf.get()).upkeepFailUnclaimall) {
            unclaimFaction(faction);
        }
    }

    public static boolean unclaimFaction(Faction faction) {
        Set<PS> chunks = BoardColl.get().getChunks(faction);
        Faction newFaction = FactionColl.get().getNone();
        MPlayer msender = MPlayer.get(IdUtil.getConsole());
        String formatOne = "<h>%s<i> %s <h>%d <i>chunk using tax unset all.";
        String formatMany = "<h>%s<i> %s <h>%d <i>chunks using tax unset all.";
        boolean pre = msender.isOverriding();
        msender.setOverriding(Boolean.valueOf(true));
        boolean ret = msender.tryClaim(newFaction, chunks, "<h>%s<i> %s <h>%d <i>chunk using tax unset all.", "<h>%s<i> %s <h>%d <i>chunks using tax unset all.");
        msender.setOverriding(Boolean.valueOf(pre));
        if (ret)
            faction.msg("%s<i> couldn't afford upkeep tax, so all land was unset.", new Object[] { faction.describeTo((RelationParticipator)faction, true) });
        return ret;
    }

    public static boolean disbandFaction(Faction faction) {
        EventFactionsDisband event = new EventFactionsDisband(null, faction);
        event.run();
        if (event.isCancelled())
            return false;
        Faction newFaction = FactionColl.get().getNone();
        for (MPlayer mplayer : faction.getMPlayers()) {
            EventFactionsMembershipChange membershipChangeEvent = new EventFactionsMembershipChange(null, mplayer, newFaction, EventFactionsMembershipChange.MembershipChangeReason.DISBAND);
            membershipChangeEvent.run();
        }
        faction.msg("%s<i> couldn't afford upkeep tax and was disbanded.", new Object[] { faction.describeTo((RelationParticipator)faction, true) });
        if ((MConf.get()).logFactionDisband)
            Factions.get().log(new Object[] { Txt.parse("<i>The faction <h>%s <i>(<h>%s<i>) was disbanded because it couldn't afford upkeep tax.", new Object[] { faction.getName(), faction.getId() }) });
        faction.detach();
        return true;
    }
}
