package com.gmail.ryderzye.factionstax.engine;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsChunksChange;
import com.massivecraft.factions.event.EventFactionsCreate;
import com.massivecraft.factions.event.EventFactionsCreateFlags;
import com.massivecraft.factions.event.EventFactionsCreatePerms;
import com.massivecraft.factions.event.EventFactionsExpansions;
import com.massivecraft.factions.event.EventFactionsFactionShowAsync;
import com.massivecraft.factions.event.EventFactionsMembershipChange;
import com.gmail.ryderzye.factionstax.FactionsTax;
import com.gmail.ryderzye.factionstax.cmd.arg.TypeTaxKey;
import com.gmail.ryderzye.factionstax.entity.TConf;
import com.gmail.ryderzye.factionstax.entity.TFaction;
import com.massivecraft.massivecore.Engine;
import com.massivecraft.massivecore.PriorityLines;
import com.massivecraft.massivecore.collections.MassiveList;
import com.massivecraft.massivecore.mixin.MixinActual;
import com.massivecraft.massivecore.mixin.MixinMessage;
import com.massivecraft.massivecore.mson.Mson;
import com.massivecraft.massivecore.util.MUtil;
import com.massivecraft.massivecore.util.Txt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class EngineMain extends Engine {
    private static EngineMain i = new EngineMain();

    public static EngineMain get() {
        return i;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFactionsExpansions(EventFactionsExpansions event) {
        Map<String, Boolean> expansions = event.getExpansions();
        expansions.put("FactionsTax", Boolean.valueOf(true));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFactionsCreateFlags(EventFactionsCreateFlags event) {
        FactionsTax.getFlagTaxkick();
        FactionsTax.getFlagTaxfree();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFactionsCreatePerms(EventFactionsCreatePerms event) {
        FactionsTax.getPermTax();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFactionShow(EventFactionsFactionShowAsync event) {
        Faction faction = event.getFaction();
        TFaction tfaction = TFaction.get(faction);
        CommandSender sender = event.getSender();
        Map<String, PriorityLines> idPriorityLiness = event.getIdPriorityLiness();
        Map<String, Double> rules = tfaction.getRulesWithDefault();
        List<String> playerTaxDescParts = new ArrayList<>();
        for (String special : TypeTaxKey.specials) {
            if (special.equals("all"))
                continue;
            Double tax = rules.get(special);
            if (tax == null)
                continue;
            String playerTaxDescPart = TFaction.getTaxRuleDesc(special, tax.doubleValue(), sender);
            playerTaxDescParts.add(playerTaxDescPart);
        }
        String playerTaxDesc = Txt.implode(playerTaxDescParts, Txt.parse(" <i>| "));
        String playerTaxLine = Txt.parse("<a>Player Tax: <i>%s", new Object[] { playerTaxDesc });
        PriorityLines playerTaxPriorityLines = new PriorityLines(7500, new Object[] { playerTaxLine });
        idPriorityLiness.put("factionstax_playertax", playerTaxPriorityLines);
        double upkeepTax = tfaction.getUpkeepTax();
        String upkeepTaxDesc = tfaction.getUpkeepTaxDesc(upkeepTax);
        String upkeepTaxLine = Txt.parse("<a>Upkeep Tax: <i>%s", new Object[] { upkeepTaxDesc });
        PriorityLines upkeepTaxPriorityLines = new PriorityLines(7510, new Object[] { upkeepTaxLine });
        idPriorityLiness.put("factionstax_upkeeptax", upkeepTaxPriorityLines);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void installationGraceNotice(PlayerJoinEvent event) {
        if (!(TConf.get()).installationGraceNoticeUsed)
            return;
        final Player player = event.getPlayer();
        if (MUtil.isntPlayer(player))
            return;
        if (!MixinActual.get().isActualJoin(event))
            return;
        if (System.currentTimeMillis() >= (TConf.get()).pluginInstallationMillis + (TConf.get()).pluginInstallationGraceMillis)
            return;
        MPlayer mplayer = MPlayer.get(player);
        Faction faction = mplayer.getFaction();
        TFaction tfaction = TFaction.get(faction);
        double tax = tfaction.getTax(mplayer);
        if (faction.isNone() && tax == 0.0D)
            return;
        final MassiveList<Mson> messages = new MassiveList(Txt.parse((TConf.get()).installationGraceNoticeLines));
        messages.add(0, Txt.titleize(Txt.parse((TConf.get()).installationGraceNoticeTitle)));
        Bukkit.getScheduler().runTaskLater((Plugin)FactionsTax.get(), new Runnable() {
            public void run() {
                MixinMessage.get().messageOne(player, messages);
            }
        }, (TConf.get()).installationGraceNoticeDelayTicks);
    }

    public static void warnAndInform(final CommandSender sender, final boolean inform) {
        if (MUtil.isntSender(sender))
            return;
        Bukkit.getScheduler().runTaskLater((Plugin)FactionsTax.get(), new Runnable() {
            public void run() {
                MPlayer mplayer = MPlayer.get(sender);
                Faction faction = mplayer.getFaction();
                if (inform)
                    FactionsTax.inform(mplayer);
                FactionsTax.warnPerhaps(mplayer);
                FactionsTax.warnPerhaps(faction, mplayer);
            }
        }, 0L);
    }

    public static void warnAndInform(final Faction faction) {
        if (faction == null)
            return;
        if (faction.isNone())
            return;
        Bukkit.getScheduler().runTaskLater((Plugin)FactionsTax.get(), new Runnable() {
            public void run() {
                FactionsTax.warnPerhaps(faction);
            }
        },  0L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void warnAndInform(PlayerJoinEvent event) {
        warnAndInform((CommandSender)event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void warnAndInform(EventFactionsCreate event) {
        warnAndInform(event.getSender(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void warnAndInform(EventFactionsMembershipChange event) {
        if (event.getReason() == EventFactionsMembershipChange.MembershipChangeReason.CREATE)
            return;
        warnAndInform(event.getSender(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void warnAndInform(EventFactionsChunksChange event) {
        warnAndInform(event.getNewFaction());
    }
}
