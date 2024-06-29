package org.galliumpowered.permission;

import org.galliumpowered.world.entity.Player;

public class PermissionOwner {
    String name;

    public PermissionOwner(Player player) {
        this.name = player.getUUID();
    }

    public PermissionOwner(Group group) {
        this.name = group.getName();
    }

    /**
     * The name of the permission owner
     * @return Permission owner name
     */
    public String getName() {
        return name;
    }
}
