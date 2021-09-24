package com.gmail.ryderzye.factionstax;

import com.massivecraft.massivecore.Identified;
import com.massivecraft.massivecore.util.PermissionUtil;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;

public enum Perm implements Identified {
    BASECOMMAND, INFO, FACTION, PLAYER, SET, RUN, VERSION;

    private final String id;

    public String getId() {
        return this.id;
    }

    Perm() {
        this.id = PermissionUtil.createPermissionId((Plugin)FactionsTax.get(), this);
    }

    public boolean has(Permissible permissible, boolean verboose) {
        return PermissionUtil.hasPermission(permissible, this, verboose);
    }

    public boolean has(Permissible permissible) {
        return PermissionUtil.hasPermission(permissible, this);
    }
}
