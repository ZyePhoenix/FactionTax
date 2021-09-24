package com.gmail.ryderzye.factionstax.cmd.arg;

import com.massivecraft.factions.cmd.type.TypeMPlayer;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.type.TypeAbstractSelect;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.bukkit.command.CommandSender;

public class TypeTaxKey extends TypeAbstractSelect<String> {
    public static final Set<Rel> rels;
    public static final List<String> specials;
    public static final List<String> altNames;
    public static final String ALL = "all";
    private static TypeTaxKey i;

    public static TypeTaxKey get() {
        return i;
    }

    public TypeTaxKey() {
        super(String.class);
    }

    public String getName() {
        return "tax target";
    }

    public String select(String str, CommandSender sender) {
        str = str.toLowerCase();
        Iterator var3 = specials.iterator();

        String special;
        do {
            if (!var3.hasNext()) {
                try {
                    MPlayer mplayer = (MPlayer)TypeMPlayer.get().read(str, sender);
                    return mplayer.getId();
                } catch (MassiveException var5) {
                    return null;
                }
            }

            special = (String)var3.next();
        } while(!special.startsWith(str));

        return special;
    }

    public Collection<String> altNames(CommandSender sender) {
        Collection<String> ret = super.altNames(sender);
        ret.add("the name of a specific player");
        return ret;
    }

    public Collection<String> getTabList(CommandSender sender, String arg) {
        return specials;
    }

    public Collection<String> getAll(CommandSender sender) {
        return specials;
    }

    static {
        rels = EnumSet.of(Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.RECRUIT);
        specials = new LinkedList();
        specials.add("default");
        Iterator var0 = rels.iterator();

        while(var0.hasNext()) {
            Rel rel = (Rel)var0.next();
            specials.add(rel.name().toLowerCase());
        }

        specials.add("all");
        altNames = new LinkedList(specials);
        altNames.add("the name of a specific player");
        i = new TypeTaxKey();
    }
}
